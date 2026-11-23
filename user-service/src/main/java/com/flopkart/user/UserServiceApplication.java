package com.flopkart.user;

import com.flopkart.user.entity.User;
import com.flopkart.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedManagerUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String managerEmail = "manager@flopkart.com";
            userRepository.findByEmail(managerEmail).ifPresentOrElse(
                    user -> {
                        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
                            user.setRole("MANAGER");
                            userRepository.save(user);
                        }
                    },
                    () -> {
                        User manager = User.builder()
                                .name("Store Manager")
                                .email(managerEmail)
                                .password(passwordEncoder.encode("Manager@123"))
                                .role("MANAGER")
                                .build();
                        userRepository.save(manager);
                    }
            );
        };
    }
}
