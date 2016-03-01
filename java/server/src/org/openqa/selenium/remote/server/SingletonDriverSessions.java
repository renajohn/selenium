package org.openqa.selenium.remote.server;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.SessionId;

/**
 * Created by olivier.crameri on 2/26/16.
 */
public class SingletonDriverSessions extends DefaultDriverSessions {

    private volatile SessionId uniqueSessionId = null;

    public SessionId newSession(Capabilities desiredCapabilities) throws Exception {
        synchronized (this) {
            if (uniqueSessionId == null)
                uniqueSessionId = super.newSession(desiredCapabilities);
        }
        return uniqueSessionId;
    }
    public void deleteSession(SessionId sessionId) {
        if (uniqueSessionId != null && uniqueSessionId.equals(sessionId))
            uniqueSessionId = null;
        super.deleteSession(sessionId);
    }
}
