package in.game.service.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.model.GameMetaInfo;
import in.game.model.NewGameRequest;
import in.game.model.SalvoImpact;
import in.game.model.User;

/**
 * Utilities for game play client services
 *  
 * @author aghoshal
 */
public class GamePlayClientHelper {
	private static final Logger log = LoggerFactory.getLogger(GamePlayClientHelper.class);

	@Autowired
	ControllerHelper controllerHelper;
	
	@Autowired
	GameEngine gameEngine;

	/**
	 * Captures details of a newly set-up game with an opponent
	 * 
	 * @param opponent
	 * @param newGameResponse
	 */
	public void captureGameImage(User opponent, GameMetaInfo newGameResponse) {
		// Create image of game on self
		NewGameRequest newGameImageOnSelf = new NewGameRequest(controllerHelper.fetchSelf(), opponent);
		newGameImageOnSelf.setPlayerTurn(newGameResponse.getStarting());
		
		// Keep the gameId consistent
		newGameImageOnSelf.setGameId(newGameResponse.getGameId());
		
		GameMetaInfo newGameImage = this.gameEngine.setUpNewGame(newGameImageOnSelf);
		log.debug("Image of game: " + newGameImage);
	}
	
	/**
	 * Prepares error SalvoImpact object
	 * 
	 * @param message
	 * @return
	 */
	public SalvoImpact prepareSalvoImpactInError(String errorMessage) {
		SalvoImpact salvoImpact = new SalvoImpact();
		salvoImpact.setErrorMessage(errorMessage);
		salvoImpact.setErrorFlag(true);
		return salvoImpact;
	}
}
