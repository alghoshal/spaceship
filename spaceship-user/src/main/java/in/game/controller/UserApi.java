package in.game.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import in.game.model.SalvoImpact;
import in.game.model.User;
import in.game.model.view.GameView;

/**
 * User api used by players to communicate
 * 
 * @author aghoshal
 */
public interface UserApi {
	
	/**
	 * Fires the specified round of salvos on the game opponent.
	 * 
	 * A round of the salvo specifies the X-Y co-ordinates of the 
	 * board (in hex) on which the opponent has fired.
	 * E.g.
	 * 	0xA -> (0,10),
	 * 	DxE -> (13,14), and so on.
	 * 
	 * Returns:
	 * -	Http Status 200 (OK) on success
	 * 	or 
	 * - 	Http Status 404 (Not Found) in case not allowed to 
	 *		fire on this game or invalid game
	 * 
	 * @param gameId
	 * @param salvo
	 * @return		SalvoImpact & Http/200 (Ok) on success,
	 * 				 or	else Http/404 (Not Found) on failure
	 */
	ResponseEntity<SalvoImpact> fireSalvoOnOpponent(String gameId, Map<String,String[]> salvo);
	
	/**
	 * Sets up the game to run in an auto-pilot mode until 
	 * one of the players wins.
	 * 
	 * Returns: a Http status 200 (OK) if the auto-pilot is 
	 * successfully set-up, or else 404 (Not Found) in case
	 * game is invalid.
	 * 
	 * @param response
	 * @param gameId
	 */
	void runAutoPilot(HttpServletResponse response, String gameId);
	
	/**
	 * Fetches game details
	 * 
	 * @param gameId
	 * @return			GameView & Http/200 (Ok) if the gameId is valid
	 * 				or else Http/404 (Not Found)
	 */
	ResponseEntity<GameView> fetchGame(String gameId);
	
	/**
	 * Sets up a new game between self & the specified opponent.
	 * - Also validates that this is a valid opponent reachable 
	 * on the hostname/ port specified in the request
	 * 
	 * - Sends a request to the opponent's 
	 * {@link ProtocolApi}{@link #createNewGame(HttpServletResponse, User)}
	 * to set-up the game between self & opponent.
	 * 
	 * Returns:
	 * - Http Status 303 (See Other) along with the
	 * game uri in the Location header.
	 * - Http Status 400 (Bad Request) on error in case opponent
	 * is unreachable.
	 * 
	 * @param response
	 * @param opponent
	 */
	void createNewGame(HttpServletResponse response, User opponent);
}
