package in.game.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.game.persistence.PersistenceService;
import in.game.model.SalvoImpact;
import in.game.model.SpaceshipGame;
import in.game.model.view.GameView;
import in.game.service.GamePlayClientImpl;
import in.game.service.helper.ServiceLocatorHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MockSpaceshipApplication.class })
@WebIntegrationTest({ "server.port:"+MockSpaceshipApplication.USER_INSTANCE_PORT, 
	"management.port:"+ MockSpaceshipApplication.USER_INSTANCE_MANAGEMENT_PORT,
	"spring.application.instance_id:"+MockSpaceshipApplication.USER_INSTANCE_ID})
@TestPropertySource(properties = { "eureka.client.enabled=false" })
public class UserControllerIntegrationTest {
	private static final int MAX_SLEEP_RETRIES = 1;
	private static final int SLEEP_TIME_SECOND = 20000;
	static final int SLEEP_TIME_FIRST = 10000;
	private static final Logger log = LoggerFactory.getLogger(UserControllerIntegrationTest.class);
	static String hostName = MockSpaceshipApplication.USER_INSTANCE_HOSTNAME;
	static int selfPort = MockSpaceshipApplication.USER_INSTANCE_PORT;
	static String selfId = MockSpaceshipApplication.USER_INSTANCE_ID;
	
	static int otherPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_PORT;
	static int otherManagementPort = MockSpaceshipApplication.OTHER_USER_INSTANCE_MANAGEMENT_PORT;
	static String otherInstanceId = MockSpaceshipApplication.OTHER_USER_INSTANCE_ID;

	
	@Autowired
	ServiceLocatorHelper serviceLocatorHelper;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ControllerHelper controllerHelper;

	@Autowired
	GameEngine gameEngine;

	@Autowired
	PersistenceService<SpaceshipGame> persistenceService;

	//@Value("${local.server.port}")
	//int port=-1;

	@Value("${spring.application.instance_id}")
	String instanceId;
	
	@Value("${spaceship.protocol.fire.back.self}")
	String spaceshipProtocolEndpointFireBackOnSelf;

	static Thread secondApplicationThread;
	
	static ThreadPoolTaskExecutor taskExecutor;

	@BeforeClass
	public static void setUp() throws InterruptedException {
		log.debug("SetUp | Staring up 2nd application");
		if(null==taskExecutor){
			log.debug("Setting up taskExecutor");
			taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(2);
			taskExecutor.setMaxPoolSize(10);
			taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
			taskExecutor.initialize();
		}
		if(null==secondApplicationThread){
			secondApplicationThread = startSecondApplicationContext(otherPort, otherManagementPort, otherInstanceId);
			taskExecutor.execute(secondApplicationThread);
			// Allow the other thread 10s to boot-up
			Thread.sleep(SLEEP_TIME_FIRST);
			log.debug("2nd application up");
		}
	}

	@AfterClass
	public static void tearDown() throws InterruptedException {
		log.debug("TearDown | Shutting down 2nd application");
		
		taskExecutor.shutdown();
		// Allow 10s to shut-down
		Thread.sleep(SLEEP_TIME_FIRST/2);
		log.debug("2nd application shut down");

	}

