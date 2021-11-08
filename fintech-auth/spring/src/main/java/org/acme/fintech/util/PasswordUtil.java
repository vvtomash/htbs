package org.acme.fintech.util;

import org.acme.fintech.exception.PasswordValidationException;
import org.acme.fintech.model.Credential;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PasswordUtil {
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_DERIVED_KEY_SIZE = 512;
    private static final int DEFAULT_ITERATIONS = 27500;
    private static final int DEFAULT_SALT_SIZE = 16;

    public static void validatePassword(Credential credential, String rawPassword) {
        if (credential == null) {
            throw new PasswordValidationException();
        }

        // Encode given password using restored salt
        byte[] saltBytes = PasswordUtil.decodeBase64(credential.getSalt());
        String encodedPassword = PasswordUtil.encodePassword(rawPassword, saltBytes);

        // Verify credential
        if (!credential.getPassword().equals(encodedPassword)) {
            throw new PasswordValidationException();
        }
    }

    public static String encodePassword(String rawPassword, byte[] salt) {
        KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt,
                DEFAULT_ITERATIONS, DEFAULT_DERIVED_KEY_SIZE);

        try {
            byte[] key = getSecretKeyFactory().generateSecret(spec).getEncoded();
            return Base64.encodeBase64String(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] randomSalt() {
        byte[] buffer = new byte[DEFAULT_SALT_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(buffer);
        return buffer;
    }

    public static String encodeBase64(byte[] value) {
        return Base64.encodeBase64String(value);
    }

    public static byte[] decodeBase64(String value) {
        return Base64.decodeBase64(value);
    }

    private static SecretKeyFactory getSecretKeyFactory() {
        try {
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("PBKDF2 algorithm not found", e);
        }
    }
}
