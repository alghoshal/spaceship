package in.game.service.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class ServiceLocatorHelper {
	private static final Logger log = LoggerFactory.getLogger(ServiceLocatorHelper.class);

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${spring.cloud.client.hostname}")
	private String hostName;

	@Value("${server.port}")
	private String port;

	@Value("${spring.application.instance_id}")
	private String instanceId;

	@Value("${spaceship.game.name}")
	private String spaceshipGameName;

	@Value("${spaceship.game.endpoint.fire.salvo}")
	private String spaceshipGameEndpointFireSalvo;
	
	@Value("${spaceship.game.endpoint.new.game}")
	private String spaceshipGameEndpointNewGame;
	
	@Value("${spaceship.user.full_name}")
	private String userFullName;
	
	@Autowired
	DiscoveryClient discoveryClient;
	
	String localIp = "127.0.0.1";
	
	/**
	 * Locates the appropriate game instance for this gameId 
	 * TODO: Other
	 * strategies - DHP, Lifo, Load-balance, etc.
	 * @param gameId
	 * @serviceRegistryName		
	 * @return
	 */
	public ServiceInstance locateServiceInstance(String gameId) {
		return locateFirstServiceInstance(spaceshipGameName);
	}
	
	/**
	 * Locates the first instance of the service from registry
	 * @param serviceRegistryName	name of the service
	 * @return
	 */
	public ServiceInstance locateFirstServiceInstance(String serviceRegistryName) {
		List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceRegistryName);
		log.debug(serviceRegistryName + ", located services: " + serviceInstances);
		ServiceInstance service = !serviceInstances.isEmpty() ? serviceInstances.get(0) : null;
		log.debug("First service: " + service.getUri());
		return service;
	}
	
	/**
	 * true if the service is up on host:port
	 * @param serviceRegistryName
	 * @param hostName
	 * @param port
	 * @return
	 */
	public boolean isServiceUp(String serviceRegistryName, String hostName, String port){
		log.debug("Checking service availability for: "+serviceRegistryName+", on: "+hostName+":"+port);
		List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceRegistryName);
		for (ServiceInstance serviceInstance : serviceInstances) {
			if((hostName.equals(serviceInstance.getHost())||
					(isLocalHost(hostName)&&isLocalHost(serviceInstance.getHost()))) 
					&& port.equals(serviceInstance.getPort()+"")) return true;
		}
		return false;
	}

	boolean isLocalHost(String host) {
		return "localhost".equals(host) || localIp.equals(host);
	}

}
