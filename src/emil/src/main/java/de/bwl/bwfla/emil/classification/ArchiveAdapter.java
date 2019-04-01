package de.bwl.bwfla.emil.classification;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.emil.ClassificationData;
import de.bwl.bwfla.emil.datatypes.EmilObjectEnvironment;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emil.datatypes.EnvironmentInfo;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.wikidata.reader.QIDsFinder;
import de.bwl.bwfla.wikidata.reader.entities.RelatedQIDS;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
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
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

@ApplicationScoped
public class ArchiveAdapter {
    protected final static Logger LOG = Logger.getLogger(ArchiveAdapter.class.getName());
    @Inject
    @Config(value = "ws.objectarchive")
    private String objectArchive;

    @Inject
    @Config(value = "ws.imagearchive")
    private String imageArchive;

    @Inject
    @Config(value = "emil.classificationservice")
    private String classificationService;

    @Inject
    @Config(value = "emil.imageproposerservice")
    private String imageProposerService;

    @Inject
    @Config(value = "commonconf.serverdatadir")
    protected String serverdatadir;


    @Inject
	@Config(value = "emil.emilobjectenvironmentspaths")
	private String emilObjectEnvironmentsPath;

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser;


    protected ObjectArchiveHelper objHelper;
    protected EnvironmentsAdapter envHelper;
    protected ImageClassifier imageClassifier;
    protected ImageProposer imageProposer;

    @Inject
    protected ClassificationData db;

    @Inject
    private EmilEnvironmentRepository emilEnvRepo;

    @PostConstruct
    public void init()  {
        objHelper = new ObjectArchiveHelper(objectArchive);
        envHelper = new EnvironmentsAdapter(imageArchive);
        imageClassifier = new ImageClassifier(classificationService + "/imageclassifier");
        try {
            imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
        } catch (IllegalArgumentException e) {}
    }

    public ClassificationResult getEnvironmentsForObject(String archiveId, String objectId) throws BWFLAException {
        return this.getEnvironmentsForObject(archiveId, objectId, false, false);
    }

    public ClassificationResult getEnvironmentsForObject(String archiveId, String objectId, boolean forceCharacterization, boolean forceProposal) throws BWFLAException {
        try {
            // shortcut to force characterization if requested:
            if (forceCharacterization) {
                throw new NoSuchElementException();
            }
            ClassificationResult cached = db.load(objectId);
            if(!forceProposal && cached.getEnvironmentList() != null && cached.getEnvironmentList().size() > 0) {
                return cached;
            }
            else {
                ClassificationResult result = propose(cached, objectId);
                return result;
            }
        } catch (NoSuchElementException e) {
            // if no data for the object id, classify it
            ClassificationResult response = this.classifyObject(archiveId, objectId);
            try {
                // do not saveDoc the proposal result here
                db.save(response, objectId);
                response = propose(response, objectId);
            } catch (JAXBException | IOException e1) {
                LOG.log(Level.SEVERE, e1.getMessage(), e1);
                throw new BWFLAException(e1);
            }
            return response;
        }
    }

    public ClassificationResult getClassificationResultForObject(String objectId){
        return db.load(objectId);
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
                            LOG.info("porposed envs contains: " + emilEnv.getEnvId() + " skipp env");
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
                fcString = getFileCollectionForObject(archiveId, objectId);
            }
            catch (BWFLAException e)
            {
                LOG.warning("file collection for " + objectId + " is null");
                return new ClassificationResult();
            }

            FileCollection fc = FileCollection.fromValue(fcString);

            if(fc == null)
            {
                LOG.warning("file collection for " + objectId + " is null");
                return new ClassificationResult();
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

    private ClassificationResult propose(ClassificationResult response, String objectId)
    {
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
        if(defaultList.size() > 0)
            // response.setEnvironmentList(defaultList);
            environmentList.addAll(defaultList);

        HashMap<String, RelatedQIDS> qidsHashMap = new HashMap<>();
        environmentList.forEach(env -> {
            String os = null;
            try {
                os =  ((MachineConfiguration) envHelper.getEnvironmentById(env.getId())).getOperatingSystemId();

                if(os != null) {
                    // sanitze: remove ':'
                    os = os.replace(':', '_');
                    qidsHashMap.put(env.getId(), QIDsFinder.findFollowingAndFollowedQIDS(os));
                }
            } catch (BWFLAException e) {
                e.printStackTrace();
            }
        });

        response.setEnvironmentList(environmentList);
        return response;
    }


    public String getFileCollectionForObject(String archiveId, String objectId)
            throws  BWFLAException {
        if(archiveId == null)
        {
            if(authenticatedUser == null || authenticatedUser.getUsername() == null)
                archiveId = "default";
            else
                archiveId = authenticatedUser.getUsername();
        }

        try {
            FileCollection fc = objHelper.getObjectReference(archiveId, objectId);
            if (fc == null)
                throw new BWFLAException("Returned FileCollection is null for '" + archiveId + "/" + objectId + "'!");
            return fc.value();
            
        } catch (JAXBException e) {
            throw new BWFLAException("Cannot find object reference for '" + objectId + "'", e);
        }
    }

    public void setCachedEnvironmentsForObject(String objectId, List<EnvironmentInfo> environments, String userDescription)
            throws BWFLAException {

        ClassificationResult result = null;
        try {
            result = db.load(objectId);
        } catch (NoSuchElementException e) {
            result = new ClassificationResult();
        }

        result.setEnvironmentList(environments);
        result.setUserDescription(userDescription);
        try {
            db.save(result, objectId);
        } catch (JAXBException | IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new BWFLAException(e);
        }
    }

    public List<String> getEnvironmentDependencies(String envId) throws IOException, JAXBException {
        return db.getEnvironmentDependencies(envId);
    }

}
