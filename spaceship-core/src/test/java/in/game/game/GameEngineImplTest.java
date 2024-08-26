package in.game.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import in.game.app.SpaceshipCoreApplication;
import in.game.game.persistence.PersistenceService;
import in.game.game.rules.GameRules;
import in.game.game.rules.GameRulesFactory;
import in.game.game.rules.StandardRule;
import in.game.model.GameMetaInfo;
import in.game.model.NewGameRequest;
import in.game.model.SalvoFireRequest;
import in.game.model.SalvoImpact;
import in.game.model.SalvoImpactType;
import in.game.model.SpaceshipGame;
import in.game.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(SpaceshipCoreApplication.class)
@TestPropertySource(properties ={"eureka.client.enabled=false"})
public class GameEngineImplTest {
	@Autowired
	PersistenceService<SpaceshipGame> persistenceService;
	
	@Autowired
	GameEngine gameEngine;
	
	String fullName="abc";
	String userId="123";
	String gameId="ads-sdsd-bbb12";
	String opponentId="2323";
	
	@Test
	public void testPrepareGameSnapshot(){
		
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self, opponent);
		game.setGameId(gameId);
		
		NewGameRequest newGameRequest = new NewGameRequest();
		GameEngineImpl impl = new GameEngineImpl();
		GameMetaInfo snapshotResponse = impl.prepareGameSnapshot(game,newGameRequest);
		
