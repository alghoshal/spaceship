package in.game.game.rule;

import org.junit.Before;
import org.junit.Test;

import in.game.game.rules.GameRules;
import in.game.game.rules.GameRulesFactory;
import in.game.model.SpaceshipGame;
import in.game.model.User;
import static org.junit.Assert.*;

public class SuperChargeTest {
	
	SpaceshipGame game;
	String selfId = "self";
	String opponentId = "opponent";
	
	@Before
	public void setUp(){
		User self = new User();
		self.setUserId(selfId);
		
		User opponent = new User();
		opponent.setUserId(opponentId);
		game = new SpaceshipGame(self,opponent);
	}
	
	@Test
	public void testGetRule(){
		int shipsAlive =2;
		game.setSelfOnBoardEntitiesAlive(shipsAlive);
		GameRules rule = GameRulesFactory.fetchRule(GameRules.SUPER_CHARGE);
		assertEquals(shipsAlive,rule.getNumberOfShots(game));
		
		game.setOpponentOnBoardEntityDestroyed(true);
		assertEquals(shipsAlive,rule.getNumberOfShots(game));
	}
	
	@Test
	public void testGetRuleOpponent(){
		int shipsAlive =2;
		int shipsAliveOpponent = 3;
		game.setSelfOnBoardEntitiesAlive(shipsAlive);
		game.setOpponentOnBoardEntitiesAlive(shipsAliveOpponent);
		GameRules rule = GameRulesFactory.fetchRule(GameRules.SUPER_CHARGE);
		
		assertEquals(shipsAlive,rule.getNumberOfShots(game));
		assertEquals(shipsAliveOpponent,rule.getNumberOfShots(game,false));
		assertEquals(shipsAlive,rule.getNumberOfShots(game,true));
		
		game.setOpponentOnBoardEntityDestroyed(true);
		assertEquals(shipsAlive,rule.getNumberOfShots(game));
		assertEquals(shipsAlive,rule.getNumberOfShots(game,true));
		assertEquals(shipsAliveOpponent,rule.getNumberOfShots(game,false));
		
		game.setOpponentOnBoardEntityDestroyed(false);
		game.setSelfOnBoardEntityDestroyed(true);
		assertEquals(shipsAlive,rule.getNumberOfShots(game));
		assertEquals(shipsAlive,rule.getNumberOfShots(game,true));
		assertEquals(shipsAliveOpponent,rule.getNumberOfShots(game,false));
	}
	
	@Test
	public void testFlipTurn(){
		int shipsAlive =2;
		int shipsAliveOpponent = 3;
		game.setSelfOnBoardEntitiesAlive(shipsAlive);
		game.setOpponentOnBoardEntitiesAlive(shipsAliveOpponent);
		GameRules rule = GameRulesFactory.fetchRule(GameRules.SUPER_CHARGE);
		game.setGameRules(rule);

		// Default, NO-KILL mode
		game.setTurnSelf(true);
		assertFalse(rule.flipTurn(game));

		game.flipTurn();
		assertFalse(game.isTurnSelf());
		assertTrue(rule.flipTurn(game));
		
		game.flipTurn();
		assertFalse(rule.flipTurn(game));
		assertTrue(game.isTurnSelf());
		
		// Kill mode - opponent killed
		game.setTurnSelf(true);
		game.setOpponentOnBoardEntityDestroyed(true);
		
		assertTrue(rule.flipTurn(game));
		game.flipTurn();
		assertTrue(game.isTurnSelf());

		// Kill mode - self killed
		game.setTurnSelf(false);
		game.setOpponentOnBoardEntityDestroyed(false);
		game.setSelfOnBoardEntityDestroyed(true);
		assertFalse(rule.flipTurn(game));
		game.flipTurn();
		assertFalse(rule.flipTurn(game));
		assertFalse(game.isTurnSelf());

	}

}
