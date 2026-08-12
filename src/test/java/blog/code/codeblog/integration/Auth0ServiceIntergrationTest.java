package blog.code.codeblog.integration;

import blog.code.codeblog.dto.authentication.SendOTPRequestDTO;
import blog.code.codeblog.execptions.TooManyRequests;
import blog.code.codeblog.service.integration.Auth0ServiceIntergration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Auth0ServiceIntergrationTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private Auth0ServiceIntergration auth0ServiceIntergration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should throw TooManyRequests when Redis rate-limit key already exists")
    void sendOTPThrowsTooManyRequestsWhenRateLimited() {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(
                anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        SendOTPRequestDTO dto = new SendOTPRequestDTO("test@example.com");

        assertThrows(TooManyRequests.class, () -> auth0ServiceIntergration.sendOTP(dto));
    }
}