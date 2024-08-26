package in.game.game;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import in.game.game.helpers.SpaceshipGameBuilder;
import in.game.game.persistence.PersistenceService;
import in.game.model.GameMetaInfo;
import in.game.model.NewGameRequest;
import in.game.model.SalvoFireRequest;
import in.game.model.SalvoImpact;
import in.game.model.SpaceshipGame;
import in.game.model.view.GameView;

@Service
public class GameEngineImpl implements GameEngine {
	private static final Logger log = LoggerFactory.getLogger(GameEngineImpl.class);

	@Autowired
	SpaceshipGameBuilder gameBuilder;

	@Autowired
	PersistenceService<SpaceshipGame> persistenceService;
	
	@Value("${spaceship.validate.player.turn}")
	boolean validatePlayerTurn;
	
	@Value("${spaceship.validate.salvo.count}")
	boolean validateSalvoCount;

	/**
	 * @see in.game.game.GameEngine#setUpNewGame(in.game.model.NewGameRequest)
	 */
	public GameMetaInfo setUpNewGame(NewGameRequest newGameRequest) {
		SpaceshipGame game = gameBuilder.buid(newGameRequest);
		if(null==game){
			log.debug("Something wrong, unable to build game");
			GameMetaInfo gameMetaInfo = new GameMetaInfo();
			gameMetaInfo.setGameHttpResponseStatus(HttpStatus.BAD_REQUEST);
			return gameMetaInfo;
		}
		log.debug("Persisting game to cache: " + game);
		this.persistenceService.save(game.getGameId(), game);
		GameMetaInfo gameMetaInfo = prepareGameSnapshot(game, newGameRequest);
		gameMetaInfo.setGameHttpResponseStatus(HttpStatus.CREATED);
		return gameMetaInfo;
	}

	/**
	 * @see in.game.game.GameEngine#fetchGame(java.lang.String)
	 */
	public SpaceshipGame fetchGame(String gameId) {
		return this.persistenceService.lookUpByKey(gameId);
	}

	/**
	 * @see in.game.game.GameEngine#fetchAl()
	 */
	public Collection<SpaceshipGame> fetchAll() {
		return this.persistenceService.getAll();
	}

	/**
	 * @see in.game.game.GameEngine#fetchGameView(java.lang.String)
	 */
	public GameView fetchGameView(String gameId) {
		SpaceshipGame game = fetchGame(gameId);
		log.debug("Fetched game: " + game);
		if (null == game) {
			log.error("Game not found for gameId: "+gameId); 
			return null;
		}

		GameView gameView = new GameView(game);
		log.debug("Transformed GameView: " + gameView);
		return gameView;
	}

	/**
	 * @see in.game.game.GameEngine#handleSalvoFire(java.lang.String,
	 *      java.util.List)
	 */
	public SalvoImpact handleSalvoFire(String gameId, SalvoFireRequest salvo) {
		log.debug("Handling salvo fire on self: " + gameId);
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		SalvoImpact salvoImpact = null;
		if (null == game) {
			salvoImpact = setUpSalvoImpactForError("Invalid game id: " + gameId, HttpStatus.NOT_FOUND);
		} else if (!game.isActive()) {
			log.debug("Game no longer active, all salvos missed: " + game.getGameId());
			salvoImpact = game.updateAllSalvosAsMisses(setUpSalvoImpactForError(null, 
					HttpStatus.NOT_FOUND),
					salvo.getSalvo());
		} else if(validateSalvoCount && (game.getGameRules().getNumberOfShots(game,false)!=salvo.getSalvo().length)){
			salvoImpact = setUpSalvoImpactForError(game.getGameId()+", wrong number of shots in salvos, must fire exactly: " 
					+ game.getGameRules().getNumberOfShots(game,false)+" shots, while shots fired: "+salvo.getSalvo().length, 
					HttpStatus.BAD_REQUEST);
		} else if(validatePlayerTurn && game.isTurnSelf()){
			salvoImpact = setUpSalvoImpactForError(game.getGameId()+", wrong end point turn of self: " + game.getSelf().getUserId(), 
					HttpStatus.BAD_REQUEST);
		}
		else {
			log.debug("Handling salvo fired on self for game: " + game.getGameId());
			salvoImpact = game.handleSalvoFireOnSelf(salvo.getSalvo());
			GameMetaInfo snapshot = new GameMetaInfo();
			if (game.getSelfOnBoardEntitiesAlive() > 0) {
				snapshot.setPlayerTurn(salvo.getSelf().getUserId());
				game.flipTurn();
			} else {
				String wonBy = game.getOpponent().getUserId();
				log.debug("Game over, won by:" + wonBy);
				game.setActive(false);
				game.setWon(wonBy);
				snapshot.setWon(wonBy);
			}

			salvoImpact.setGame(snapshot);
			salvoImpact.setResponseStatus(HttpStatus.OK);

			// Update game
			persistenceService.save(game.getGameId(), game);
		}
		return salvoImpact;
	}

	/**
	 * @see in.game.game.GameEngine#captureSalvoImpactOnOpponent(java.lang.String,
	 *      in.game.model.SalvoImpact)
	 */
	public SalvoImpact captureSalvoImpactOnOpponent(String gameId, SalvoImpact salvoResponseOpponent) {
		log.debug("Capturing salvo impact on opponent ");
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		SalvoImpact salvoImpact = game.captureSalvoImpactOnOpponent(salvoResponseOpponent);
		String wonBy = salvoResponseOpponent.getGame().getWon();
		if(!StringUtils.isEmpty(wonBy)){
			log.debug("Game over, won by:" + wonBy);
			game.setWon(wonBy);
			game.setActive(false);
		}else game.flipTurn();

		persistenceService.save(gameId, game);
		return salvoImpact;
	}

	/**
	 * @see in.game.game.GameEngine#isValidGame(java.lang.String)
	 */
	public boolean isValidGame(String gameId) {
		SpaceshipGame game = this.persistenceService.lookUpByKey(gameId);
		return null != game && game.isActive();
	}

	/**
	 * @see in.game.game.GameEngine#updateGame(in.game.model.SpaceshipGame)
	 */
	public SpaceshipGame updateGame(SpaceshipGame game) {
		log.debug("Saving game ");
		return this.persistenceService.save(game.getGameId(), game);
	}

	/**
	 * 
	 * @param game
	 * @return
	 */
	GameMetaInfo prepareGameSnapshot(SpaceshipGame game, NewGameRequest newGameRequest) {
		GameMetaInfo gameSnapshot = new GameMetaInfo();
		gameSnapshot.setFullName(game.getSelf().getFullName());
		gameSnapshot.setGameId(game.getGameId());
		gameSnapshot.setUserId(game.getSelf().getUserId());
		gameSnapshot.setStarting(game.getPlayerTurn());

		if (null != newGameRequest.getOpponent() && !StringUtils.isEmpty(newGameRequest.getOpponent().getRules()))
			gameSnapshot.setRules(game.getGameRules().getName());
		return gameSnapshot;
	}
	
	/**
	 * Sets up error details in SalvoImpact
	 * @param salvoImpact
	 * @param errorMessage
	 * @return
	 */
	SalvoImpact setUpSalvoImpactForError(String errorMessage, HttpStatus responseStatus) {
		SalvoImpact salvoImpact = new SalvoImpact();
		log.error(errorMessage);
		salvoImpact.setErrorMessage(errorMessage);
		salvoImpact.setErrorFlag(true);
		salvoImpact.setResponseStatus(responseStatus);
		return salvoImpact;
	}
}
