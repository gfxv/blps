package dev.gfxv.blps;

import dev.gfxv.blps.entity.Role;
import dev.gfxv.blps.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BlpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlpsApplication.class, args);
	}

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.findByName("ROLE_USER").isEmpty()) {
				Role userRole = new Role();
				userRole.setName("ROLE_USER");
				roleRepository.save(userRole);
			}
			if (roleRepository.findByName("ROLE_MODERATOR").isEmpty()) {
				Role modRole = new Role();
				modRole.setName("ROLE_MODERATOR");
				roleRepository.save(modRole);
			}

			if (roleRepository.findByName("ROLE_GLOBAL_MODERATOR").isEmpty()) {
				Role modRole = new Role();
				modRole.setName("ROLE_GLOBAL_MODERATOR");
				roleRepository.save(modRole);
			}
		};
	}
}
