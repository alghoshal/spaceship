package in.game.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import in.game.controller.helper.ControllerHelper;
import in.game.factory.SpaceshipFactory;
import in.game.factory.SpaceshipFactoryImpl;
import in.game.game.GameEngine;
import in.game.game.GameEngineImpl;
import in.game.game.helpers.SpaceshipGameBuilder;
import in.game.game.persistence.InMemoryCache;
import in.game.game.persistence.PersistenceService;
import in.game.model.SpaceshipGame;
import in.game.service.GameAutoPlayService;
import in.game.service.GameAutoPlayServiceImpl;
import in.game.service.GamePlayClientImpl;
import in.game.service.GamePlayClientService;
import in.game.service.helper.GamePlayClientHelper;
import in.game.service.helper.ServiceLocatorHelper;

/**
 * @author aghoshal
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpaceshipApplication {
	private static final Logger log = LoggerFactory.getLogger(SpaceshipApplication.class);

	@Autowired
    DiscoveryClient discoveryClient;
	
	@Value("${auto.play.thread.pool.maximumPoolSize}")
	int autoPlayThreadPoolTaskExecutorMaxPoolSize;

	@Value("${auto.play.thread.pool.corePoolSize}")
	int autoPlayThreadPoolTaskExecutorCorePoolSize;
	
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
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate template = new RestTemplate();
	    return template;
	}
	
	@Bean
	public GamePlayClientService gamePlayClientService(){
		return new GamePlayClientImpl();
	}
	
	@Bean
	public GameAutoPlayService gameAutoPlayService(){
		return new GameAutoPlayServiceImpl();
	}
	
	@Bean
	public ThreadPoolTaskExecutor autoPlayThreadPoolTaskExecutor(){
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(autoPlayThreadPoolTaskExecutorCorePoolSize);
		taskExecutor.setMaxPoolSize(autoPlayThreadPoolTaskExecutorMaxPoolSize);
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		return taskExecutor;	
	}
	
	@Bean
	public ServiceLocatorHelper serviceLocatorHelper(){
		return new ServiceLocatorHelper();
	}
	
	@Bean
	public ControllerHelper controllerHelper(){
		return new ControllerHelper();
	}
	
	@Bean
	public GamePlayClientHelper gamePlayClientHelper(){
		return new GamePlayClientHelper();
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.debug("Staring user application");
		SpringApplication.run(SpaceshipApplication.class, args);
	}
	
}
