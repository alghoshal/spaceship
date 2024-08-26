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
public class AutoplayTest {
	private static final Logger log = LoggerFactory.getLogger(AutoplayTest.class);

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

	// @Test
	public void testStartNewGameAndSetUpAutoPlayScenario3And5() throws InterruptedException {
		String selfId = "player-1";
		int selfPort = MockSpaceshipApplication.USER_INSTANCE_PORT;
		String hostName = "localhost";
		String rule = "desperation";
		String otherInstanceId = "gamelabs-1";
		int otherPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_PORT;

		int SLEEP_TIME_FIRST = 10000;
		int SLEEP_TIME_SECOND = 20000;
		int loopCount = 50;
		int MAX_SLEEP_RETRIES = 4;

		for (int i = 0; i < loopCount; i++) {
			log.debug("---- Run no: " + i);
			// 1: Create new game
			String createGame = "{\"user_id\": \"" + otherInstanceId
					+ "\",\"full_name\": \"gameLabs Opponent\",\"rules\": \"" + rule
					+ "\",\"spaceship_protocol\": {\"hostname\": \"" + hostName + "\",\"port\": " + otherPort + "}}";
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

			int maxSleepTries = MAX_SLEEP_RETRIES, currentSleepCount = 0;
			// Sleep more
			while (currentSleepCount++ < maxSleepTries) {
				log.debug("More sleep: " + currentSleepCount);
				String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
				log.debug("View game status endpoint: " + viewStatusGameEndpointSelf);
				ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf,
						HttpMethod.GET, null, GameView.class);
				GameView gameViewSelf = gameResponseEntitySelf.getBody();

				if (StringUtils.isEmpty(gameViewSelf.getGameMetaInfo().getWon())) {
					// Sleep more
					Thread.sleep(SLEEP_TIME_SECOND);
				} else
					break;
			}

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
			assertEquals(rule, gameViewSelf.getGameMetaInfo().getRules());
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
			assertEquals(rule, gameViewOpponent.getGameMetaInfo().getRules());
			String wonOpponent = gameViewOpponent.getGameMetaInfo().getWon();
			log.debug("Final state of game on self: " + gameViewOpponent);
			assertTrue("Invalid won by :" + wonOpponent,
					otherInstanceId.equals(wonOpponent) || selfId.equals(wonOpponent));

			assertEquals(wonSelf, wonOpponent);
		}
	}

	@Test
	public void testPing() {
		ResponseEntity<String> pingResponseEntity = restTemplate.exchange(
				"http://localhost:" + MockSpaceshipApplication.USER_INSTANCE_PORT + "/ping", HttpMethod.GET, null,
				String.class);
		assertEquals("pong",pingResponseEntity.getBody());
	}
}
