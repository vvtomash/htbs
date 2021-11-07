package org.acme.fintech.util;

import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.OtpToken;

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

    public static OtpToken newOtpToken() {
        String code = RandomString.randomCode(6).toUpperCase();
        LocalDateTime expTime = LocalDateTime.now(ZoneOffset.UTC)
                .plusMinutes(5); // TODO Configuration

        OtpToken token = new OtpToken();
        token.setCode(code);
        token.setExpTime(expTime);

        return token;
    }
}
