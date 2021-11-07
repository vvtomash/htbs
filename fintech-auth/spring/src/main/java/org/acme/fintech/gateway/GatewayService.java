package org.acme.fintech.gateway;

public interface GatewayService {
    void send(String target, String message);
}
