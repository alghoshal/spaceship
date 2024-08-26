package in.game.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class MockSpaceshipApplication {
	private static final Logger log = LoggerFactory.getLogger(MockSpaceshipApplication.class);
	
	public static final String USER_INSTANCE_HOSTNAME = "localhost";
	public static final int USER_INSTANCE_PORT = 9315;
	public static final int USER_INSTANCE_MANAGEMENT_PORT = 9316;
	public static final String USER_INSTANCE_ID = "selfId";
	
	public static final int OTHER_USER_INSTANCE_PORT = 9212;
	public static final int OTHER_USER_INSTANCE_MANAGEMENT_PORT = 9213;
	public static final String OTHER_USER_INSTANCE_ID = "otherID";
	
	
	@Value("${auto.play.thread.pool.maximumPoolSize}")
	int autoPlayThreadPoolTaskExecutorMaxPoolSize;

	@Value("${auto.play.thread.pool.corePoolSize}")
	int autoPlayThreadPoolTaskExecutorCorePoolSize;
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate  restTemplate = new TestRestTemplate();
		return restTemplate;
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
	
	@Bean
	@Primary
	public DiscoveryClient discoveryClient(){
		//TODO: Mockito!
		return new DiscoveryClient() {
			
			public List<String> getServices() {
				return null;
			}
			
			public ServiceInstance getLocalServiceInstance() {
				return null;
			}
			
			public List<ServiceInstance> getInstances(String arg0) {
				ServiceInstance otherUserInstance = new ServiceInstance() {
					
					public boolean isSecure() {
						return false;
					}
					
					public URI getUri() {
						try {
							return new URI("http://"+USER_INSTANCE_HOSTNAME+":"+OTHER_USER_INSTANCE_PORT);
						} catch (URISyntaxException e) {
							log.error(e.getMessage());
						}
						return null;
					}
					
					public String getServiceId() {
						return null;
					}
					
					public int getPort() {
						return OTHER_USER_INSTANCE_PORT;
					}
					
					public Map<String, String> getMetadata() {
						return null;
					}
					
					public String getHost() {
						return USER_INSTANCE_HOSTNAME;
					}
				};
				
				ServiceInstance thisInstance = new ServiceInstance() {
					
					public boolean isSecure() {
						return false;
					}
					
					public URI getUri() {
						try {
							return new URI("http://"+USER_INSTANCE_HOSTNAME+":"+USER_INSTANCE_PORT);
						} catch (URISyntaxException e) {
							log.error(e.getMessage());
						}
						return null;
					}
					
					public String getServiceId() {
						return null;
					}
					
					public int getPort() {
						return USER_INSTANCE_PORT;
					}
					
					public Map<String, String> getMetadata() {
						return null;
					}
					
					public String getHost() {
						return USER_INSTANCE_HOSTNAME;
					}
				};
				
				return Arrays.asList(otherUserInstance,thisInstance);
			}
			
			public String description() {
				return "Mock Discovery Client";
			}
		};
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.debug("Starting user application");
		SpringApplication.run(MockSpaceshipApplication.class, args);
	}
}
