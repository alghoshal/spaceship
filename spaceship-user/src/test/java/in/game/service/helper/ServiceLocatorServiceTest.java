package in.game.service.helper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import in.game.service.helper.ServiceLocatorHelper;

public class ServiceLocatorServiceTest {

	@Test
	public void testIsServiceUp(){
		final String serviceRegistryName = "abcdef"; 
		final String hostName = "localhost"; 
		final int port=3131;
		
		ServiceLocatorHelper helper = new ServiceLocatorHelper();
		helper.discoveryClient = new DiscoveryClient() {
			
			public List<String> getServices() {
				return null;
			}
			
			public ServiceInstance getLocalServiceInstance() {
				return null;
			}
			
			public List<ServiceInstance> getInstances(String arg0) {
				List<ServiceInstance> services = new ArrayList<ServiceInstance>();
				ServiceInstance service = new ServiceInstance() {
					
					public boolean isSecure() {
						return false;
					}
					
					public URI getUri() {
						return null;
					}
					
					public String getServiceId() {
						return serviceRegistryName;
					}
					
					public int getPort() {
						return port;
					}
					
					public Map<String, String> getMetadata() {
						return null;
					}
					
					public String getHost() {
						return hostName;
					}
				};
				services.add(service);
				return services;
			}
			
			public String description() {
				return "Missing";
			}
		};
		
		assertTrue(helper.isServiceUp(serviceRegistryName, hostName, port+""));
		assertTrue(helper.isServiceUp(serviceRegistryName, "127.0.0.1", port+""));
		assertFalse(helper.isServiceUp(serviceRegistryName, "127.0.0.2", port+""));
		assertFalse(helper.isServiceUp(serviceRegistryName, hostName, "3132"));
	}
	
	@Test
	public void testIsLocalHost(){
		ServiceLocatorHelper helper = new ServiceLocatorHelper();
		assertTrue(helper.isLocalHost("localhost"));
		assertTrue(helper.isLocalHost("127.0.0.1"));
		assertFalse(helper.isLocalHost("localhoste"));
		assertFalse(helper.isLocalHost("127.0.0.2"));
	}
}
