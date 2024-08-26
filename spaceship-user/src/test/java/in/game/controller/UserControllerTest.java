package in.game.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.util.Random;

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
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.game.helpers.SpaceshipGameBuilder;
import in.game.game.persistence.PersistenceService;
import in.game.model.SpaceshipGame;
import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;
import in.game.model.User;
import in.game.model.view.GameView;
import in.game.service.GamePlayClientService;
import in.game.service.helper.ServiceLocatorHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MockSpaceshipApplication.class })
@WebIntegrationTest({ "server.port:" + MockSpaceshipApplication.USER_INSTANCE_PORT,
		"management.port:" + MockSpaceshipApplication.USER_INSTANCE_MANAGEMENT_PORT,
		"spring.application.instance_id:" + MockSpaceshipApplication.USER_INSTANCE_ID })
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class UserControllerTest {
	private static final Logger log = LoggerFactory.getLogger(UserControllerTest.class);

	@Autowired
	ServiceLocatorHelper serviceLocatorHelper;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ControllerHelper controllerHelper;

	@Autowired
	GameEngine gameEngine;

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
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId + "/fire";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> salvoRequest = new HttpEntity<String>(salvo, headers);
		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				salvoRequest, SalvoImpact.class, gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testSalvoFireOnOpponentInActiveGameScenario3() {
		String gameId = "23";

		SpaceshipGame game = new SpaceshipGame();
		game.setActive(false);
		this.persistenceService.save(gameId, game);

		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId + "/fire";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> salvoRequest = new HttpEntity<String>(salvo, headers);
		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				salvoRequest, SalvoImpact.class, gameId);

		assertTrue("Response Code Error: " + salvoResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(salvoResponseEntity.getStatusCode()));
	}

	@Test
	public void testFetchInvalidGameScenario4() {
		String gameId = "hhshh-232";
		String fetchGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId;
		ResponseEntity<GameView> gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null,
				GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.NOT_FOUND.equals(gameResponseEntity.getStatusCode()));

		GameView game = gameResponseEntity.getBody();
		assertNull(game);
	}

	@Test
	public void testFetchValidGameScenario4() {

		// Create a new game
		String opponentId = "gamelabs-1";
		String createGame = "{\"user_id\": \"" + opponentId
				+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		log.debug("Create game endpoint: " + createGameEndpoint);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				createGameRequest, GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		String gameId = newGameResponse.getGameId();

		// Now fetch details of the game
		String fetchGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId;
		log.debug("Fetch game endpoint: " + fetchGameEndpoint);
		ResponseEntity<GameView> gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null,
				GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(gameResponseEntity.getStatusCode()));

		GameView gameView = gameResponseEntity.getBody();
		log.debug("" + gameView);
		assertEquals(instanceId, gameView.getSelf().getUserId());
		assertEquals(instanceId, gameView.getSelf().getUserId());

	}

	@Test
	public void testFetchInActiveGameScenario4() {

		// Create a new game
		String opponentId = "gamelabs-1";
		String createGame = "{\"user_id\": \"" + opponentId
				+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";
		String won = "acf";

		log.debug("Create game endpoint: " + createGameEndpoint);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				createGameRequest, GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		String gameId = newGameResponse.getGameId();

		// Now fetch details of the game
		String fetchGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId;
		log.debug("Fetch game endpoint: " + fetchGameEndpoint);
		ResponseEntity<GameView> gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null,
				GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(gameResponseEntity.getStatusCode()));

		GameView gameView = gameResponseEntity.getBody();
		log.debug("" + gameView);
		assertEquals(instanceId, gameView.getSelf().getUserId());
		assertEquals(instanceId, gameView.getSelf().getUserId());

		// Set the game as inactive & ensure it is accessible
		SpaceshipGame game = persistenceService.lookUpByKey(gameId);
		game.setActive(false);
		game.setWon(won);
		persistenceService.save(gameId, game);

		gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null, GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(gameResponseEntity.getStatusCode()));

		gameView = gameResponseEntity.getBody();
		log.debug("GameView after setting to inactive" + gameView);
		assertEquals(instanceId, gameView.getSelf().getUserId());
		assertEquals(instanceId, gameView.getSelf().getUserId());
		assertEquals(won, gameView.getGameMetaInfo().getWon());
		assertNull(gameView.getGameMetaInfo().getPlayerTurn());
	}

	@Test
	public void testCreateFetchValidGameWithSpecialCharScenario4() {
		// Create a new game
		String opponentId = "gamelabs-1";
		String createGame = "{\"user_id\": \"" + opponentId
				+ "\",\"full_name\": \"XebiäLabs @pponent With Sp chãr\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9001}}";
		String createGameEndpoint = "http://localhost:" + port + "/spaceship/protocol/game/new";

		log.debug("Create game endpoint: " + createGameEndpoint);
		MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(createGameEndpoint, HttpMethod.POST,
				controllerHelper.prepareRequestBody(createGame, mediaType, String.class), GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		String gameId = newGameResponse.getGameId();

		// Now fetch details of the game
		String fetchGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId;
		log.debug("Fetch game endpoint: " + fetchGameEndpoint);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<GameView> gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null,
				GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(gameResponseEntity.getStatusCode()));

		GameView gameView = gameResponseEntity.getBody();
		log.debug("" + gameView);
		assertEquals(instanceId, gameView.getSelf().getUserId());
		assertEquals(instanceId, gameView.getSelf().getUserId());

		// Validate no changes to game schema
		ResponseEntity<String> newGameResponseStringEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET,
				null, String.class);
		String gameViewString = newGameResponseStringEntity.getBody();
		log.debug(gameViewString);
	}

	@Test
	public void testFetchValidGameBaselineResponseJsonScenario4() {

		// Create a new game
		String fullName = "abc";
		String userId = "123";
		String gameId = "ads-sdsd-bbb12";
		String opponentId = "oppo2323";

		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setFullName("Mr Opponent");
		opponent.setUserId(opponentId);

		SpaceshipGame game = new SpaceshipGame(self, opponent);
		game.setGameId(gameId);
		game = this.persistenceService.save(game.getGameId(), game);

		// Now fetch details of the game
		String fetchGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/" + gameId;
		log.debug("Fetch game endpoint: " + fetchGameEndpoint);
		ResponseEntity<GameView> gameResponseEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET, null,
				GameView.class);

		assertTrue("Response Code Error: " + gameResponseEntity.getStatusCode(),
				HttpStatus.OK.equals(gameResponseEntity.getStatusCode()));

		GameView gameView = gameResponseEntity.getBody();
		log.debug("Game fetched: " + gameView);
		assertEquals(userId, gameView.getSelf().getUserId());
		assertEquals(opponentId, gameView.getOpponent().getUserId());

		// Validate no changes to game schema
		ResponseEntity<String> newGameResponseStringEntity = restTemplate.exchange(fetchGameEndpoint, HttpMethod.GET,
				null, String.class);
		String gameViewString = newGameResponseStringEntity.getBody();
		String messageExpectedAsPerSchema = "{\"self\":{\"userId\":\"123\",\"board\":[\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\"]},\"opponent\":{\"userId\":\"oppo2323\",\"board\":[\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\",\"................\"]},\"game\":{\"player_turn\":\"123\"}}";
		log.debug(messageExpectedAsPerSchema);
		log.debug(gameViewString);

		assertEquals(messageExpectedAsPerSchema, gameViewString);
	}

	@Test
	public void testStartNewGameInvalidLocalhostToIPScenario8() {

		String createGame = "{\"user_id\": \"gamelabs-2\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 9000}}";
		String createNewGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<String> newGameResponseEntity = null;
		try {
			newGameResponseEntity = restTemplate.exchange(createNewGameEndpoint, HttpMethod.POST, createGameRequest,
					String.class);
			assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
					HttpStatus.BAD_REQUEST.equals(newGameResponseEntity.getStatusCode()));
		} catch (HttpClientErrorException hce) {
			assertThat(hce.getMessage(), containsString("400 Bad Request"));
			assertTrue(true);
		}
	}

	@Test
	public void testStartNewGameInvalidUserUnreachableScenario8() {

		String createGame = "{\"user_id\": \"gamelabs-2\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"127.0.0.1\",\"port\": 19001}}";
		String createNewGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<String> newGameResponseEntity = null;
		try {
			newGameResponseEntity = restTemplate.exchange(createNewGameEndpoint, HttpMethod.POST, createGameRequest,
					String.class);
			assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
					HttpStatus.BAD_REQUEST.equals(newGameResponseEntity.getStatusCode()));
		} catch (HttpClientErrorException hce) {
			assertThat(hce.getMessage(), containsString("400 Bad Request"));
			assertTrue(true);
		}
	}

	@Test
	public void testStartNewGameInvalidUserOpponentIsSelfScenario8() {

		String createGame = "{\"user_id\": \"gamelabs-2\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"localhost\",\"port\":"
				+ +port + "}}";
		String createNewGameEndpoint = "http://localhost:" + port + "/spaceship/user/game/new";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> createGameRequest = new HttpEntity<String>(createGame, headers);
		ResponseEntity<String> newGameResponseEntity = null;
		try {
			newGameResponseEntity = restTemplate.exchange(createNewGameEndpoint, HttpMethod.POST, createGameRequest,
					String.class);
			assertTrue("Response Code Error: " + newGameResponseEntity.getStatusCode(),
					HttpStatus.BAD_REQUEST.equals(newGameResponseEntity.getStatusCode()));
		} catch (HttpClientErrorException hce) {
			assertThat(hce.getMessage(), containsString("400 Bad Request"));
			assertTrue(true);
		}
	}

	//@Test
	public void testStartNewGameAndSetUpAutoPlayScenario3And5() throws InterruptedException {
		String selfId = "default";
		int selfPort = MockSpaceshipApplication.USER_INSTANCE_PORT;
		String hostName = "localhost";
		String rule = "desperation";
		String otherInstanceId = "gamelabs-1";
		int otherPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_MANAGEMENT_PORT;

		int SLEEP_TIME_FIRST = 10000;
		int loopCount = 50;

		for (int i = 0; i < loopCount; i++) {
			log.debug("---- Run no: " + i);
			// 1: Create new game
			String createGame = "{\"user_id\": \"" + otherInstanceId
					+ "\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \"" + hostName
					+ "\",\"port\": " + otherPort + "}}";
			String createNewGameEndpoint = "http://localhost:" + selfPort + "/spaceship/user/game/new";

			ResponseEntity<String> newGameResponseEntity = null;
			try {

				newGameResponseEntity = restTemplate.exchange(createNewGameEndpoint, HttpMethod.POST,
						controllerHelper.prepareRequestBody(createGame, MediaType.APPLICATION_JSON, String.class),
						String.class);
			} catch (HttpClientErrorException hce) {
				fail();
				assertThat(hce.getMessage(), containsString("400 Bad Request"));
				assertTrue(true);
			}
			assertTrue("Response Code Error:" + newGameResponseEntity.getStatusCode(),
					HttpStatus.SEE_OTHER.equals(newGameResponseEntity.getStatusCode()));
			String newGameResponseBodyLocation = newGameResponseEntity.getHeaders().getLocation().toString();
			String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");

			// 2: Start autoplay
			log.debug("New game uri: " + newGameResponseBodyLocation);
			String autoPlayGameEndpoint = "http://localhost:" + selfPort + newGameResponseBodyLocation + "/auto";
			log.debug("Setting up autoplay at endpoint: " + autoPlayGameEndpoint);

			ResponseEntity<String> autoPlayResponseEntity = restTemplate.exchange(autoPlayGameEndpoint, HttpMethod.POST,
					null, String.class);

			assertEquals(HttpStatus.OK, autoPlayResponseEntity.getStatusCode());
			String newPlayResponse = autoPlayResponseEntity.getBody();
			assertTrue(StringUtils.isEmpty(newPlayResponse));

			// Allow the autoplay to complete
			Thread.sleep(SLEEP_TIME_FIRST);

			// 3: Get game status to know who won at the end of autoplay
			// 3a: Status from self
			String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
			log.debug("View game status endpoint: " + viewStatusGameEndpointSelf);
			ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf,
					HttpMethod.GET, null, GameView.class);
			GameView gameViewSelf = gameResponseEntitySelf.getBody();
			log.debug("Game view self: " + gameViewSelf.toString());

			assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
			assertNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
			assertNull(gameViewSelf.getGameMetaInfo().getRules());
			String wonSelf = gameViewSelf.getGameMetaInfo().getWon();
			log.debug("Final state of game on self: " + gameViewSelf);
			assertTrue("Invalid won by :" + wonSelf, otherInstanceId.equals(wonSelf) || selfId.equals(wonSelf));

			// 3b): Status from opponent
			String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort + newGameResponseBodyLocation;
			log.debug("View game status endpoint: " + viewStatusGameEndpointOpponent);
			ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent,
					HttpMethod.GET, null, GameView.class);
			GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
			log.debug("Game view opponent: " + gameViewOpponent.toString());

			assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
			assertNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
			assertNull(gameViewOpponent.getGameMetaInfo().getRules());
			String wonOpponent = gameViewOpponent.getGameMetaInfo().getWon();
			log.debug("Final state of game on self: " + gameViewOpponent);
			assertTrue("Invalid won by :" + wonOpponent,
					otherInstanceId.equals(wonOpponent) || selfId.equals(wonOpponent));

			assertEquals(wonSelf, wonOpponent);
		}

	}

	@Test
	public void testPingSeveralTimes() {
		int loopCount = 5;
		int selfPort = port;
		Random r = new Random();
		for (int i = 0; i < loopCount; i++) {
			String pingEndpoint = "http://localhost:" + selfPort + "/ping-2";
			int nextInt = r.nextInt();
			ResponseEntity<String> pingResponseEntity = restTemplate.exchange(pingEndpoint, HttpMethod.PUT,
					controllerHelper.prepareRequestBody(nextInt + "", MediaType.APPLICATION_JSON, String.class),
					String.class);

			String pingResponse = pingResponseEntity.getBody();
			assertTrue("Response Code Error: " + pingResponseEntity.getStatusCode(),
					HttpStatus.OK.equals(pingResponseEntity.getStatusCode()));

			assertEquals("pong" + nextInt,pingResponse);
		}
	}
}
