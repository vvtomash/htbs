package org.acme.fintech.gateway;

import org.jboss.logging.Logger;

public class FcmService implements MessageService {
    private static final Logger logger = Logger.getLogger(FcmService.class);

    @Override
    public void send(String target, String message) {
        logger.warn(String.format("***** SIMULATION MODE ***** Would send PUSH to %s with text: %s", target, message));
    }
}
