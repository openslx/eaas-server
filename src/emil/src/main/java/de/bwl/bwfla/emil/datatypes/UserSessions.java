package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserSessions {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, EmilSessionEnvironment>> sessionCache;
    Logger LOG = Logger.getLogger(UserSessions.class.getName());
    public UserSessions(List<EmilSessionEnvironment> envs)  {
        sessionCache = new ConcurrentHashMap<>();

        for(EmilSessionEnvironment session : envs)
        {
            try {
                add(session);
            }
            catch (BWFLAException e)
            {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
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

    public void remove(EmilSessionEnvironment env) throws BWFLAException {
        if(env == null)
            return;

        ConcurrentHashMap<String, EmilSessionEnvironment> userMap = sessionCache.get(env.getUserId());
        if(userMap == null)
            throw new BWFLAException("environment for user " +  env.getUserId() + " not found");

        userMap.remove(env.objectId);
    }
}
