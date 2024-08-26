package in.game.game.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.game.factory.SpaceshipFactory;
import in.game.game.rules.GameRules;
import in.game.game.rules.GameRulesFactory;
import in.game.model.SpaceshipGame;
import in.game.model.NewGameRequest;
import in.game.model.Spaceship;
import in.game.model.SpaceshipOrientation;
import in.game.model.User;

@Component
public class SpaceshipGameBuilder {
	private static final Logger log = LoggerFactory.getLogger(SpaceshipGameBuilder.class);

	@Autowired
	SpaceshipFactory factory;

	public SpaceshipGameBuilder() {}

	/**
	 * Builds a new game
	 * 
	 * @param self
	 * @param opponent
	 * @return
	 */
	public SpaceshipGame buid(NewGameRequest newGameRequest) {
		User self = newGameRequest.getSelf();
		User opponent = newGameRequest.getOpponent();
		SpaceshipGame newGame = new SpaceshipGame(self, opponent);

		// Assign gameId
		if (!StringUtils.isEmpty(newGameRequest.getGameId())) newGame.setGameId(newGameRequest.getGameId());

		// Assign spaceships
		newGame = assignSpaceshipsToBoard(fetchSpaceShips(), newGame);
		
		// Assign playerTurn
		newGame.setTurnSelf(getStartingPlayer(newGameRequest, newGame));
		
		// Assign rules
		if(isGameRulesMentioned(newGameRequest)) {
			newGame.setRulesMentioned(true);
			GameRules rules = GameRulesFactory.fetchRule(newGameRequest.getOpponent().getRules());
			if(null==rules) {
				log.error("Invalid rule mentioned, unable to set-up game");
				return null;
			}
			newGame.setGameRules(rules);
		}
		return newGame;
	}
	

	/**
	 * true if game rules have been mentioned
	 * @param newGameRequest
	 * @return
	 */
	boolean isGameRulesMentioned(NewGameRequest newGameRequest) {
		return null!=newGameRequest.getOpponent() && !StringUtils.isEmpty(newGameRequest.getOpponent().getRules());
	}

	/**
	 * Assigns spaceships to random location on the board
	 * 
	 * @param spaceshipsSet
	 * @param game
	 * @return
	 */
	SpaceshipGame assignSpaceshipsToBoard(List<Spaceship> spaceshipsSet, SpaceshipGame game) {
		Random r = new Random();
		int boardSize = game.getBoardSelf().length;
		for (Spaceship spaceship : spaceshipsSet) {
			while (!game.updateBoard(game.getBoardSelf(), spaceship.getShape(),
					r.nextInt(boardSize - spaceship.getShape().length),
					r.nextInt(boardSize - spaceship.getShape()[0].length), spaceship)) {
			}
		}
		
		// Both players play with an equal no. of ships
		game.setOpponentOnBoardEntitiesAlive(game.getSelfOnBoardEntitiesAlive());
		return game;
	}

	/**
	 * Setups spaceships for the user
	 * 
	 * @return
	 */
	List<Spaceship> fetchSpaceShips() {
		Random r = new Random();
		SpaceshipOrientation[] spaceshipOrientations = SpaceshipOrientation.values();
		int noOfSpaceships = spaceshipOrientations.length;

		return Arrays.asList(factory.createWinger(spaceshipOrientations[r.nextInt(noOfSpaceships)]),
				factory.createAngle(spaceshipOrientations[r.nextInt(noOfSpaceships)]),
				factory.createAClass(spaceshipOrientations[r.nextInt(noOfSpaceships)]),
				factory.createBClass(spaceshipOrientations[r.nextInt(noOfSpaceships)]),
				factory.createSClass(spaceshipOrientations[r.nextInt(noOfSpaceships)]));
	}

	/**
	 * Picks one of the starting players at random
	 * 
	 * @param game
	 * @return
	 */
	boolean getStartingPlayer(NewGameRequest newGameRequest, SpaceshipGame game) {
		log.debug("Player turn mentioned: "+newGameRequest.getPlayerTurn());
		if(!StringUtils.isEmpty(newGameRequest.getPlayerTurn())) 
			return game.getSelf().getUserId().equals(newGameRequest.getPlayerTurn());
		
		return new Random().nextInt(2) == 0;
	}
}
