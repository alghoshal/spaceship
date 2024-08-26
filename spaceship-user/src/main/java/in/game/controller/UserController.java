package in.game.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import in.game.controller.helper.ControllerHelper;
import in.game.model.GameMetaInfo;
import in.game.model.SalvoImpact;
import in.game.model.User;
import in.game.model.view.GameView;
import in.game.service.helper.ServiceLocatorHelper;

/**
 * User api end-point
 * 
 * @author aghoshal
 */
@Controller
@Import({ ServiceLocatorHelper.class, ControllerHelper.class })
public class UserController extends BaseController implements UserApi{
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	/**
	 * @see in.game.controller.UserApi#fireSalvoOnOpponent(java.lang.String, java.util.Map)
	 */
	@RequestMapping(path = "/spaceship/user/game/{gameId}/fire", method = RequestMethod.PUT, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<SalvoImpact> fireSalvoOnOpponent(@PathVariable String gameId, @RequestBody Map<String,String[]> salvo) {
		log.debug("Firing salvo on opponent gameId: " + gameId + ", salvo: " + salvo);
		return this.gamePlayClientService.fireSalvoOnOpponent(gameId, salvo.get(SALVO_FIRE_KEY));
	}

	/**
	 * @see in.game.controller.UserApi#runAutoPilot(javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@RequestMapping(path = "/spaceship/user/game/{gameId}/auto", method = RequestMethod.POST)
	public void runAutoPilot(HttpServletResponse response, @PathVariable String gameId) {
		log.debug("Setting up AutoPilot for: " + gameId);
		
		// Ensure game is valid
		if(!this.gameEngine.isValidGame(gameId)){
			log.error("Invalid gameId: "+gameId);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// Set-up auto-pilot
		this.autoPlayService.setUpAutoRun(gameId);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @see in.game.controller.UserApi#fetchGame(java.lang.String)
	 */
	@RequestMapping(path = "/spaceship/user/game/{gameId}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<GameView> fetchGame(@PathVariable String gameId) {
		log.debug("Fetching details of gameId: " + gameId);
		GameView gameView = this.gameEngine.fetchGameView(gameId);
		return new ResponseEntity<GameView>(gameView,(null!=gameView)?HttpStatus.OK:HttpStatus.NOT_FOUND);
	}

	/**
	 * @see in.game.controller.UserApi#createNewGame(javax.servlet.http.HttpServletResponse, in.game.model.User)
	 */
	@RequestMapping(path = "/spaceship/user/game/new", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.TEXT_HTML_VALUE })
	public void createNewGame(HttpServletResponse response, @RequestBody User opponent) {
		log.debug("Staring new game for user: " + opponent);

		User self = controllerHelper.fetchSelf();
		
		// Validate opponent != self
		boolean isValidOpponent = controllerHelper.isOpponentDifferentFromSelf(opponent, self);
		if (!isValidOpponent) {
			log.debug("Invalid opponent, same as self ");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		// Validate opponent is reachable
		isValidOpponent = serviceLocatorHelper.isServiceUp(applicationName,
				opponent.getSpaceshipProtocol().getHostname(), opponent.getSpaceshipProtocol().getPort());
		if (!isValidOpponent) {
			log.debug("Invalid opponent, unreachable on hostname/port");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		// Send new game request
		self.setRules(opponent.getRules());
		GameMetaInfo newGameResponse = gamePlayClientService.setUpNewGameWithOpponent(response, self, opponent, 
				opponent.getSpaceshipProtocol().getUri() + spaceshipProtocolEndpointNewGame);
		log.debug("New game set-up response: " + newGameResponse);

	}
	
	@RequestMapping(path = "/ping", method = {RequestMethod.GET})
	@ResponseBody
	public ResponseEntity<String> ping() {
		return new ResponseEntity<String>("pong",HttpStatus.OK);
	}
	
	@RequestMapping(path = "/ping-2", method = {RequestMethod.PUT})
	@ResponseBody
	public ResponseEntity<String> ping(@RequestBody String body) {
		log.debug("Body contains: "+body);
		return new ResponseEntity<String>("pong"+body,HttpStatus.OK);
	}
}
