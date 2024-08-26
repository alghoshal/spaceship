package in.game.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import in.game.game.GameEngine;
import in.game.model.SpaceshipGame;

@Service
public class GameAutoPlayServiceImpl implements GameAutoPlayService{
	private static final Logger log = LoggerFactory.getLogger(GameAutoPlayServiceImpl.class);

	@Autowired
	GameEngine gameEngine;
	
	@Autowired
	GamePlayClientService gamePlayClientService;
	
	@Autowired
	ThreadPoolTaskExecutor autoPlayThreadPoolTaskExecutor;
	
	@Value("${auto.play.sleep.time}")
	int autoPlaySleepTime;
	
	@Value("${auto.play.sleep.inbetween}")
	boolean sleepInBetweenRuns;
	
	/**
	 * @see in.game.service.GameAutoPlayService#setUpAutoRun(java.lang.String)
	 */
	public boolean setUpAutoRun(String gameId) {
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		if(game.isActive() && game.isAutoplayOn()){
			log.debug("Autoplay already on for game: "+game);
			return true;
		}
		
		game.setAutoplayOn(true);
		game = this.gameEngine.updateGame(game);

		log.debug("Adding game for autoplay: "+game.getGameId());
		AutoPlayer player = new AutoPlayer(gameEngine, gamePlayClientService, game, autoPlaySleepTime, sleepInBetweenRuns);
		autoPlayThreadPoolTaskExecutor.execute(player);
		
		return true;
	}

	/**
	 * @see in.game.service.GameAutoPlayService#autoPlayUnderway()
	 */
	public Collection<SpaceshipGame> autoPlayUnderway() {
		return this.gameEngine.fetchAll();
	}

	/**
	 * @see in.game.service.GameAutoPlayService#isAutoPlayOn(java.lang.String)
	 */
	public boolean isAutoPlayOn(String gameId) {
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		return null!=game && game.isActive() && game.isAutoplayOn();
	}
}
