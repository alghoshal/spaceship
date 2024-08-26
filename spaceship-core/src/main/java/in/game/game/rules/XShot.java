package in.game.game.rules;

import in.game.model.BoardGame;

/**
 * Allows firing a salvo of X (1 to 10) shots
 *  
 * @author aghoshal
 */
public class XShot implements GameRules {
	int numberOfShotsX;
	
	public XShot(int numberOfShotsX){
		this.numberOfShotsX = numberOfShotsX;
	}
	
	/**
	 * Fixed X-number of shots fired
	 */
	public int getNumberOfShots(BoardGame game) {
		return numberOfShotsX;
	}
	
	/**
	 * Fixed X-number of shots fired
	 */
	public int getNumberOfShots(BoardGame game, boolean turnSelf) {
		return numberOfShotsX;
	}
	
	public int getNumberOfShots(int entitiesAlive) {
		return numberOfShotsX;
	}
	
	/**
	 * @see in.game.game.rules.GameRules#getName()
	 */
	public String getName() {
		return numberOfShotsX+X_SHOT;
	}
	
	/**
	 * @see in.game.game.rules.GameRules#flipTurn(in.game.model.BoardGame)
	 */
	public boolean flipTurn(BoardGame game) {
		return !game.isTurnSelf();
	}

}
