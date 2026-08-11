package blog.code.codeblog.service.integration;

import blog.code.codeblog.execptions.TooManyRequests;
import blog.code.codeblog.dto.authentication.SendOTPRequestDTO;
import blog.code.codeblog.dto.authentication.VerifyOTPrequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class Auth0ServiceIntergration {

    private final RestClient http = RestClient.create();



    @Value("${auth0.issuer-uri}")
    private String issuer;

    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.client.id}")
    private String clientId;

    @Value("${auth0.client.secret}")
    private String clientSecret;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void sendOTP(SendOTPRequestDTO dto) {

        Boolean first = redisTemplate.opsForValue()
                .setIfAbsent("pwdless:" + dto.email(), String.valueOf(true),60, TimeUnit.SECONDS);

      if (Boolean.FALSE.equals(first)){
          throw new TooManyRequests("Too many OTP requests. Please wait 60 seconds before trying again.");
      }

        try {
            http.post()
                    .uri(issuer + "passwordless/start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "connection", "email",
                            "email", dto.email(),
                            "send", "code"
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("[passwordless] OTP sent for {}", dto.email());
        } catch (RestClientResponseException e) {
            log.error("[passwordless] Failed to request OTP for {}: status={} body={}",
                    dto.email(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadCredentialsException("Failed to request OTP");
        }
    }


    public boolean verifyOTP(VerifyOTPrequestDTO dto) {
        try {
            http.post()
                    .uri(issuer + "oauth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "grant_type", "http://auth0.com/oauth/grant-type/passwordless/otp",
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "username", dto.email(),
                            "otp", dto.otp(),
                            "realm", "email",
                            "audience", audience,
                            "scope", "openid profile email"
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("[VerifyOTP] Invalid or expired OTP for {}: status={} body={}",
                    dto.email(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadCredentialsException("Invalid or expired OTP");
        }

        log.info("[VerifyOTP] OTP successfully validated for {}", dto.email());

        return true;
    }
}