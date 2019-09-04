package de.bwl.bwfla.emil.utils.tasks;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.utils.ArchiveAdapter;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.EnvironmentInfo;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageclassifier.client.ClassificationEntry;
import de.bwl.bwfla.imageclassifier.client.Identification;
import de.bwl.bwfla.imageclassifier.client.IdentificationRequest;
import de.bwl.bwfla.imageclassifier.client.ImageClassifier;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import de.bwl.bwfla.imageproposer.client.Proposal;
import de.bwl.bwfla.imageproposer.client.ProposalRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClassificationTask extends AbstractTask<ClassificationResult> {


    private static final Logger LOG = Logger.getLogger(ClassificationTask.class.getName());

    public ClassificationTask(ProposeEnvironmentsRequest req)
    {
        this.request = req;
        this.emilEnvRepo = req.archive.environmentRepository();
        this.envHelper = req.archive.environments();
        this.imageClassifier = req.archive.imageClassifier();
        this.imageProposer = req.archive.imageProposer();
    }

    private final EmilEnvironmentRepository emilEnvRepo;
    private final ProposeEnvironmentsRequest request;
    private final EnvironmentsAdapter envHelper;
    private final ImageClassifier imageClassifier;
    private final ImageProposer imageProposer;

    public static class ProposeEnvironmentsRequest
    {
        String objectId;
        ArchiveAdapter archive;
        ClassificationResult cached;
    }

    private List<EnvironmentInfo> resolveEmilEnvironments(String objectId, Collection<String> proposedEnvironments) throws IOException, BWFLAException {

        HashMap<String, List<EmilEnvironment>> envMap = new HashMap<>();
        List<EmilEnvironment> environments = emilEnvRepo.getEmilEnvironments();
//        if (environments != null && environments.size() == 0) {
//             FIXME
//             we need to call EmilEnvironmentData.init() here
//        }
        HashSet<String> knownEnvironments = new HashSet<>();
        for (String envId : proposedEnvironments) {
            try {
                envHelper.getEnvironmentById(envId);
            } catch (BWFLAException e)
            {
                continue;
            }
            List<EmilEnvironment> emilEnvironments = emilEnvRepo.getChildren(envId, environments);
            List<EmilEnvironment> resultList = new ArrayList<>();
            for(EmilEnvironment emilEnv : emilEnvironments) {
                if(emilEnv instanceof EmilObjectEnvironment) // do this later
                    continue;
                if (emilEnv != null) {
                    if(!knownEnvironments.contains(emilEnv.getEnvId())) {
                        if(!proposedEnvironments.contains(emilEnv.getEnvId())) {
                            resultList.add(emilEnv);
                            knownEnvironments.add(emilEnv.getEnvId());
                        }
                        else {
                            // LOG.info("proposed envs contains: " + emilEnv.getEnvId() + " skipp env");
                            EmilEnvironment _env = emilEnvRepo.getEmilEnvironmentById(envId);
                            if(_env instanceof EmilObjectEnvironment)
                                break;

                            if(_env != null && _env.isVisible())
                            {
                                resultList.add(_env);
                            }
                            break;
                        }
                    }
                }
            }
            envMap.put(envId, resultList);
        }

        List<EnvironmentInfo> result = new ArrayList<>();

        List<EmilObjectEnvironment> emilObjectEnvironments = emilEnvRepo.getEmilObjectEnvironmentByObject(objectId);

        for(EmilObjectEnvironment objEnv : emilObjectEnvironments) {
            EnvironmentInfo ei = new EnvironmentInfo(objEnv.getEnvId(), objEnv.getTitle());
            // LOG.info("found oe: " + objEnv.getTitle());
            ei.setObjectEnvironment(true);
            result.add(ei);
        }

        for(String envId: proposedEnvironments)
        {
            List<EmilEnvironment> resolved = envMap.get(envId);
            if(resolved == null)
                continue;

            for(EmilEnvironment env : resolved)
                result.add(new EnvironmentInfo(env.getEnvId(), env.getTitle()));
        }
        // LOG.info("resolve environment: received " + proposedEnvironments + " proposed envs, resolved " + result.size());

        return result;
    }


    private ClassificationResult classifyObject(String archiveId, String objectId) throws BWFLAException {
        try {

            String fcString;
            try {
                fcString = request.archive.getFileCollectionForObject(archiveId, objectId);
            }
            catch (BWFLAException e)
            {
                LOG.warning("file collection for " + objectId + " is null");

                return new ClassificationResult(e);
            }

            FileCollection fc = FileCollection.fromValue(fcString);

            if(fc == null)
            {
                LOG.warning("file collection for " + objectId + " is null");
                return new ClassificationResult( new BWFLAException("could not load file collection for object " + objectId));
            }

            ClassificationResult response;

            IdentificationRequest req = new IdentificationRequest(fc, null);
            Identification<ClassificationEntry> id = this.imageClassifier.getClassification(req);

            HashMap<String, Identification.IdentificationDetails<ClassificationEntry>> data = id.getIdentificationData();
            if(data == null)
            {
                LOG.warning("identification failed for objectID:" + objectId );
                return new ClassificationResult();
            }

            HashMap<String, ClassificationResult.IdentificationData> fileFormats = new HashMap<>();
            HashMap<String, DiskType> mediaFormats = new HashMap<>();
            for(FileCollectionEntry fce : fc.files)
            {
                Identification.IdentificationDetails<ClassificationEntry> details = data.get(fce.getId());
                if(details == null)
                    continue;

                // FIXME
                List<ClassificationResult.FileFormat> fmts = details.getEntries().stream().map((ClassificationEntry ce) -> {
                    return new ClassificationResult.FileFormat(ce.getType(), ce.getTypeName(), ce.getCount(), ce.getFromDate(), ce.getToDate());
                }).collect(Collectors.toList());

                ClassificationResult.IdentificationData d = new ClassificationResult.IdentificationData();
                d.setFileFormats(fmts);
                fileFormats.put(fce.getId(), d);

                if(details.getDiskType() != null)
                    mediaFormats.put(fce.getId(), details.getDiskType());
            }
            response = new ClassificationResult(objectId, fileFormats, mediaFormats);

            return response;
        } catch (Throwable t) {
            LOG.warning("classification failed: " + t.getMessage());
            LOG.log(Level.SEVERE, t.getMessage(), t);
            return new ClassificationResult();
        }

    }

    private ClassificationResult propose(ClassificationResult response, String objectId) {
        HashMap<String, DiskType> mediaFormats = new HashMap<>();
        if(response.getMediaFormats() != null)
        {
            for(String key : response.getMediaFormats().keySet()) {
                if(response.getMediaFormats().get(key) != null)
                    mediaFormats.put(key, response.getMediaFormats().get(key));
            }
        }

        HashMap<String, List<ProposalRequest.Entry>> fileFormats = new HashMap<>();
        if(response.getFileFormatMap() != null)
        {
            for(String key : response.getFileFormatMap().keySet())
            {
                List<ProposalRequest.Entry> proposalList = new ArrayList<>();
                List<ClassificationResult.FileFormat> foundFmts = response.getFileFormatMap().get(key).getFileFormats();
                if(foundFmts == null)
                    continue;

                for(ClassificationResult.FileFormat fmt : foundFmts)
                {
                    proposalList.add(new ProposalRequest.Entry(fmt.getPuid(), fmt.getCount()));
                }
                fileFormats.put(key, proposalList);
            }
        }

        Proposal proposal = null;
        try {
            proposal = imageProposer.propose(new ProposalRequest(fileFormats, mediaFormats));
        } catch (InterruptedException e) {
            return new ClassificationResult(new BWFLAException(e));
        }

        List<EnvironmentInfo> environmentList;
        List<ClassificationResult.OperatingSystem> suggested = new ArrayList<>();;

        try {
            environmentList = resolveEmilEnvironments(objectId, proposal.getImages());
        } catch (IOException | BWFLAException e) {
            return new ClassificationResult(new BWFLAException(e));
        }

        List<EnvironmentInfo> defaultList = new ArrayList<>();
        try {
            for (String osId : proposal.getSuggested().keySet()) {
                ClassificationResult.OperatingSystem os = new ClassificationResult.OperatingSystem(osId, proposal.getSuggested().get(osId));
                String envId = envHelper.getDefaultEnvironment(osId);
                if (envId != null) {
                    EmilEnvironment emilEnv = emilEnvRepo.getEmilEnvironmentById(envId);
                    if (emilEnv != null) {
                        EnvironmentInfo info = new EnvironmentInfo(emilEnv.getEnvId(), emilEnv.getTitle());
                        os.setDefaultEnvironment(info);

                        EnvironmentInfo infoL = new EnvironmentInfo(emilEnv.getEnvId(), emilEnv.getTitle() + " (D)");
                        if(!defaultList.stream().filter(o -> o.getId().equals(infoL.getId())).findFirst().isPresent())
                            defaultList.add(infoL);
                    }
                }
                suggested.add(os);
            }
            response.setSuggested(suggested);
        }
        catch (BWFLAException e)
        {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        response.setSuggested(suggested);

        if(defaultList.size() > 0) {
            for(EnvironmentInfo info : environmentList)
            {
                if(info.isObjectEnvironment()) {
                    LOG.info("adding oe to default list.");
                    defaultList.add(0, info);
                }
            }
            response.setEnvironmentList(defaultList);
        }
        else
            response.setEnvironmentList(environmentList);

//=======
//        HashMap<String, RelatedQIDS> qidsHashMap = new HashMap<>();
//        environmentList.forEach(env -> {
//            String os = null;
//            try {
//                os =  ((MachineConfiguration) envHelper.getEnvironmentById(env.getId())).getOperatingSystemId();
//
//                if(os != null) {
//                    // sanitze: remove ':'
//                    os = os.replace(':', '_');
//                    qidsHashMap.put(env.getId(), QIDsFinder.findFollowingAndFollowedQIDS(os));
//                }
//            } catch (BWFLAException e) {
//                e.printStackTrace();
//            }
//        });
//
//        response.setEnvironmentList(environmentList);
//>>>>>>> master
        return response;
    }


    private ClassificationResult propose()
    {
        ClassificationResult result = propose(cached, objectId);
    }


    private ClassificationResult fullClassification()
    {
        ClassificationResult response = this.classifyObject(archiveId, objectId);
        try {
            // do not saveDoc the proposal result here
            classification.save(response, objectId);
            response = propose(response, objectId);
        } catch (JAXBException | IOException e1) {
            LOG.log(Level.SEVERE, e1.getMessage(), e1);
            throw new BWFLAException(e1);
        }
        return response;
    }

    @Override
    protected ClassificationResult execute() throws Exception {

        try {
            LOG.severe("getEnvironmentsForObject " + forceCharacterization + " " + forceProposal);
            // shortcut to force characterization if requested:
            if (forceCharacterization) {
                throw new NoSuchElementException();
            }

            ClassificationResult cached = null;

            try { cached = classification.load(objectId); } catch (NoSuchElementException ignore) { }
            if(cached == null) {
                if(noUpdate)
                    return new ClassificationResult();

                throw new NoSuchElementException();
            }

            if(forceProposal || cached.getEnvironmentList().size() == 0)
            {

                try {
                    classification.save(result, objectId);
                } catch (JAXBException |IOException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
                return result;
            }
            else
                return cached;
        } catch (NoSuchElementException e) {
            // if no data for the object id, classify it
            LOG.severe("no such element exception");

        }
    }

}
