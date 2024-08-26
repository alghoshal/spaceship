package in.game.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import in.game.game.GameEngine;
import in.game.model.SalvoImpact;
import in.game.model.SpaceshipGame;

/**
 * Executes the game in auto play mode
 * 
 * @author aghoshal
 */
public class AutoPlayer implements Runnable{
	private static final Logger log = LoggerFactory.getLogger(AutoPlayer.class);

	GameEngine gameEngine;
	GamePlayClientService gamePlayClientService;
	SpaceshipGame game;
	int autoPlaySleepTime;
	boolean sleepInBetweenRuns;
	
	public AutoPlayer(GameEngine gameEngine, GamePlayClientService gamePlayClientService, SpaceshipGame game, int autoPlaySleepTime, boolean sleepInBetweenRuns){
		this.gameEngine = gameEngine;
		this.gamePlayClientService = gamePlayClientService;
		this.game = game;
		this.autoPlaySleepTime = autoPlaySleepTime;
		this.sleepInBetweenRuns = sleepInBetweenRuns;
	}
	
	public void run() {
		String gameId = game.getGameId();
		log.debug("Staring autoplay for game: "+game);
		if(game.isActive()){
			SalvoImpact salvoImpact = null;
			ResponseEntity<SalvoImpact> fireSalvoResponseEntity =null;
			while(true){
				SpaceshipGame game = this.gameEngine.fetchGame(gameId);
				boolean turnSelf = game.isTurnSelf();
				if(!turnSelf){
					log.debug("Getting opponent to fire back on self: "+gameId);
					fireSalvoResponseEntity = this.gamePlayClientService.haveOpponentFireBackOnSelf(gameId);
					salvoImpact = fireSalvoResponseEntity.getBody();
				}else{
					log.debug("Firing on opponent: "+gameId);
					fireSalvoResponseEntity = this.gamePlayClientService.fireSalvoOnOpponent(gameId);
					salvoImpact = fireSalvoResponseEntity.getBody();
				}
				
				if(!StringUtils.isEmpty(salvoImpact.getGame().getWon())){
					log.debug("Game up, stopping autoplay for: "+gameId);
					break;
				}
				
				if(sleepInBetweenRuns){
					try {
						// Slow down to prevent overwhelming buffers
						Thread.sleep(autoPlaySleepTime);
					} catch (InterruptedException ie) {
						log.error("Woken from sleep: "+ie);
					}
				}
			}
		}
		log.debug("Exiting autoplay: "+gameId);
	}
}
