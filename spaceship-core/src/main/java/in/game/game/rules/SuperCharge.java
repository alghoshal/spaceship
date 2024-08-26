package in.game.game.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.game.model.BoardGame;
import in.game.model.SpaceshipGame;

/**
 * Awards another set of salvo incase a ship is destroyed in this turn
 *  
 * @author aghoshal
 */
public class SuperCharge extends StandardRule implements GameRules {
	private static final Logger log = LoggerFactory.getLogger(SuperCharge.class);

	/**
	 * Defaults to {@link StandardRule}, 
	 * expect if an opponent's ship was destroyed in the last round, 
	 * in which case gives the player another salvo of shots.
	 */
	public int getNumberOfShots(BoardGame game) {
		return super.getNumberOfShots(game);
	}
	
	public int getNumberOfShots(BoardGame game, boolean turnSelf) {
		return super.getNumberOfShots(game,turnSelf);
	}
	
	/**
	 * @see in.game.game.rules.StandardRule#getName()
	 */
	public String getName() {
		return SUPER_CHARGE;
	}
	
	/**
	 * @see in.game.game.rules.GameRules#flipTurn(in.game.model.BoardGame)
	 */
	public boolean flipTurn(BoardGame game) {
		boolean turnSelf =game.isTurnSelf();
		// Retain turn on kill
		if((turnSelf&&game.isOpponentOnBoardEntityDestroyed())||
				(!turnSelf&&game.isSelfOnBoardEntityDestroyed())) {
			log.debug(game.getGameId()+" super-charged, player retains turn: "+game.getPlayerTurn());
			return turnSelf;
		}
		return !turnSelf;
	}

}
