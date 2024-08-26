package in.game.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import in.game.factory.SpaceshipFactory;
import in.game.factory.SpaceshipFactoryImpl;
import in.game.game.GameEngine;
import in.game.game.GameEngineImpl;
import in.game.game.helpers.SpaceshipGameBuilder;
import in.game.game.persistence.InMemoryCache;
import in.game.game.persistence.PersistenceService;
import in.game.model.SpaceshipGame;

/**
 * @author aghoshal
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpaceshipCoreApplication {

	@Autowired
    private DiscoveryClient discoveryClient;
	
	@Bean
	public GameEngine gameEngine(){
		return new GameEngineImpl();
	}
	
	@Bean
	public SpaceshipGameBuilder gameBuilder(){
		return new SpaceshipGameBuilder();
	}
	
	@Bean
	public SpaceshipFactory spaceshipFactory(){
		return new SpaceshipFactoryImpl();
	}
	
	@Bean
	public PersistenceService<SpaceshipGame> persistenceService(){
		return new InMemoryCache<SpaceshipGame>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleCommandLinePropertySource ps = new SimpleCommandLinePropertySource(args);
		print(ps);
		SpringApplication.run(SpaceshipCoreApplication.class, args);
	}

	static void print(SimpleCommandLinePropertySource ps) {
		System.out.println("Printing args......"+ps+" : "+ps.getPropertyNames().length);
		for (String s : ps.getPropertyNames()) {
			System.out.println(s);
		}
		
	}
}
