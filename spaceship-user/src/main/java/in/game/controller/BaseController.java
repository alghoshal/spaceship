package in.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.service.GameAutoPlayService;
import in.game.service.GamePlayClientService;
import in.game.service.helper.ServiceLocatorHelper;

/**
 * Base controller
 * 
 * @author aghoshal
 */
@Controller
public class BaseController {
	public static final String SALVO_FIRE_KEY = "salvo";
	public static final String HYPHEN = "-";
	public static final String COLON = ":";

	@Autowired
	ServiceLocatorHelper serviceLocatorHelper;

	@Autowired
	ControllerHelper controllerHelper;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ObjectMapper jsonObjectMapper;
	
	@Autowired
	GameEngine gameEngine;
	
	@Autowired
	GamePlayClientService gamePlayClientService;
	
	@Autowired
	GameAutoPlayService autoPlayService; 
	
	@Value("${spring.application.name}")
	String applicationName;

	@Value("${spring.cloud.client.hostname}")
	String hostName;

	@Value("${server.port}")
	String port;

	@Value("${spring.application.instance_id}")
	String instanceId;

	@Value("${spaceship.game.name}")
	String spaceshipGameName;

	@Value("${spaceship.game.endpoint.fire.salvo}")
	String spaceshipGameEndpointFireSalvo;

	@Value("${spaceship.game.endpoint.new.game}")
	String spaceshipGameEndpointNewGame;

	@Value("${spaceship.user.full_name}")
	String userFullName;

	@Value("${spaceship.protocol.new.game}")
	String spaceshipProtocolEndpointNewGame;
	
	@Value("${spaceship.game.endpoint.fetch.game}")
	String spaceshipProtocolEndpointFetchGame;
	
	@Value("${spaceship.protocol.handle.salvo.fire}")
	String spaceshipProtocolEndpointHandleSalvoFire;
	
}
