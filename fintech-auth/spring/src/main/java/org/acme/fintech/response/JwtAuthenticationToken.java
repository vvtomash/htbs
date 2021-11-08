package org.acme.fintech.response;

import lombok.Data;

@Data
public class JwtAuthenticationToken {
    private String accessToken;
    private long expiresIn = -1;
    private String tokenType = "Bearer";
}
