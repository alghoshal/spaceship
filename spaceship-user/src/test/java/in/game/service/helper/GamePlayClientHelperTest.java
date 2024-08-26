package in.game.service.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import in.game.controller.MockSpaceshipApplication;
import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.game.helpers.SpaceshipGameBuilder;
import in.game.game.persistence.PersistenceService;
import in.game.model.SpaceshipGame;
import in.game.service.GamePlayClientImpl;
import in.game.service.GamePlayClientService;
import in.game.service.helper.GamePlayClientHelper;
import in.game.service.helper.ServiceLocatorHelper;
import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MockSpaceshipApplication.class })
@WebIntegrationTest({ "server.port:"+MockSpaceshipApplication.USER_INSTANCE_PORT, 
	"management.port:"+ MockSpaceshipApplication.USER_INSTANCE_MANAGEMENT_PORT,
	"spring.application.instance_id:"+MockSpaceshipApplication.USER_INSTANCE_ID})
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class GamePlayClientHelperTest {
	private static final Logger log = LoggerFactory.getLogger(GamePlayClientHelperTest.class);

	@Autowired
	ServiceLocatorHelper serviceLocatorHelper;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ControllerHelper controllerHelper;

	@Autowired
	GameEngine gameEngine;
	
	@Autowired
	GamePlayClientHelper gamePlayClientHelper;

	@Autowired
	SpaceshipGameBuilder gameBuilder;
	
	@Autowired
	PersistenceService<SpaceshipGame> persistenceService;
	
	@Autowired
	GamePlayClientService gamePlayClientService;

	@Value("${local.server.port}")
	int port;

	@Value("${spring.application.instance_id}")
	String instanceId;

	@Test
	public void testSalvoFireOnOpponentInvalidGameScenario3() {

		String gameId = "23";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<SalvoImpact> salvoResponseEntity = gamePlayClientService.fireSalvoOnOpponent(gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testSalvoFireOnOpponentInActiveGameScenario3() {
		String gameId = "23";

		SpaceshipGame game = new SpaceshipGame();
		game.setActive(false);
		this.persistenceService.save(gameId, game);

		ResponseEntity<SalvoImpact> salvoResponseEntity = gamePlayClientService.fireSalvoOnOpponent(gameId,new String[]{"0x0","AxA"});
		log.debug(salvoResponseEntity.toString());
		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));
	}
	
	@Test
	public void testStartNewGameWithRulesScenario7() {
		String createGame = "{\"user_id\": \"gamelabs-1\",\"full_name\": \"gameLabs Opponent\",\"rules\": \"6-shot\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint,
				HttpMethod.POST, createGameRequest, GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));
		
		assertEquals(instanceId,newGameResponse.getUserId());
		assertNotNull(newGameResponse.getStarting());
	}
	
	@Test
	public void testHaveOpponentFireBackFromSelfToSelf(){
		String createGame = "{\"user_id\": \""+instanceId
				+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \"6-shot\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\":"
				+ port+"}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint,
				HttpMethod.POST, createGameRequest, GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));
		
		assertEquals(instanceId,newGameResponse.getUserId());
		assertNotNull(newGameResponse.getStarting());
		
		ResponseEntity<SalvoImpact> fireBackSalvoResponseEntity =
				this.gamePlayClientService.haveOpponentFireBackOnSelf(newGameResponse.getGameId());
		
		assertTrue("Response Code Error: " + fireBackSalvoResponseEntity.getStatusCode(),
				HttpStatus.BAD_REQUEST.equals(fireBackSalvoResponseEntity.getStatusCode()));
	}
	
	@Test
	public void testSalvoImpact(){
		String errorMessage  = "Something wrong";
		GamePlayClientImpl gamePlayClientImpl = (GamePlayClientImpl) gamePlayClientService;
		SalvoImpact salvoImpact = gamePlayClientHelper.prepareSalvoImpactInError(errorMessage);
		assertNotNull(salvoImpact);
		assertEquals(errorMessage, salvoImpact.getErrorMessage());
		assertTrue(salvoImpact.isErrorFlag());
	}

}
