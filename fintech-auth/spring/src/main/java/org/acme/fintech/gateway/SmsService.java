package org.acme.fintech.gateway;

import org.jboss.logging.Logger;

public class SmsService implements MessageService {
    private static final Logger logger = Logger.getLogger(SmsService.class);

    public void send(String target, String message) {
        logger.warn(String.format("***** SIMULATION MODE ***** Would send SMS to %s with text: %s", target, message));
    }
}
