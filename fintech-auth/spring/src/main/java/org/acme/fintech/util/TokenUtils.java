package org.acme.fintech.util;

import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.model.OtpToken;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TokenUtils {
    public static void validateOtpToken(OtpToken otpToken, String otpCode) {
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
}
