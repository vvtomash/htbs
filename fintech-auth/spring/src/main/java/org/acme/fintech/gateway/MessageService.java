package org.acme.fintech.gateway;

public interface MessageService {
    void send(String target, String message);
}
