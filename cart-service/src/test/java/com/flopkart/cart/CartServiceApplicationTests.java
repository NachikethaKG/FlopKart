package com.flopkart.cart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "management.health.redis.enabled=false"
})
class CartServiceApplicationTests {

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }
}
