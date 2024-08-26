package in.game.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
@EnableEurekaServer
public class RegistryService {
	
	public static void main(String[] args) {
		SpringApplication.run(RegistryService.class, args);
	}

}