		assertEquals(fullName,snapshotResponse.getFullName());
		assertEquals(gameId,snapshotResponse.getGameId());
		assertEquals(userId,snapshotResponse.getUserId());
		assertTrue("Starting player not valid: "+snapshotResponse.getStarting(),
				snapshotResponse.getStarting().equals(userId)
				||snapshotResponse.getStarting().equals(opponentId));
		
	}
	
	@Test
	public void testPrepareGameSnapshotWithRules(){
		
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);
		
		String rule = "9-shot";
		User opponent = new User();
		opponent.setUserId(opponentId);
		opponent.setRules(rule);
		SpaceshipGame game = new SpaceshipGame(self, opponent);
		game.setGameId(gameId);
		game.setGameRules(GameRulesFactory.fetchRule(rule));
		
		NewGameRequest newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		GameEngineImpl impl = new GameEngineImpl();
		GameMetaInfo snapshotResponse = impl.prepareGameSnapshot(game,newGameRequest);
		
		assertEquals(fullName,snapshotResponse.getFullName());
		assertEquals(gameId,snapshotResponse.getGameId());
		assertEquals(userId,snapshotResponse.getUserId());
		assertTrue("Starting player not valid: "+snapshotResponse.getStarting(),
				snapshotResponse.getStarting().equals(userId)
				||snapshotResponse.getStarting().equals(opponentId));
		assertEquals(rule, snapshotResponse.getRules());
	}
	
	@Test
	public void testPrepareGameSnapshotWithoutRules(){
		
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);
		
		String rule = "9-shot";
		User opponent = new User();
		opponent.setUserId(opponentId);
		SpaceshipGame game = new SpaceshipGame(self, opponent);
		game.setGameId(gameId);
		game.setGameRules(GameRulesFactory.fetchRule(rule));
		
		NewGameRequest newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		GameEngineImpl impl = new GameEngineImpl();
		GameMetaInfo snapshotResponse = impl.prepareGameSnapshot(game,newGameRequest);
		
		assertEquals(fullName,snapshotResponse.getFullName());
		assertEquals(gameId,snapshotResponse.getGameId());
		assertEquals(userId,snapshotResponse.getUserId());
		assertTrue("Starting player not valid: "+snapshotResponse.getStarting(),
				snapshotResponse.getStarting().equals(userId)
				||snapshotResponse.getStarting().equals(opponentId));
		assertNull(snapshotResponse.getRules());
		
	}
	
	@Test
	public void testGamePreparedAndPersisted(){
		String fullName="abc";
		String userId="123";
		String opponentId="2323";
		
		User self = new User();
		
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);

		SpaceshipGame game = new SpaceshipGame(self,opponent);
		
		NewGameRequest newGameRequest = new NewGameRequest();
		newGameRequest.setSelf(self);
		newGameRequest.setOpponent(opponent);
		newGameRequest.setPlayerTurn(opponentId);
		GameMetaInfo snapshotResponse = gameEngine.setUpNewGame(newGameRequest);
		String gameId = snapshotResponse.getGameId();
		
		assertEquals(fullName,snapshotResponse.getFullName());
		assertEquals(userId,snapshotResponse.getUserId());
		assertTrue("Starting player not valid: "+snapshotResponse.getStarting(),
				snapshotResponse.getStarting().equals(opponentId));

		// Validate game is persisted
		SpaceshipGame gameFromCache= persistenceService.lookUpByKey(gameId);
		assertEquals(game.getSelf(), gameFromCache.getSelf());
		assertEquals(game.getOpponent(), gameFromCache.getOpponent());
		assertEquals(gameId, gameFromCache.getGameId());
		assertEquals(opponentId, gameFromCache.getPlayerTurn());
	}
	
	@Test
	public void testCaptureSalvoImpactOpponent(){
		String fullName="abc";
		String userId="123";
		String opponentId="2323";
		
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		
		NewGameRequest newGameRequest = new NewGameRequest();
		newGameRequest.setSelf(self);
		newGameRequest.setOpponent(opponent);
		GameMetaInfo snapshotResponse = gameEngine.setUpNewGame(newGameRequest);
		String gameId = snapshotResponse.getGameId();
		
		assertEquals(fullName,snapshotResponse.getFullName());
		assertEquals(userId,snapshotResponse.getUserId());
		assertTrue("Starting player not valid: "+snapshotResponse.getStarting(),
				snapshotResponse.getStarting().equals(userId)
				||snapshotResponse.getStarting().equals(opponentId));

		// Validate game is persisted
		SpaceshipGame gameFromCache= persistenceService.lookUpByKey(gameId);
		assertEquals(game.getSelf(), gameFromCache.getSelf());
		assertEquals(game.getOpponent(), gameFromCache.getOpponent());
		assertEquals(gameId, gameFromCache.getGameId());
		
		SalvoImpact salvoResponseOpponent = new SalvoImpact();
		Map<String, String> salvo = new HashMap<String, String>();
		salvo.put("0x0", "miss");
		salvo.put("AxB", "hit");
		salvo.put("7x8", "miss");
		salvoResponseOpponent.setSalvo(salvo);
		GameMetaInfo snapshot = new GameMetaInfo();
		snapshot.setPlayerTurn(opponentId);
		salvoResponseOpponent.setGame(snapshot);
		SalvoImpact impactResult = gameEngine.captureSalvoImpactOnOpponent(gameId, salvoResponseOpponent);
		assertNotNull(impactResult);
		
		// Validate game is persisted
		gameFromCache= persistenceService.lookUpByKey(gameId);
		assertEquals(game.getSelf(), gameFromCache.getSelf());
		assertEquals(game.getOpponent(), gameFromCache.getOpponent());
		assertEquals(gameId, gameFromCache.getGameId());		
		char[][] opponentBoard = gameFromCache.getBoardOpponent();
		
		assertEquals(SalvoImpactType.MISS.getImpactValue(),opponentBoard[0][0]);
		assertEquals(SalvoImpactType.EMPTY.getImpactValue(),opponentBoard[1][1]);
		assertEquals(SalvoImpactType.HIT.getImpactValue(),opponentBoard[10][11]);
		assertEquals(SalvoImpactType.MISS.getImpactValue(),opponentBoard[7][8]);

		
	}
	
	@Test
	public void testGameIsValidInactive(){
		String gameId = "avas";
		SpaceshipGame game = new SpaceshipGame();
		game.setActive(false);
		game.setGameId(gameId);
		
		this.persistenceService.save(gameId, game);
		assertFalse(gameEngine.isValidGame(gameId));
	}

	@Test
	public void testGameIsValidNull(){
		String gameId = "avas33";
		SpaceshipGame game = new SpaceshipGame();
		game.setActive(false);
		game.setGameId(gameId);
		
		this.persistenceService.save(gameId, game);
		assertFalse(gameEngine.isValidGame("random-key"));
	}

	@Test
	public void testGameIsValidActive(){
		String gameId = "avas";
		SpaceshipGame game = new SpaceshipGame();
		game.setGameId(gameId);
		
		this.persistenceService.save(gameId, game);
		assertTrue(gameEngine.isValidGame(gameId));
	}

	
	@Test
	public void testSetSalvoImpactInError(){
		String errorMessage = "something wrong";
		
		GameEngineImpl gameEngineImpl = (GameEngineImpl) gameEngine;
		SalvoImpact salvoImpact = gameEngineImpl.setUpSalvoImpactForError(errorMessage,HttpStatus.BAD_REQUEST);
		
		assertEquals(errorMessage, salvoImpact.getErrorMessage());
		assertTrue(salvoImpact.isErrorFlag());
		assertEquals(HttpStatus.BAD_REQUEST,salvoImpact.getResponseStatus());
	}
	
	@Test
	public void testGameInActiveAllSalvosMiss(){
		String gameId = "a434vas";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setActive(false);
		this.persistenceService.save(gameId, game);
		
		assertFalse(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		
		Map<String, String> salvoImpacts = salvoImpact.getSalvo();

		assertNotNull(salvoImpact);
		assertEquals(salvo.length, salvoImpacts.size()); 
		for (String aSalvoImpact : salvoImpacts.values()) {
			assertEquals(SalvoImpactType.MISS.getImpactName(), aSalvoImpact);
		}
		assertEquals(HttpStatus.NOT_FOUND, salvoImpact.getResponseStatus());
		
	}
	
	@Test
	public void testHandleSalvosFiredMoreThanAllowed(){
		String gameId = "avas89";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		
		assertTrue(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		salvoFireRequest.setSelf(self);
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
		
	}
	
	@Test
	public void testHandleSalvosFiredLessThanAllowed(){
		String gameId = "avas4343";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(3);
		this.persistenceService.save(gameId, game);
		
		assertTrue(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		salvoFireRequest.setSelf(self);
		
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
		
	}
	
	@Test
	public void testSalvoFireOnOpponentValidateSalvoCountDisabledTurnSelfEnabledScenario3() {
		String gameId = "avas45";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);
		game.setTurnSelf(true);
		
		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		
		GameEngineImpl gameEngineImpl = (GameEngineImpl) gameEngine;
		gameEngineImpl.validateSalvoCount = false;
		assertTrue(gameEngineImpl.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		salvoFireRequest.setSelf(self);
		
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		gameEngineImpl.validateSalvoCount = true;

		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
	}
	
	@Test
	public void testSalvoFireOnOpponentValidateSalvoCountEnabledScenario3() {
		String gameId = "ava4s";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		
		assertTrue(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
	}
	
	@Test
	public void testSalvoFireOnOpponentValidateTurnDisabledScenario3() {
		String gameId = "avas3";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		
		GameEngineImpl gameEngineImpl = (GameEngineImpl) gameEngine;
		gameEngineImpl.validatePlayerTurn = false;
		assertTrue(gameEngineImpl.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		
		salvoFireRequest.setSelf(self);
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		gameEngineImpl.validatePlayerTurn = true;

		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
	}
	
	@Test
	public void testSalvoFireOnOpponentValidateTurnEnabledScenario3() {
		String gameId = "avas2";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		
		assertTrue(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		salvoFireRequest.setSelf(self);
		
		SalvoImpact salvoImpact = gameEngine.handleSalvoFire(gameId, salvoFireRequest);
		assertTrue(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.BAD_REQUEST, salvoImpact.getResponseStatus());
	}
	
	@Test
	public void testSalvoFireOnOpponentValidateTurnAndSalvoCountDisabledScenario3() {
		String gameId = "avas1";
		User self = new User();
		self.setFullName(fullName);
		self.setUserId(userId);

		User opponent = new User();
		opponent.setUserId(opponentId);
		
		SpaceshipGame game = new SpaceshipGame(self,opponent);
		game.setGameId(gameId);

		game.setSelfOnBoardEntitiesAlive(1);
		this.persistenceService.save(gameId, game);
		GameEngineImpl gameEngineImpl = (GameEngineImpl) gameEngine;
		gameEngineImpl.validatePlayerTurn = false;
		gameEngineImpl.validateSalvoCount = false;

		
		assertTrue(gameEngine.isValidGame(gameId));
		
		SalvoFireRequest salvoFireRequest = new SalvoFireRequest();
		String salvo[] = {"0x0","AxA"};
		salvoFireRequest.setSalvo(salvo);
		salvoFireRequest.setSelf(self);
		SalvoImpact salvoImpact = gameEngineImpl.handleSalvoFire(gameId, salvoFireRequest);
		
		gameEngineImpl.validatePlayerTurn = true;
		gameEngineImpl.validateSalvoCount = true;
		assertFalse(salvoImpact.isErrorFlag());
		assertNotNull(salvoImpact);
		assertNotNull(salvoImpact.getSalvo());
		assertEquals(HttpStatus.OK, salvoImpact.getResponseStatus());
	}

}
