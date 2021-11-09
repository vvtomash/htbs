package org.acme.fintech.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.acme.fintech.exception.TokenValidationException;
import org.acme.fintech.model.Client;
import org.acme.fintech.model.OtpToken;
import org.acme.fintech.response.JwtAuthenticationToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

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

    public static JwtAuthenticationToken newJWSToken(Client client) {
        // TODO Move to Key Management System (KMS)
        Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String accessToken = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(client.getPhone())
                .setIssuedAt(new Date())
                .signWith(secretKey)
                .compact();
        JwtAuthenticationToken response = new JwtAuthenticationToken();
        response.setAccessToken(accessToken);
        return response;
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey)keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();

        String publicKey64 = Encoders.BASE64.encode(publicKey.getEncoded());
        System.out.println("original publicKey base64: " + publicKey64);

        String sha256Hex = DigestUtils.sha256Hex(publicKey.getEncoded());
        System.out.println("original publicKey finger: " + sha256Hex);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey restoredPublicKey = keyFactory.generatePublic(publicKeySpec);

        publicKey64 = Encoders.BASE64.encode(restoredPublicKey.getEncoded());
        System.out.println("restored publicKey base64: " + publicKey64);

        sha256Hex = DigestUtils.sha256Hex(restoredPublicKey.getEncoded());
        System.out.println("restored publicKey finger: " + sha256Hex);


        /*
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String jwtToken = Jwts.builder()
                .setSubject("Joe")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setHeaderParam("kid", "myKeyId")
                .signWith(privateKey)
                .compact();
        System.out.println("jwtToken = " + jwtToken);

        Object body = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
        System.out.println("body = " + body);
         */
    }
}
