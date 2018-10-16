package de.bwl.bwfla.emil;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.database.MongodbEaasConnector;
import de.bwl.bwfla.emil.datatypes.rest.ClassificationResult;
import org.apache.tamaya.inject.ConfigurationInjection;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@ApplicationScoped
public class ClassificationData {

    @Inject
    private MongodbEaasConnector dbConnector;

//  name of collection in eaas database
    private String collectionName = "classificationCache";
//  key inside collection
    private String objectKey = "classificationResult";
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

    }

    public ClassificationResult load(String objectId) throws NoSuchElementException, UnknownHostException {
        try {
            return dbConnector.getJaxbObject(collectionName, objectId, objectKey + idDBkey, ClassificationResult.class);
        } catch ( JAXBException e) {
            e.printStackTrace();
            throw new NoSuchElementException();
        }
    }

    public void save(ClassificationResult c, String objectId) throws JAXBException, IOException, BWFLAException {
        // classification wasn't successful
        if (c.getObjectId() == null || c.getObjectId().equals(""))
            return;
        dbConnector.saveDoc(collectionName, objectId, objectKey + idDBkey, c.JSONvalue(false));
    }

    public List<String> getEnvironmentDependencies(String envId) throws UnknownHostException {
        List<String> objects = new ArrayList<>();
        ArrayList<ClassificationResult> results = null;
        try {
            results = dbConnector.getJaxbObjects(collectionName, objectKey, ClassificationResult.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            return objects;
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
