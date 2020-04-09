package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.EnvironmentInfo;
import de.bwl.bwfla.common.services.rest.ErrorInformation;
import de.bwl.bwfla.emil.datatypes.OverrideCharacterizationRequest;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import de.bwl.bwfla.emil.datatypes.rest.ClientClassificationRequest;
import de.bwl.bwfla.emil.datatypes.rest.TaskStateResponse;
import de.bwl.bwfla.common.services.security.AuthenticatedUser;
import de.bwl.bwfla.common.services.security.Role;
import de.bwl.bwfla.common.services.security.Secured;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.utils.TaskManager;
import de.bwl.bwfla.emil.tasks.ClassificationTask;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.imageclassifier.client.ImageClassifier;
import de.bwl.bwfla.imageproposer.client.ImageProposer;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/classification")
public class ObjectClassification {

    @Inject
    private MongodbEaasConnector dbConnector;
    private MongodbEaasConnector.DatabaseInstance db;

    @Inject
    @Config(value = "emil.classificationservice")
    private String classificationService;

    @Inject
    @Config(value = "emil.imageproposerservice")
    private String imageProposerService;

    @Inject
    @Config("emil.classificationDatabase")
    private String dbName;

    @Inject
    private EmilObjectData objects;

    @Inject
    private EmilEnvironmentRepository metadata;

    @Inject
    private DatabaseEnvironmentsAdapter environments;

    @Inject
    private TaskManager taskManager;

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser;


    //  name of collection in eaas database
    static String collectionName = "classificationCache";
    //  key inside collection
    static String parentElement = "classificationResult";
    //  if we want to get result for specific object, we need to call it for id. Example: "classificationResult.objectId"
    private String idDBkey = ".objectId";

    protected static final Logger LOG = Logger.getLogger(MongodbEaasConnector.class.getCanonicalName());

    static private ImageClassifier imageClassifier;
    static private ImageProposer imageProposer;

    @PostConstruct
    public void init()
    {
        db = dbConnector.getInstance(dbName);

        imageClassifier = new ImageClassifier(classificationService + "/imageclassifier");
        try {
            imageProposer = new ImageProposer(imageProposerService + "/imageproposer");
        } catch (IllegalArgumentException e) {}
    }


    @Secured(roles = {Role.RESTRCITED})
    @POST
    @Path("/overrideObjectCharacterization")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response overrideObjectCharacterization(OverrideCharacterizationRequest request) {
        String objectId = request.getObjectId();
        String objectArchive = request.getObjectArchive();
        List<EnvironmentInfo> environments = request.getEnvironments();
        try {
            setCachedEnvironmentsForObject(objectId, environments, request.getDescription());
            return Emil.successMessageResponse("");
        } catch (Exception e) {
            return Emil.errorMessageResponse(e.getMessage());
        }
    }

