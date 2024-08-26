package in.game.service;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;
import in.game.model.User;

public interface GamePlayClientService {

	/**
	 * Client that fires salvos on opponent
	 * 
	 * @param gameId
	 * @param salvo
	 * @return
	 */
	ResponseEntity<SalvoImpact> fireSalvoOnOpponent(String gameId, String[] salvo);
	
	/**
	 * Client that fetches as set of salvos & fires them on opponent
	 * 
	 * @param gameId
	 * @return
	 */
	ResponseEntity<SalvoImpact> fireSalvoOnOpponent(String gameId);
	
	/**
	 * Client that asks opponent to fire salvo back on self
	 * 
	 * @param gameId
	 * @return
	 */
	ResponseEntity<SalvoImpact> haveOpponentFireBackOnSelf(String gameId);
	
	/**
	 * Sets up new game using Opponent's new game endpoint
	 * 
	 * @param response
	 * @param self
	 * @param newGameEndPoint
	 * @return
	 */
	GameMetaInfo setUpNewGameWithOpponent(HttpServletResponse response, User self, User opponent, String newGameEndPoint);
}
