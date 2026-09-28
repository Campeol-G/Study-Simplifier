package com.studys.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "studys.auth")
public class JwtProperties {

    private String jwtSecret = "dev-only-secret-com-no-minimo-32-caracteres";
    private long jwtExpirationMs = 86400000;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setJwtExpirationMs(long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }
}