	@Test
	public void testStartNewGameAndFireSalvoScenario3And8() {

		// 1: Create new game
		String createGame = "{\"user_id\": \""+otherInstanceId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + otherPort + "}}";
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

		// 2: Fire salvo on game
		String newGameResponseBodyLocation = newGameResponseEntity.getHeaders().getLocation().toString();
		log.debug("New game uri: " + newGameResponseBodyLocation);
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		
		String salvo = "{\"salvo\": [\"0x0\", \"8x4\", \"DxA\", \"AxA\", \"7xF\"]}";
		String fireSalvoGameEndpoint = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort) 
				+ newGameResponseBodyLocation + "/fire";

		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvo, MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);

		assertEquals(HttpStatus.OK, salvoResponseEntity.getStatusCode());

	}
	
	//@Test
	public void testStartNewGameAndSetUpAutoPlayScenario3And5() throws InterruptedException {
		// 1: Create new game
		String createGame = "{\"user_id\": \""+otherInstanceId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + otherPort + "}}";
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
		log.debug("Setting up autoplay at endpoint: "+autoPlayGameEndpoint);
		
		ResponseEntity<String> autoPlayResponseEntity = restTemplate.exchange(autoPlayGameEndpoint, HttpMethod.POST,
				null,
				String.class);

		assertEquals(HttpStatus.OK, autoPlayResponseEntity.getStatusCode());
		String newPlayResponse = autoPlayResponseEntity.getBody();
		assertTrue(StringUtils.isEmpty(newPlayResponse));
		
		// Allow the autoplay to complete
		Thread.sleep(SLEEP_TIME_FIRST);
		
		int maxSleepTries = MAX_SLEEP_RETRIES, currentSleepCount=0;
		// Sleep more
		while(currentSleepCount++<maxSleepTries){
			SpaceshipGame game = this.gameEngine.fetchGame(gameId);
			if(game.isActive()){
				// Sleep more
				Thread.sleep(SLEEP_TIME_SECOND);
			}
			else break;
		}
		
		// 3: Get game status to know who won at the end of autoplay 
		// 3a: Status from self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getRules());
		String wonSelf = gameViewSelf.getGameMetaInfo().getWon();
		log.debug("Final state of game on self: "+gameViewSelf);
		assertTrue("Invalid won by :"+wonSelf,otherInstanceId.equals(wonSelf)||selfId.equals(wonSelf));

		// 3b): Status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getRules());
		String wonOpponent = gameViewOpponent.getGameMetaInfo().getWon();
		log.debug("Final state of game on self: "+gameViewOpponent);
		assertTrue("Invalid won by :"+wonOpponent,otherInstanceId.equals(wonOpponent)||selfId.equals(wonOpponent));
		
		assertEquals(wonSelf, wonOpponent);
		
	}
	
	//@Test
	public void testStartNewGameAndSetUpAutoPlaySeveralGamesTogetherScenario3And5() throws InterruptedException {
		
		int i, noOfGames =1;
		
		List<String> newGamesUris = new ArrayList<String>();
		for(i=0;i<noOfGames;i++){
			// 1: Create new game
			String createGame = "{\"user_id\": \""+otherInstanceId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
					+ hostName + "\",\"port\": " + otherPort + "}}";
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
			newGamesUris.add(newGameResponseBodyLocation);
		}
		
		for(i=0;i<noOfGames;i++){
			String newGameResponseBodyLocation = newGamesUris.get(i);
			// 2: Start autoplay
			log.debug("New game uri: " + newGameResponseBodyLocation);
			String autoPlayGameEndpoint = "http://localhost:" + selfPort + newGameResponseBodyLocation + "/auto";
			log.debug("Setting up autoplay at endpoint: "+autoPlayGameEndpoint);
			
			ResponseEntity<String> autoPlayResponseEntity = restTemplate.exchange(autoPlayGameEndpoint, HttpMethod.POST,
					null,
					String.class);
	
			assertEquals(HttpStatus.OK, autoPlayResponseEntity.getStatusCode());
			String newPlayResponse = autoPlayResponseEntity.getBody();
			assertTrue(StringUtils.isEmpty(newPlayResponse));
		}
		
		// Ensure all games were set-up
		Collection<SpaceshipGame> allGames = this.gameEngine.fetchAll();
		int gamesSetUpCount=0;
		for (SpaceshipGame game : allGames) {
			if(newGamesUris.contains("/spaceship/user/game/"+game.getGameId())) gamesSetUpCount++;
			else log.debug("Other games: "+game);
		}
		
		assertEquals(noOfGames,gamesSetUpCount);
		
		
		// Allow the autoplay to complete
		Thread.sleep(SLEEP_TIME_FIRST);
		
		
		int maxSleepTries = MAX_SLEEP_RETRIES, currentSleepCount=0;
		// Sleep more
		while(currentSleepCount++<maxSleepTries){
			boolean allAutoplaysOver=true;
			
			for(i=0;i<noOfGames;i++){
				String newGameResponseBodyLocation = newGamesUris.get(i);
				String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
				SpaceshipGame game = this.gameEngine.fetchGame(gameId);
				if(game.isActive()) allAutoplaysOver = false;
			}
			
			if(!allAutoplaysOver) Thread.sleep(SLEEP_TIME_SECOND);
			else break;
		}
		

		for(i=0;i<noOfGames;i++){
			String newGameResponseBodyLocation = newGamesUris.get(i);

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
			assertTrue("Invalid won by :" + wonOpponent, otherInstanceId.equals(wonOpponent) || selfId.equals(wonOpponent));

			assertEquals(wonSelf, wonOpponent);
		}
	}

	@Test
	public void testStartNewGameScenario8() throws InterruptedException {

		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort+ "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		assertTrue(game.isActive());
		assertFalse(game.isAutoplayOn());
		
		// 3: Get game status
		// 3a: Status from self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getRules());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());

		// 3b): Status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getRules());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		
		assertEquals(gameViewSelf.getGameMetaInfo().getPlayerTurn(),gameViewOpponent.getGameMetaInfo().getPlayerTurn());

	}
	
	@Test
	public void testAllowedToFireOnlyOnTurnScenario8() throws InterruptedException {

		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort+ "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		assertTrue(game.isActive());
		assertFalse(game.isAutoplayOn());
		boolean turnSelf = game.isTurnSelf();
		
		// Request fire back from the wrong/out of turn player
		String fireBackOnSelfGameEndpoint = 
				((turnSelf)?game.getOpponent().getSpaceshipProtocol().getUri():game.getSelf().getSpaceshipProtocol().getUri())
				+ spaceshipProtocolEndpointFireBackOnSelf;
				
		log.debug("Fire back salvo game endpoint: " + fireBackOnSelfGameEndpoint);
		ResponseEntity<SalvoImpact> firebackResponseEntity = restTemplate.exchange(fireBackOnSelfGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(GamePlayClientImpl.EMPTY, GamePlayClientImpl.JSON_MEDIA_TYPE_UTF8, String.class), SalvoImpact.class,
				gameId);
		SalvoImpact firebackResponse = firebackResponseEntity.getBody();
		log.debug("Fire back response from opponent: " + firebackResponse);
		
		assertEquals(HttpStatus.BAD_REQUEST, firebackResponseEntity.getStatusCode());
		assertNotNull(firebackResponse);

	}
	
	@Test
	public void testStartNewGameWithStandardRuleWhenPassedExplicitlyScenario7() throws InterruptedException {
		String rule="standard";
		
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \""+rule+"\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		// 2: Get game status self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		// 3: Get game status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, 
				null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		//4. Validate that shots get fired as per the rule
		String fireBackGameEndpointOpponent = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort)
				+"/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		ResponseEntity<SalvoImpact> gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		SalvoImpact salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		assertEquals(5,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
		
		// Auto-play till a ship gets destroyed
		while(true){
			// Fire until a ship is destroyed
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+ "/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
			game = this.gameEngine.fetchGame(gameId);
			if(game.getOpponentOnBoardEntitiesAlive()!=5||game.getSelfOnBoardEntitiesAlive()!=5) break;
		}
		
		boolean fireOneExtraRound = false;
		if(game.isOpponentOnBoardEntityDestroyed()&&game.isTurnSelf()
				||game.isSelfOnBoardEntityDestroyed()&&!game.isTurnSelf()) fireOneExtraRound = true;
	
		// 5: Destroy a ship & then validate that an additional (2) shots get fired back
		fireBackGameEndpointOpponent = "http://localhost:" 
				+ + ((game.isTurnSelf())?selfPort:otherPort)
				+"/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		
		if(fireOneExtraRound){
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+"/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		}
		
		assertEquals(4,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
	}
	
	@Test
	public void testStartNewGameWithStandardRuleWhenAppliedImplicitlyScenario7() throws InterruptedException {
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		// 2: Get game status self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());
		assertNull(gameViewSelf.getGameMetaInfo().getRules());
		
		// 3: Get game status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, 
				null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		assertNull(gameViewSelf.getGameMetaInfo().getRules());
		
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		//4. Validate that shots get fired as per the rule
		String fireBackGameEndpointOpponent = "http://localhost:" 
				+ + ((game.isTurnSelf())?selfPort:otherPort)
				+ "/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		ResponseEntity<SalvoImpact> gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		SalvoImpact salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		assertEquals(5,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
		
		game = this.gameEngine.fetchGame(gameId);
		
		// Auto-play till a ship gets destroyed
		while(true){
			// Fire until a ship is destroyed
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+ "/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
			game = this.gameEngine.fetchGame(gameId);
			if(game.getOpponentOnBoardEntitiesAlive()!=5||game.getSelfOnBoardEntitiesAlive()!=5) break;
		}
		
		boolean fireOneExtraRound = false;
		if(game.isOpponentOnBoardEntityDestroyed()&&game.isTurnSelf()
				||game.isSelfOnBoardEntityDestroyed()&&!game.isTurnSelf()) fireOneExtraRound = true;
	
		// 5: Destroy a ship & then validate that an additional (2) shots get fired back
		fireBackGameEndpointOpponent = "http://localhost:" 
				+ + ((game.isTurnSelf())?selfPort:otherPort)
				+ "/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		
		if(fireOneExtraRound){
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+ "/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		}
		assertEquals(4,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
	}
	
	@Test
	public void testStartNewGameWithDesperationRuleScenario7() throws InterruptedException {
		String rule="desperation";
		
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \""+rule+"\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		// 2: Get game status self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		// 3: Get game status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, 
				null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		//4. Validate that shots get fired as per the rule
		String fireBackGameEndpointOpponent = "http://localhost:" 
				+ + ((game.isTurnSelf())?selfPort:otherPort)
				+ "/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		ResponseEntity<SalvoImpact> gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		SalvoImpact salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		assertEquals(1,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
		
		// Auto-play till a ship gets destroyed
		while(true){
			// Fire until a ship is destroyed
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+ "/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
			game = this.gameEngine.fetchGame(gameId);
			if(game.getOpponentOnBoardEntitiesAlive()!=5||game.getSelfOnBoardEntitiesAlive()!=5) break;
		}
		
		boolean fireOneExtraRound = false;
		if(game.isOpponentOnBoardEntityDestroyed()&&game.isTurnSelf()
				||game.isSelfOnBoardEntityDestroyed()&&!game.isTurnSelf()) fireOneExtraRound = true;
		
		// 5: Destroy a ship & then validate that an additional (2) shots get fired back
		fireBackGameEndpointOpponent = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort)
				+"/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		
		if(fireOneExtraRound){
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ ((game.isTurnSelf())?selfPort:otherPort)
					+"/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		}
		assertEquals(2,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
	}
	
	@Test
	public void testStartNewGameWithSuperChargeRuleScenario7() throws InterruptedException {
		String rule="super-charge";
		
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \""+rule+"\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		// 2: Get game status self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		// 3: Get game status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, 
				null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		//4. Validate that shots get fired as per the rule
		String fireBackGameEndpointOpponent = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort)
				+"/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		ResponseEntity<SalvoImpact> gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		SalvoImpact salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response - supercharge: "+salvoResponse.toString());
		assertEquals(5,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
		
		String previousPlayerTurn;
		previousPlayerTurn = game.getPlayerTurn();
		// Auto-play till a ship gets destroyed
		while(true){
			boolean turnPriorToFiring = game.isTurnSelf();
			log.debug("Player turn: "+previousPlayerTurn);
			// Fire until a ship is destroyed
			fireBackGameEndpointOpponent = "http://localhost:" 
					+ + ((game.isTurnSelf())?selfPort:otherPort)
					+ "/spaceship/protocol/game/"
					+ gameId +"/fire-back";
			log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
			gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
					controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
					SalvoImpact.class);
			assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
			salvoResponse = gameResponseFireBackEntity.getBody();
			log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
			game = this.gameEngine.fetchGame(gameId);
			if(game.getOpponentOnBoardEntitiesAlive()!=5||game.getSelfOnBoardEntitiesAlive()!=5) {
				// Retains turn
				assertEquals(turnPriorToFiring,game.isTurnSelf());
				assertEquals(previousPlayerTurn, game.getPlayerTurn());
				log.debug("Player retains turn: "+game.getPlayerTurn());
				break;
			}else{
				assertEquals(!turnPriorToFiring, game.isTurnSelf());
				assertFalse(previousPlayerTurn.equals(game.getPlayerTurn()));
				previousPlayerTurn = game.getPlayerTurn();
			}
		}
		
		assertTrue(game.isOpponentOnBoardEntityDestroyed()||game.isSelfOnBoardEntityDestroyed());
		
		assertEquals(5,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());

		// 6: Validate that in the next run the correct player fires
		log.debug("Player turn: "+game.getPlayerTurn());
		fireBackGameEndpointOpponent = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort)
				+ "/spaceship/protocol/game/"
				+ gameId +"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response - supercharge: "+salvoResponse.toString());
		assertEquals(5,salvoResponse.getSalvo().keySet().size());
		assertNotNull(salvoResponse.getGame().getPlayerTurn());

	}
	
	@Test
	public void testStartNewGameWithValidXShotRuleScenario7() throws InterruptedException {
		String rule="9-shot";
		
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \""+rule+"\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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

		// 2: Get game status self
		String viewStatusGameEndpointSelf = "http://localhost:" + selfPort + newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointSelf);
		ResponseEntity<GameView> gameResponseEntitySelf = restTemplate.exchange(viewStatusGameEndpointSelf, HttpMethod.GET, null,
				GameView.class);
		GameView gameViewSelf = gameResponseEntitySelf.getBody();
		log.debug("Game view self: "+gameViewSelf.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntitySelf.getStatusCode());
		assertNotNull(gameViewSelf.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewSelf.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		// 3: Get game status from opponent
		String viewStatusGameEndpointOpponent = "http://localhost:" + otherPort+ newGameResponseBodyLocation;
		log.debug("View game status endpoint: "+viewStatusGameEndpointOpponent);
		ResponseEntity<GameView> gameResponseEntityOpponent = restTemplate.exchange(viewStatusGameEndpointOpponent, HttpMethod.GET, 
				null,
				GameView.class);
		GameView gameViewOpponent = gameResponseEntityOpponent.getBody();
		log.debug("Game view opponent: "+gameViewOpponent.toString());
		
		assertEquals(HttpStatus.OK, gameResponseEntityOpponent.getStatusCode());
		assertNotNull(gameViewOpponent.getGameMetaInfo().getPlayerTurn());
		assertNull(gameViewOpponent.getGameMetaInfo().getWon());
		assertEquals(rule,gameViewSelf.getGameMetaInfo().getRules());
		
		String gameId = newGameResponseBodyLocation.replace("/spaceship/user/game/", "");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		
		//4. Validate that shots get fired as per the rule
		String fireBackGameEndpointOpponent = "http://localhost:" 
				+ ((game.isTurnSelf())?selfPort:otherPort)
				+"/spaceship/protocol/game/"
				+ newGameResponseBodyLocation.replace("/spaceship/user/game/", "")
				+"/fire-back";
		log.debug("Fire back endpoint: "+fireBackGameEndpointOpponent);
		ResponseEntity<SalvoImpact> gameResponseFireBackEntity = restTemplate.exchange(fireBackGameEndpointOpponent, HttpMethod.PUT, 
				controllerHelper.prepareRequestBody("", MediaType.APPLICATION_JSON, String.class),
				SalvoImpact.class);
		assertEquals(HttpStatus.OK, gameResponseFireBackEntity.getStatusCode());
		SalvoImpact salvoResponse = gameResponseFireBackEntity.getBody();
		log.debug("Salvo fired on opponent response: "+salvoResponse.toString());
		assertEquals(rule.replace("-shot", ""),salvoResponse.getSalvo().keySet().size()+"");
		assertNotNull(salvoResponse.getGame().getPlayerTurn());
		
	}

	@Test
	public void testStartNewGameWithInValidXShotDefaultsToStandardRuleScenario7() throws InterruptedException {
		String rule="12-shot";
		
		String createGame = "{\"user_id\": \""+selfId+"\",\"full_name\": \"gameLabs Opponent\",\"rules\": \""+rule+"\",\"spaceship_protocol\": {\"hostname\": \""
				+ hostName + "\",\"port\": " + selfPort + "}}";
		String createNewGameEndpoint = "http://localhost:" + otherPort + "/spaceship/user/game/new";

		log.debug("Sending create new game request on: " + createNewGameEndpoint);
		log.debug("Create game message: " + createGame);
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
				HttpStatus.BAD_REQUEST.equals(newGameResponseEntity.getStatusCode()));
		
	}

	/**
	 * Starts up a 2nd game application, to be used for testing player interactions
	 * 
	 * @param otherServerPort
	 * @param otherManagementPort
	 * @param otherInstanceId
	 * @return
	 */
	public static Thread startSecondApplicationContext(final int otherServerPort, final int otherServerManagementPort,
			final String otherServerInstanceId) {
		log.debug("Starting another application instance ");

		SpringApplication application = new SpringApplication(SpaceshipApplication.class);
		Properties properties = new Properties();
		properties.put("server.port", otherServerPort);
		properties.put("management.port", otherServerManagementPort);
		properties.put("eureka.client.enabled", false);
		application.setDefaultProperties(properties);

		Thread t = new Thread() {
			public void run() {
				try {
					ConfigurableApplicationContext appContext = SpringApplication.run(MockSpaceshipApplication.class,
							"--eureka.client.enabled=false",
							"--server.port=" + otherServerPort,
							"--management.port=" + otherServerManagementPort,
							"--spring.application.instance_id=" + otherServerInstanceId);
					log.debug("2nd server instance started :" + appContext);
				} catch (Exception e) {
					log.error("2nd instance already started: ");
				}
			}
		};

		//t.start();
		return t;
	}

}
