package htbs.keycloak.extension.gateway;

import java.util.Map;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public interface SmsService {

	void send(String phoneNumber, String message);

}
