package in.game.game.helpers;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import in.game.app.SpaceshipCoreApplication;
import in.game.game.GameEngine;
import in.game.model.SpaceshipGame;
import in.game.model.NewGameRequest;
import in.game.model.Spaceship;
import in.game.model.SpaceshipOrientation;
import in.game.model.SpaceshipType;
import in.game.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(SpaceshipCoreApplication.class)
@TestPropertySource(properties ={"eureka.client.enabled=false"})
public class GameBuilderTest {
	private static final Logger log = LoggerFactory.getLogger(GameBuilderTest.class);
	
	@Autowired
	SpaceshipGameBuilder gameBuilder; 

	@Autowired
	GameEngine gameEngine; 
	
	@Test
	public void testFetchSpaceShips(){
		List<Spaceship> spaceships=gameBuilder.fetchSpaceShips();
		assertEquals(5,spaceships.size());
	}
	
	@Test
	public void testAssignSpaceShipsToBoard(){
		List<Spaceship> spaceships=gameBuilder.fetchSpaceShips();
		
		log.debug(spaceships.toString());
		String selfId ="abc";
		User self = new User();
		self.setUserId(selfId);
		SpaceshipGame game = new SpaceshipGame(self,new User());
		game = gameBuilder.assignSpaceshipsToBoard(spaceships, game);
		
		log.debug(game.printBoard(game.getBoardSelf()));
		
		assertEquals(SpaceshipType.values().length,game.getSelfOnBoardEntitiesAlive());
	}

	
	@Test
	public void testHasGameRulesMentioned(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		assertFalse(gameBuilder.isGameRulesMentioned(newGameRequest));
		newGameRequest.setOpponent(opponent);
		assertFalse(gameBuilder.isGameRulesMentioned(newGameRequest));
		
		String rules = "10-shot";		
		opponent.setRules(rules);
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		assertTrue(gameBuilder.isGameRulesMentioned(newGameRequest));
	}
	
	@Test
	public void testBuildNewGameWithRules(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		String rules = "10-shot";		
		opponent.setRules(rules);
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		
		User self = new User();
		newGameRequest.setSelf(self);
		
		SpaceshipGame game = gameBuilder.buid(newGameRequest);
		
		assertTrue(game.isRulesMentioned());

	}

	@Test
	public void testBuildNewGameWithoutRules(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		
		User self = new User();
		newGameRequest.setSelf(self);
		
		SpaceshipGame game = gameBuilder.buid(newGameRequest);
		
		assertFalse(game.isRulesMentioned());
		assertNotNull(game.getGameId());

	}
	
	@Test
	public void testBuildNewGameWithGameId(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		String rules = "10-shot";		
		String gameId = "ab123d";
		opponent.setRules(rules);
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		newGameRequest.setGameId(gameId);
		User self = new User();
		newGameRequest.setSelf(self);
		
		SpaceshipGame game = gameBuilder.buid(newGameRequest);
		
		assertTrue(game.isRulesMentioned());
		assertEquals(gameId,game.getGameId());
	}
	
	@Test
	public void testBuildGameWithStartingPlayer(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		String rules = "10-shot";		
		String gameId = "ab123d";
		String opponentId = "opponentId";
		String userId = "selfId";
		opponent.setRules(rules);
		opponent.setUserId(opponentId);
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		newGameRequest.setGameId(gameId);
		User self = new User();
		self.setUserId(userId);
		newGameRequest.setSelf(self);
		newGameRequest.setPlayerTurn(opponent.getUserId());
		
		for(int i=0;i<100;i++){
			SpaceshipGame game = gameBuilder.buid(newGameRequest);
			assertEquals(opponent.getUserId(),game.getPlayerTurn());
			assertEquals(gameId,game.getGameId());
		}
	}
	
	@Test
	public void testGetStartingPlayer(){
		User opponent = new User();
		NewGameRequest newGameRequest = new NewGameRequest();
		String rules = "10-shot";		
		String gameId = "ab123d";
		String opponentId = "opponentId";
		String userId = "selfId";
		opponent.setRules(rules);
		opponent.setUserId(opponentId);
		newGameRequest = new NewGameRequest();
		newGameRequest.setOpponent(opponent);
		newGameRequest.setGameId(gameId);
		User self = new User();
		self.setUserId(userId);
		newGameRequest.setSelf(self);
		newGameRequest.setPlayerTurn(opponent.getUserId());
		
		SpaceshipGame game = new SpaceshipGame();
		game.setSelf(self);
		assertFalse(gameBuilder.getStartingPlayer(newGameRequest, game));
		
		newGameRequest.setPlayerTurn(self.getUserId());
		assertTrue(gameBuilder.getStartingPlayer(newGameRequest, game));
	}

}
