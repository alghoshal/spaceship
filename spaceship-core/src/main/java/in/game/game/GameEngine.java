package in.game.game;

import java.util.Collection;

import org.springframework.stereotype.Service;

import in.game.model.SpaceshipGame;
import in.game.model.GameMetaInfo;
import in.game.model.NewGameRequest;
import in.game.model.SalvoFireRequest;
import in.game.model.SalvoImpact;
import in.game.model.view.GameView;

@Service
public interface GameEngine {
	
	/**
	 * Sets up a new game 
	 * @param newGameRequest
	 * @return
	 */
	GameMetaInfo setUpNewGame(NewGameRequest newGameRequest);
	
	/**
	 * Returns details of the game
	 * 
	 * @param gameId
	 * @return
	 */
	SpaceshipGame fetchGame(String gameId);
	
	/**
	 * Returns a listing of all games being played
	 * 
	 * @return
	 */
	Collection<SpaceshipGame> fetchAll();
	
	/**
	 * True if a valid game exists for the gameId
	 * 
	 * @param gameId
	 * @return
	 */
	boolean isValidGame(String gameId);
	
	/**
	 * Returns details of the game transformed as GameView
	 * 
	 * @param gameId
	 * @return
	 */
	GameView fetchGameView(String gameId);
	
	/**
	 * Handles salvo fire from an opponent
	 * @param gameId
	 * @param salvo
	 * @return
	 */
	SalvoImpact handleSalvoFire(String gameId, SalvoFireRequest salvo);

	/**
	 * Updates opponents board as per the salvo impact response
	 * 
	 * @param gameId
	 * @param salvoResponse
	 * @return
	 */
	SalvoImpact captureSalvoImpactOnOpponent(String gameId, SalvoImpact salvoResponseOpponent);

	/**
	 * Saves updated game
	 * 
	 * @param game
	 * @return
	 */
	SpaceshipGame updateGame(SpaceshipGame game);
}
