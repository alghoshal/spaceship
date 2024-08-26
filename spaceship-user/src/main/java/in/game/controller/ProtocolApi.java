package in.game.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import in.game.game.rules.GameRules;
import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;
import in.game.model.User;

/**
 * Protocol API used by spaceship instances to communicate
 * 
 * @author aghoshal
 */
public interface ProtocolApi {
	
	/**
	 * Starts a new game against the specified user.
	 * - Returns Http Status 201 (Created)
	 * 
	 * @param opponent
	 * @return			GameView & Http/201 (Created)
	 */
	ResponseEntity<GameMetaInfo> startNewGame(User opponent);
	
	/**
	 * Handles salvo fire from an opponent. 
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
	 * @return			SalvoImpact & Http/200 (Ok) on success,
	 * 				 or	else Http/404 (Not Found) on failure
	 */
	ResponseEntity<SalvoImpact> handleSalvoFireFromOpponent(String gameId,Map<String, String[]> salvo);
	
	/**
	 * Fires back a round of salvos on the opponent,
	 * when triggered by an opponent/ caller.
	 * 
	 * Applies the appropriate game rules(@see {@link GameRules})
	 * to select a round of salvos to fire back on the opponent.
	 * 
	 * Returns response codes similar to {@link UserApi#fireSalvoOnOpponent(String, Map)}
	 * 
	 * @param gameId
	 * @return		
	 */
	ResponseEntity<SalvoImpact> fireBackOnOpponent(String gameId);
	
}
