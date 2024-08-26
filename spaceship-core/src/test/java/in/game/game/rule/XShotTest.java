package in.game.game.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import in.game.game.rules.GameRules;
import in.game.game.rules.GameRulesFactory;
import in.game.model.SpaceshipGame;
import in.game.model.User;

public class XShotTest {
	
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
		int x = 10;
		GameRules rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		assertEquals(x,rule.getNumberOfShots(game));
		
		x=12;
		rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		assertNull(rule);
	}
	
	@Test
	public void testGetRuleOpponent(){
		int shipsAlive =2;
		int shipsAliveOpponent = 3;
		int x = 10;
		game.setSelfOnBoardEntitiesAlive(shipsAlive);
		game.setOpponentOnBoardEntitiesAlive(shipsAliveOpponent);
		GameRules rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		
		assertEquals(x,rule.getNumberOfShots(game));
		assertEquals(x,rule.getNumberOfShots(game,false));
		assertEquals(x,rule.getNumberOfShots(game,true));
		
		game.setOpponentOnBoardEntityDestroyed(true);
		assertEquals(x,rule.getNumberOfShots(game));
		assertEquals(x,rule.getNumberOfShots(game,true));
		assertEquals(x,rule.getNumberOfShots(game,false));
		
		game.setOpponentOnBoardEntityDestroyed(false);
		game.setSelfOnBoardEntityDestroyed(true);
		assertEquals(x,rule.getNumberOfShots(game));
		assertEquals(x,rule.getNumberOfShots(game,true));
		assertEquals(x,rule.getNumberOfShots(game,false));
		
		x=12;
		rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		assertNull(rule);
	}
	
	@Test
	public void testGetRuleInvalidX(){
		int x=12;
		GameRules rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		assertNull(rule);
		
		x=0;
		rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);
		assertNull(rule);
		
		rule = GameRulesFactory.fetchRule("blah"+GameRules.X_SHOT);
		assertNull(rule);
	}
	
	@Test
	public void testFlipTurn(){
		int shipsAlive =2;
		int shipsAliveOpponent = 3;
		game.setSelfOnBoardEntitiesAlive(shipsAlive);
		game.setOpponentOnBoardEntitiesAlive(shipsAliveOpponent);
		int x=2;
		GameRules rule = GameRulesFactory.fetchRule(x+GameRules.X_SHOT);		
		game.setGameRules(rule);
		game.setTurnSelf(true);
		assertFalse(rule.flipTurn(game));

		game.flipTurn();
		assertFalse(game.isTurnSelf());
		assertTrue(rule.flipTurn(game));
		
		game.flipTurn();
		assertFalse(rule.flipTurn(game));
		assertTrue(game.isTurnSelf());
	}


}