    @Secured(roles = {Role.PUBLIC})
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public TaskStateResponse classify(ClientClassificationRequest request)
    {
        try {
            if(request.getObjectId() != null) {
                FileCollection fc = objects.getFileCollection(request.getArchiveId(), request.getObjectId());
                return new TaskStateResponse(getEnvironmentsForObject(fc,
                        request.isUpdateClassification(),
                        request.isUpdateProposal(),
                        request.isNoUpdate()));
            }
            else if(request.getUrl() != null && request.getFilename() != null)
            {
                return new TaskStateResponse(getEnvironmentsForObject(request.getUrl(), request.getFilename()));
            }
            else
                throw new BadRequestException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorInformation("invalid request"))
                        .build());
        } catch (BWFLAException e) {
            throw new BadRequestException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorInformation(e.getMessage()))
                    .build());
        }
    }


    public String getEnvironmentsForObject(String url, String filename) throws BWFLAException {

        ClassificationTask.ClassifyObjectRequest request = new ClassificationTask.ClassifyObjectRequest();
        request.url = url;
        request.filename = filename;
        request.classification = this;
        request.environments = environments;
        request.metadata = metadata;
        request.input = null;
        request.userCtx = null;

        if(authenticatedUser != null)
            request.userCtx = authenticatedUser.getUserId();

        return taskManager.submitTask(new ClassificationTask(request));
    }

    public String getEnvironmentsForObject(FileCollection fc,
                                                         boolean forceCharacterization,
                                                         boolean forceProposal,
                                                         boolean noUpdate) throws BWFLAException {

        ClassificationTask.ClassifyObjectRequest request = new ClassificationTask.ClassifyObjectRequest();
        request.fileCollection = fc;
        request.classification = this;
        request.environments = environments;
        request.metadata = metadata;
        request.input = null;
        request.noUpdate = noUpdate;
        request.forceProposal = forceProposal;
        request.userCtx = null;

        if(authenticatedUser != null)
            request.userCtx = authenticatedUser.getUserId();

        if(!forceCharacterization || noUpdate)
            try {
                request.input = load(fc.id);
            } catch (NoSuchElementException ignore) {};

        return taskManager.submitTask(new ClassificationTask(request));
    }

    public ClassificationResult getCachedEnvironmentsForObject(String objectId)
    {
        try {
            return load(objectId);
        } catch (NoSuchElementException e) {
            LOG.severe("no classification data found");
            return new ClassificationResult();
        }
    }

    private void setCachedEnvironmentsForObject(String objectId, List<EnvironmentInfo> environments, String userDescription)
            throws BWFLAException {

        ClassificationResult result = null;
        try {
            result = load(objectId);
        } catch (NoSuchElementException e) {
            result = new ClassificationResult(objectId);
        }

        result.setEnvironmentList(environments);
        result.setUserDescription(userDescription);
        try {
            save(result);
        } catch (JAXBException  e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new BWFLAException(e);
        }
    }


    public void export(File exportDirectory) throws JAXBException, IOException {
         List<ClassificationResult> results = db.getJaxbObjects(collectionName, parentElement + idDBkey, ClassificationResult.class);
         File currentExportDir = new File(exportDirectory, (new Date().toString()));
         Files.createDirectories(currentExportDir.toPath());
         for(ClassificationResult result : results)
         {
             if(result.getObjectId() == null)
                 continue;

             File exportFile = new File(currentExportDir, result.getObjectId() + ".xml");
             try {
                 PrintWriter out = new PrintWriter(exportFile);
                 out.print(result.value(true));
                 out.close();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
         }
    }

    private ClassificationResult load(String objectId) throws NoSuchElementException {
        try {
            return db.getJaxbRootbasedObject(collectionName, objectId, parentElement + idDBkey, ClassificationResult.class);
        } catch (BWFLAException e) {

            e.printStackTrace();
            throw new NoSuchElementException();
        }
    }

    public void save(ClassificationResult c) throws JAXBException, BWFLAException {
        // classification wasn't successful
        if (c.getObjectId() == null || c.getObjectId().equals(""))
            return;
        db.saveDoc(collectionName, c.getObjectId(), parentElement + idDBkey, c.JSONvalue(false));
    }

    public List<String> getEnvironmentDependencies(String envId) {
        List<String> objects = new ArrayList<>();
        ArrayList<ClassificationResult> results = null;
        try {
            results = db.getJaxbObjects(collectionName, parentElement, ClassificationResult.class);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        if (results == null) {
            return objects;
        }
        for (ClassificationResult f : results) {
            if (f.hasReferenceTo(envId)) {
                objects.add(f.getObjectId());
            }
        }
        return objects;
    }

    public void dump() {
        File exportDir = new File("/home/bwfla/export/classification-data");
        if (!exportDir.exists())
            try {
                Files.createDirectories(exportDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            export(exportDir);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }
    }

    void cleanupClassificationData(String envId)
    {
        db.deleteDoc(collectionName, envId, parentElement + ".environmentList.id", false);
    }

    public ImageProposer imageProposer() {
        return imageProposer;
    }

    public ImageClassifier imageClassifier() {
        return imageClassifier;
    }
}
