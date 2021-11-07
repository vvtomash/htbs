package org.acme.fintech.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.model.OtpToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TokenUtils {

    public static void validateOtpToken(OtpToken optToken, String code) {
        if (optToken == null) {
            throw new TokenValidationException("No OTP Token present");
        } else if (StringUtils.isEmpty(optToken.getCode())) {
            throw new TokenValidationException("No OTP Code present");
        } else if (optToken.getExpTime() == null) {
            throw new TokenValidationException("No OTP Exp time present");
        }

        LocalDateTime nowDateTime = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime expiryDateTime = optToken.getExpTime();
        if (nowDateTime.isAfter(expiryDateTime)) {
            throw new TokenValidationException("OTP Token expired");
        }
        if (!optToken.getCode().equals(code)) {
            throw new TokenValidationException("OTP Codes mismatch");
        }
    }

    public static String newJWSToken() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return newJWSToken(key);
    }

    public static String newJWSToken(Key key) {
        return Jwts.builder().setSubject("Joe").signWith(key).compact();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256); //or RS384, RS512, PS256, PS384, PS512, ES256, ES384, ES512
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String publicKey64 = Encoders.BASE64.encode(publicKey.getEncoded());
        System.out.println("original publicKey base64: " + publicKey64);

        String sha256Hex = DigestUtils.sha256Hex(publicKey.getEncoded());
        System.out.println("original publicKey finger: " + sha256Hex);

        RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey)privateKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey restoredPublicKey = keyFactory.generatePublic(publicKeySpec);

        publicKey64 = Encoders.BASE64.encode(restoredPublicKey.getEncoded());
        System.out.println("restored publicKey base64: " + publicKey64);

        sha256Hex = DigestUtils.sha256Hex(restoredPublicKey.getEncoded());
        System.out.println("restored publicKey finger: " + sha256Hex);

        //        Key key3 = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        String token = Jwts.builder().setSubject("Joe").signWith(key3).compact();
//        System.out.println("token = " + token);
    }
}
