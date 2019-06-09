package de.bwl.bwfla.emil.datatypes;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bwl.bwfla.common.database.MongodbEaasConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class UserSessions {

    @Inject
    private MongodbEaasConnector dbConnector;

    private MongodbEaasConnector.DatabaseInstance db;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, EmilSessionEnvironment>> sessionCache;

    Logger LOG = Logger.getLogger(UserSessions.class.getName());

    @Inject
    private DatabaseEnvironmentsAdapter environmentsAdapter;

    @Inject
    @Config(value = "emil.usersessionretention")
    protected long retention;

    @Inject
    @Config("emil.emilDatabase")
    private String dbName;

    private final String emilDbCollectionName = "eaasSessions";

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    private void initUserSessions()  {

        sessionCache = new ConcurrentHashMap<>();

        LOG.info("Setting user retention time to: " + retention);
        scheduledExecutorService.scheduleAtFixedRate(new Monitor(retention), 1, 1, TimeUnit.MINUTES);

        db = dbConnector.getInstance(dbName);
        try {
            db.createIndex(emilDbCollectionName, "envId");
            db.ensureTimestamp(emilDbCollectionName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            List<EmilSessionEnvironment> sessionList = db.getRootlessJaxbObjects(EmilEnvironmentRepository.MetadataCollection.PUBLIC,
                    EmilObjectEnvironment.class.getCanonicalName(), "type");
            for(EmilSessionEnvironment session : sessionList)
            {
                try {
                    add(session);
                }
                catch (BWFLAException e)
                {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } catch (BWFLAException e) {
            e.printStackTrace();
            return;
        }
    }

    public synchronized List<EmilSessionEnvironment> toList()
    {
        List<EmilSessionEnvironment> result = new ArrayList<>();
        for(String user : sessionCache.keySet()) {
            ConcurrentHashMap<String,EmilSessionEnvironment> userMap = sessionCache.get(user);
            if(userMap != null)
                result.addAll(userMap.values());
        }
        return result;
    }

    public synchronized void add(EmilSessionEnvironment session) throws BWFLAException {
        if(session == null)
            throw new BWFLAException("UserSession.add Session is null");


        if(session.getUserId() == null)
            throw new BWFLAException("UserSession.add() -> userID null ");

        ConcurrentHashMap<String, EmilSessionEnvironment> userMap = sessionCache.get(session.getUserId());
        if(userMap == null)
            userMap = new ConcurrentHashMap<>();
        userMap.put(session.objectId, session);
        sessionCache.put(session.getUserId(), userMap);
    }

    public EmilSessionEnvironment get(String userId, String objectId)
    {
        if(userId == null || objectId == null)
            return null;

        ConcurrentHashMap<String, EmilSessionEnvironment> userMap = sessionCache.get(userId);
        if(userMap == null)
            return null;

       return userMap.get(objectId);
    }

    public synchronized void delete(EmilSessionEnvironment session) throws BWFLAException {
        if(session == null)
            return;

        ConcurrentHashMap<String, EmilSessionEnvironment> userMap = sessionCache.get(session.getUserId());
        if(userMap == null)
            throw new BWFLAException("environment for user " +  session.getUserId() + " not found");

        userMap.remove(session.objectId);

        environmentsAdapter.delete(session.getArchive(), session.envId, true, true);
        db.deleteDoc(emilDbCollectionName, session.envId, session.getIdDBkey());
        if (session.getParentEnvId() != null) {
            EmilSessionEnvironment p = userMap.values()
                    .stream()
                    .filter(e -> e.envId.equals(session.getParentEnvId()))
                    .findFirst()
                    .orElse(null);
            if( p != null )
            {
                delete(p);
            }
        }
    }

    public static String getUserName(String jwtString) throws BWFLAException {
        try {
            Algorithm algorithm = Algorithm.HMAC256("EMiL2018");
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(jwtString);
            return jwt.getSubject();
        } catch (JWTVerificationException |UnsupportedEncodingException exception){
            throw new BWFLAException(exception);
        }
    }

    class Monitor implements Runnable {
        long retention;

        public Monitor(long retention) {
            this.retention = retention;
        }

        @Override
        public void run() {
            try {
                long now = (new Date().getTime());
                List<EmilSessionEnvironment> sessions = toList();
                for (EmilSessionEnvironment session : sessions) {
                    if (now - session.getLastAccess() > retention * 1000 * 60 * 60) // hours (DNB)
                    {
                        System.out.println("deleting session: " + session.getLastAccess() + " now: " + now);
                        delete(session);
                    }
                }
            } catch (BWFLAException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
