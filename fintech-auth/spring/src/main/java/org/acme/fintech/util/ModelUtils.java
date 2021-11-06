package org.acme.fintech.util;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ModelUtils {
    public static Credential newCredential(Client client, String password) {
        byte[] saltBytes = PasswordUtil.randomSalt();
        String saltString = PasswordUtil.encodeBase64(saltBytes);
        String encodedPassword = PasswordUtil.encodePassword(password, saltBytes);

        Credential credential = new Credential();
        credential.setCreateDateTime(LocalDateTime.now(ZoneOffset.UTC));
        credential.setPassword(encodedPassword);
        credential.setClient(client);
        credential.setSalt(saltString);
        return credential;
    }
}
