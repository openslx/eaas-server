package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@ApplicationScoped
public class ClassificationData {

    @Inject
    private MongodbEaasConnector dbConnector;
    private MongodbEaasConnector.DatabaseInstance db;

    @Inject
    @Config("emil.classificationDatabase")
    private String dbName;

    //  name of collection in eaas database
    public static String collectionName = "classificationCache";
    //  key inside collection
    public static String parentElement = "classificationResult";
    //  if we want to get result for specific object, we need to call it for id. Example: "classificationResult.objectId"
    private String idDBkey = ".objectId";

    protected static final Logger LOG = Logger.getLogger(MongodbEaasConnector.class.getCanonicalName());


//    private ClassificationData() {
//
//        // ConfigurationInjection.getConfigurationInjector().configure(this);
//    }

    @PostConstruct
    public void init()
    {
        db = dbConnector.getInstance(dbName);
    }

    public ClassificationResult load(String objectId) throws NoSuchElementException {
        try {
            return db.getJaxbRootbasedObject(collectionName, objectId, parentElement + idDBkey, ClassificationResult.class);
        } catch (BWFLAException e) {

            e.printStackTrace();
            throw new NoSuchElementException();
        }
    }


    public void save(ClassificationResult c, String objectId) throws JAXBException, IOException, BWFLAException {
        // classification wasn't successful
        if (c.getObjectId() == null || c.getObjectId().equals(""))
            return;
        db.saveDoc(collectionName, objectId, parentElement + idDBkey, c.JSONvalue(false));
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
}
