package in.game.game.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.game.model.BoardGame;
import in.game.model.SpaceshipType;

/**
 * Allows 1 shot initially & 1 extra shot for every ship destroyed
 *  
 * @author aghoshal
 */
public class Desperation implements GameRules {
	private static final Logger log = LoggerFactory.getLogger(Desperation.class);
	int totalNoOfShipsAllocated;
	
	public Desperation(){
		this.totalNoOfShipsAllocated = SpaceshipType.values().length;
	}
	
	/**
	 * Equals the number of ships alive for self
	 */
	public int getNumberOfShots(BoardGame game) {
		return getNumberOfShots(game, true);
	}
	
	/**
	 * Equals the no of ships alive for either self or opponent
	 * @see in.game.game.rules.GameRules#getNumberOfShots(in.game.model.BoardGame, boolean)
	 */
	public int getNumberOfShots(BoardGame game, boolean turnSelf) {
		log.debug("Fetching desperation shots for game: "+game);
		if(!turnSelf) return 1 + totalNoOfShipsAllocated - game.getOpponentOnBoardEntitiesAlive();
		return 1 + totalNoOfShipsAllocated - game.getSelfOnBoardEntitiesAlive();
	}
	
	/**
	 * @see in.game.game.rules.GameRules#getName()
	 */
	public String getName() {
		return DESPERATION;
	}
	
	/**
	 * @see in.game.game.rules.GameRules#flipTurn(in.game.model.BoardGame)
	 */
	public boolean flipTurn(BoardGame game) {
		return !game.isTurnSelf();
	}

}
