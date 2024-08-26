package in.game.game.rules;

import in.game.model.BoardGame;

/**
 * Rules by which the game is played
 * 
 * @author aghoshal
 *
 */
public interface GameRules {

	String STANDARD ="standard";
	String SUPER_CHARGE ="super-charge";
	String DESPERATION ="desperation";
	String X_SHOT = "-shot";

	/**
	 * Number of shots allowed at any stage of the game
	 * 
	 * @param game
	 * @return
	 */
	int getNumberOfShots(BoardGame game);
	
	/**
	 * Number of shots allowed at any stage of the game,
	 * for the entity self if true, or else opponent
	 * 
	 * @param game
	 * @return
	 */
	int getNumberOfShots(BoardGame game, boolean turnSelf);
	
	/**
	 * Rule Name
	 * @return
	 */
	String getName();
	
	/**
	 * Flips turn as per the current stage of the game
	 * 
	 * @param game
	 * @return
	 */
	boolean flipTurn(BoardGame game);
}
