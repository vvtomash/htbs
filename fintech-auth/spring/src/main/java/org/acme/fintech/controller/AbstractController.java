package org.acme.fintech.controller;

import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.Credential;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.util.PasswordUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class AbstractController {

    protected void validateOtpToken(OtpToken otpToken, String otpCode) {
        if (otpToken == null) {
            throw new TokenValidationException("No OTP Token present");
        }
        LocalDateTime nowDateTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiryDateTime = otpToken.getExpiryDateTime();
        if (nowDateTime.isAfter(expiryDateTime)) {
            throw new TokenValidationException("OTP Token expired");
        }
        if (!otpToken.getCode().equals(otpCode)) {
            throw new TokenValidationException("OTP codes mismatch");
        }
    }

    protected Credential newCredential(Client client, String password) {
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

