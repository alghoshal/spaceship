package in.game.service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.game.controller.BaseController;
import in.game.controller.helper.ControllerHelper;
import in.game.game.GameEngine;
import in.game.model.SpaceshipGame;
import in.game.model.GameMetaInfo;
import in.game.model.NewGameRequest;
import in.game.model.SalvoImpact;
import in.game.model.User;
import in.game.service.helper.GamePlayClientHelper;

/**
 * Game play client, makes calls to other game end-points
 * 
 * @author aghoshal
 */
@Service
public class GamePlayClientImpl  implements GamePlayClientService {
	public static final String EMPTY = "";

	private static final Logger log = LoggerFactory.getLogger(GamePlayClientImpl.class);
	
	@Autowired
	GameEngine gameEngine;
	
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ControllerHelper controllerHelper;
	
	@Autowired
	GamePlayClientHelper gamePlayClientHelper;
	
	@Value("${spaceship.protocol.handle.salvo.fire}")
	String spaceshipProtocolEndpointHandleSalvoFire;
	
	@Value("${spaceship.protocol.fire.back.self}")
	String spaceshipProtocolEndpointFireBackOnSelf;

	@Value("${spring.application.name}")
	String applicationName;
	
	@Value("${spaceship.protocol.user.base}")
	String spaceshipUserEndpointBase;
	
	@Value("${spaceship.validate.player.turn}")
	boolean validatePlayerTurn;

	public static final MediaType JSON_MEDIA_TYPE_UTF8 = new MediaType("application","json",Charset.forName("UTF-8"));
	
	public ResponseEntity<SalvoImpact> fireSalvoOnOpponent(String gameId, String[] salvo) {
		log.debug("Firing salvo on opponent gameId: " + gameId + ", salvo: " + salvo);

		// Handle invalid game
		if (!this.gameEngine.isValidGame(gameId)) {
			String message = "Game not found for gameId: "+gameId;
			log.error(message); 
			SalvoImpact salvoImpact = gamePlayClientHelper.prepareSalvoImpactInError(message);
			return new ResponseEntity<SalvoImpact>(salvoImpact,HttpStatus.NOT_FOUND);
		} 

		SpaceshipGame game = this.gameEngine.fetchGame(gameId);
		if(validatePlayerTurn&&!game.isTurnSelf()){
			String message = game.getGameId()+", wrong end point turn of the other player: " + game.getOpponent().getUserId();
			log.error(message);
			SalvoImpact salvoImpact = gamePlayClientHelper.prepareSalvoImpactInError(message);
			return new ResponseEntity<SalvoImpact>(salvoImpact,HttpStatus.BAD_REQUEST);
		}
		
		String fireSalvoGameEndpoint = game.getOpponent().getSpaceshipProtocol().getUri() + spaceshipProtocolEndpointHandleSalvoFire;
		log.debug("Firing salvo on game endpoint: " + fireSalvoGameEndpoint);
		Map<String, String[]> salvoMap = new HashMap<String, String[]>();
		salvoMap.put(BaseController.SALVO_FIRE_KEY, salvo);
		ResponseEntity<SalvoImpact> salvoResponseEntity = restTemplate.exchange(fireSalvoGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(salvoMap, JSON_MEDIA_TYPE_UTF8, Map.class), SalvoImpact.class,
				gameId);
		
		if(salvoResponseEntity.getStatusCode().is2xxSuccessful()){
			SalvoImpact salvoResponseOpponent = salvoResponseEntity.getBody();
			log.debug("Response from opponent's endpoint: " + salvoResponseOpponent);
	
			// Capture the impact on own/ self board
			SalvoImpact salvoResponseSelf = this.gameEngine.captureSalvoImpactOnOpponent(gameId, salvoResponseOpponent);
			log.debug("Updated game board of opponent: " + salvoResponseSelf);
		}
		
		return salvoResponseEntity;
	}
	
	/**
	 * @see in.game.service.GamePlayClientService#fireSalvoOnOpponent(java.lang.String)
	 */
	public ResponseEntity<SalvoImpact> fireSalvoOnOpponent(String gameId) {
		// Handle invalid game
		if (!this.gameEngine.isValidGame(gameId)) {
			String message = "Game not found for gameId: "+gameId;
			log.error(message); 
			return new ResponseEntity<SalvoImpact>(HttpStatus.NOT_FOUND);
		}
		
		log.debug("Game valid, now fetching salvo");
		SpaceshipGame game = this.gameEngine.fetchGame(gameId);

		String[] salvo = game.getSalvoSet();
		log.debug(salvo+", firing salvo on opponent for game:" + game);
		return fireSalvoOnOpponent(gameId,salvo);
	}
	
	/**
	 * @see in.game.service.GamePlayClientService#haveOpponentFireBackOnSelf(java.lang.String)
	 */
	public ResponseEntity<SalvoImpact> haveOpponentFireBackOnSelf(String gameId) {
		log.debug("Getting opponent to fire salvo back on self for gameId: " + gameId);
		String fireBackOnSelfGameEndpoint = this.gameEngine.fetchGame(gameId).getOpponent().getSpaceshipProtocol().getUri() 
				+ spaceshipProtocolEndpointFireBackOnSelf;

		log.debug("Fire back salvo game endpoint: " + fireBackOnSelfGameEndpoint);
		ResponseEntity<SalvoImpact> firebackResponseEntity = restTemplate.exchange(fireBackOnSelfGameEndpoint, HttpMethod.PUT,
				controllerHelper.prepareRequestBody(EMPTY, JSON_MEDIA_TYPE_UTF8, String.class), 
				SalvoImpact.class, gameId);
		log.debug(" Fire back response from opponent: " + firebackResponseEntity.getBody());
		
		return firebackResponseEntity;
	}
	
	/**
	 * @see in.game.service.GamePlayClientService#setUpNewGameWithOpponent(javax.servlet.http.HttpServletResponse, in.game.model.User, in.game.model.User, java.lang.String)
	 */
	public GameMetaInfo setUpNewGameWithOpponent(HttpServletResponse response, User self, User opponent, String newGameEndPoint) {
		log.debug("Starting new game on game endpoint: " + newGameEndPoint);
		ResponseEntity<GameMetaInfo> newGameResponseEntity = restTemplate.exchange(newGameEndPoint,
				HttpMethod.POST,
				controllerHelper.prepareRequestBody(self, JSON_MEDIA_TYPE_UTF8, User.class),
				GameMetaInfo.class);

		GameMetaInfo newGameResponse = newGameResponseEntity.getBody();
		log.debug("Response from new game endpoint: " + newGameResponse);

		if (newGameResponseEntity.getStatusCode().is2xxSuccessful()) {
			// Send 303 response on success
			response = controllerHelper.prepare303Response(response, newGameResponse, spaceshipUserEndpointBase);
		} else {
			log.error("Request for game creation failed, received resp. code: "
					+ newGameResponseEntity.getStatusCode());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		gamePlayClientHelper.captureGameImage(opponent, newGameResponse);
		return newGameResponse;
	}
	
}
