package in.game.game.rules;

import in.game.model.BoardGame;

/**
 * Default rule of the game
 *  
 * @author aghoshal
 */
public class StandardRule implements GameRules {
	
	/**
	 * Equals the number of ships alive of self
	 */
	public int getNumberOfShots(BoardGame game) {
		return game.getSelfOnBoardEntitiesAlive();
	}

	/**
	 * Equals the number of ships alive of self or opponent
	 */
	public int getNumberOfShots(BoardGame game, boolean turnSelf) {
		if(!turnSelf) return game.getOpponentOnBoardEntitiesAlive();
		return game.getSelfOnBoardEntitiesAlive();
	}
	
	/**
	 * @see in.game.game.rules.GameRules#getName()
	 */
	public String getName() {
		return STANDARD;
	}
	
	/**
	 * @see in.game.game.rules.GameRules#flipTurn(in.game.model.BoardGame)
	 */
	public boolean flipTurn(BoardGame game) {
		return !game.isTurnSelf();
	}

}
