package in.game.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

import in.game.controller.helper.ControllerHelper;
import in.game.game.persistence.PersistenceService;
import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;
import in.game.model.SalvoImpactType;
import in.game.model.SpaceshipGame;
import in.game.service.helper.ServiceLocatorHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(MockSpaceshipApplication.class)
@WebIntegrationTest({ "server.port:9415", "management.port:9416",
		"spring.application.instance_id:" + MockSpaceshipApplication.USER_INSTANCE_ID })
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class ProtocolControllerTest {
	private static final Logger log = LoggerFactory.getLogger(ProtocolControllerTest.class);

	ProtocolController run;

	@Autowired
	ServiceLocatorHelper serviceLocatorHelper;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ControllerHelper controllerHelper;

	@Autowired
	PersistenceService<SpaceshipGame> persistenceService;

	@Value("${spring.application.instance_id}")
	String instanceId;

	static String hostName = MockSpaceshipApplication.USER_INSTANCE_HOSTNAME;
	@Value("${server.port}")
	int selfPort; // = MockSpaceshipApplication.USER_INSTANCE_PORT;
	static String selfId = MockSpaceshipApplication.USER_INSTANCE_ID;

	static int otherPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_PORT;
	static int otherManagementPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_MANAGEMENT_PORT;
	static String otherInstanceId = MockSpaceshipApplication.OTHER_USER_INSTANCE_ID;

	@Value("${local.server.port}")
	int port; // = MockSpaceshipApplication.USER_INSTANCE_PORT;

	@Test
	public void testStartNewGameScenario1() {
		String createGame = "{\"user_id\": \"gamelabs-1\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				createGameRequest, GameMetaInfo.class);

		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));
	}

	@Test
	public void testSalvoFromOpponentInvalidGameIdScenario2() {

		String gameId = "23";
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/" + gameId;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> salvoRequest = new HttpEntity<String>(salvo, headers);
		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				salvoRequest, SalvoImpact.class, gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testStartNewGameAndFireSalvoScenario1() {
		// Set-up game
		String createGame = "{\"user_id\": \"" + otherInstanceId
				+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/new";

		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				controllerHelper.prepareRequestBody(createGame, MediaType.APPLICATION_JSON, String.class),
				GameMetaInfo.class);
		GameMetaInfo gameMetaInfo = newGameResponseEntity.getBody();

		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));

		assertEquals(instanceId, gameMetaInfo.getUserId());

		String gameId = gameMetaInfo.getGameId();
		assertFalse(StringUtils.isEmpty(gameId));

		// Override turn to opponent
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		log.debug("Game details: " + game);
		game.setTurnSelf(false);
		this.persistenceService.save(gameId, game);

		// Now fire salvo
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/" + gameId;

		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class), SalvoImpact.class,
				gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(salvoResponseEntity.getStatusCode()));

		// Override turn to self, not allowed to fire
		game = this.persistenceService.lookUpByKey(gameId);
		log.debug("Game details: " + game);
		game.setTurnSelf(true);
		this.persistenceService.save(gameId, game);

		// Now fire salvo
		salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		fireSalvoGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/" + gameId;

		salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class), SalvoImpact.class,
				gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.BAD_REQUEST.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testStartNewGameAndFireWrongNumberOfSalvoScenario1() {
		// Set-up game
		String createGame = "{\"user_id\": \"" + otherInstanceId
				+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/new";

		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				controllerHelper.prepareRequestBody(createGame, MediaType.APPLICATION_JSON, String.class),
				GameMetaInfo.class);
		GameMetaInfo gameMetaInfo = newGameResponseEntity.getBody();

		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));

		assertEquals(instanceId, gameMetaInfo.getUserId());

		String gameId = gameMetaInfo.getGameId();
		assertFalse(StringUtils.isEmpty(gameId));

		// Override turn to opponent
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		log.debug("Game details: " + game);
		game.setTurnSelf(false);
		this.persistenceService.save(gameId, game);

		// Now fire salvo less than allowed number of salvos
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/" + gameId;

		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class), SalvoImpact.class,
				gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.BAD_REQUEST.equals(salvoResponseEntity.getStatusCode()));

		// Fire more than allowed number of salvos
		game = this.persistenceService.lookUpByKey(gameId);
		log.debug("Game details: " + game);
		game.setTurnSelf(true);
		this.persistenceService.save(gameId, game);

		// Now fire salvo
		salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"BxB\", \"7xF\"]}";
		fireSalvoGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/" + gameId;

		salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class), SalvoImpact.class,
				gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.BAD_REQUEST.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testInActiveGameFireSalvoAllSalvosMissScenario1() {
		// Set-up game
		String createGame = "{\"user_id\": \"" + otherInstanceId
				+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9000}}";
		String createGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/new";

		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				controllerHelper.prepareRequestBody(createGame, MediaType.APPLICATION_JSON, String.class),
				GameMetaInfo.class);
		GameMetaInfo gameMetaInfo = newGameResponseEntity.getBody();

		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));

		assertEquals(instanceId, gameMetaInfo.getUserId());

		String gameId = gameMetaInfo.getGameId();
		assertFalse(StringUtils.isEmpty(gameId));

		// Override turn to opponent & set game inactive
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		log.debug("Game details: " + game);
		game.setTurnSelf(false);
		game.setActive(false);
		this.persistenceService.save(gameId, game);

		// Now fire salvo
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + selfPort + "/spaceship/protocol/game/" + gameId;

		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class), SalvoImpact.class,
				gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));

		SalvoImpact salvoImpact = salvoResponseEntity.getBody();
		Map<String, String> salvoHits = salvoImpact.getSalvo();
		assertNotNull(salvoHits);
		assertEquals(5, salvoHits.size()); // 5 fired
		for (String aSalvoImpact : salvoHits.values()) {
			assertEquals(SalvoImpactType.MISS.getImpactName(), aSalvoImpact);
		}

	}

	@Test
	public void testSalvoFromOpponentInvalidMediaTypeScenario2() {

		String gameId = "23";
		String salvo = "<dd><salvo>hi</salvo></dd>";
		String fireSalvoGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/" + gameId;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		HttpEntity<String> salvoRequest = new HttpEntity<String>(salvo, headers);
		ResponseEntity<SalvoImpact> salvoResponseEntity = null;
		try {
			salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT, salvoRequest,
					SalvoImpact.class, gameId);
			assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
					HttpStatus.UNSUPPORTED_MEDIA_TYPE.equals(salvoResponseEntity.getStatusCode()));
			assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
					HttpStatus.UNSUPPORTED_MEDIA_TYPE.equals(salvoResponseEntity.getStatusCode()));
		} catch (Exception hce) {
			assertThat(hce.getMessage(), containsString("Unsupported Media Type"));
			assertTrue(true);
		}
	}

	@Test
	public void testStartNewGameWithRulesScenario7() {
		String createGame = "{\"user_id\": \"gamelabs-1\",\"full_name\": \"gameLabs Opponent\",\"rules\": \"6-shot\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				createGameRequest, GameMetaInfo.class);

		assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
				HttpStatus.CREATED.equals(newGameResponseEntity.getStatusCode()));
	}

	@Test
	public void testPing() {

		String pingEndpoint = "http://localhost:" + port + "/ping";

		ResponseEntity<String> pingResponseEntity = restTemplate.exchange(pingEndpoint, HttpMethod.GET, null,
				String.class);

		String pingResponse = pingResponseEntity.getBody();
		assertTrue("Response Code Error: " + pingResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(pingResponseEntity.getStatusCode()));

		assertEquals(pingResponse, "pong");

	}
}
