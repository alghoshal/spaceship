package in.game.service;

import java.util.Collection;

import in.game.model.SpaceshipGame;

/**
 * Sets up game for autoplay
 * 
 * @author aghoshal
 */
public interface GameAutoPlayService {
	/**
	 * Sets up auto play
	 * @param gameId
	 * @return
	 */
	boolean setUpAutoRun(String gameId);
	
	/**
	 * Listing of all games currently in autoplay
	 * @return
	 */
	Collection<SpaceshipGame> autoPlayUnderway();
	
	/**
	 * True if autoplay is underway for game
	 * @param gameId
	 * @return
	 */
	boolean isAutoPlayOn(String gameId);
}
