package com.notetaking.summarizer.config;

import com.notetaking.summarizer.entity.UserEntity;
import com.notetaking.summarizer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if demo user exists
        if (userRepository.findByUsername("demo").isEmpty()) {
            log.info("Creating demo user...");

            UserEntity demoUser = UserEntity.builder()
                    .username("demo")
                    .passwordHash(passwordEncoder.encode("demo"))
                    .role("ROLE_USER")
                    .enabled(true)
                    .build();

            userRepository.save(demoUser);
            log.info("Demo user created successfully with username: demo, password: demo");
        } else {
            log.info("Demo user already exists");
        }

        // Create admin user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Creating admin user...");

            UserEntity adminUser = UserEntity.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin"))
                    .role("ROLE_ADMIN")
                    .enabled(true)
                    .build();

            userRepository.save(adminUser);
            log.info("Admin user created successfully with username: admin, password: admin");
        } else {
            log.info("Admin user already exists");
        }
    }
}

