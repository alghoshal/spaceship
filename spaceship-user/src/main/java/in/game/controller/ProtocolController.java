package in.game.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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
import in.game.model.NewGameRequest;
import in.game.model.SalvoFireRequest;
import in.game.model.SalvoImpact;
import in.game.model.User;
import in.game.service.helper.ServiceLocatorHelper;

/**
 * Protocol api end-point
 * 
 * @author aghoshal
 */
@Controller
@Configuration
@Import({ ServiceLocatorHelper.class, ControllerHelper.class })
public class ProtocolController extends BaseController implements ProtocolApi{
	private static final Logger log = LoggerFactory.getLogger(ProtocolController.class);

	/**
	 * @see in.game.controller.ProtocolApi#startNewGame(in.game.model.User)
	 */
	@RequestMapping(path = "/spaceship/protocol/game/new", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<GameMetaInfo> startNewGame(@RequestBody User opponent) {
		log.debug("Setting up new game for opponent " + opponent);
		GameMetaInfo newGameResponse = this.gameEngine.setUpNewGame(new NewGameRequest(controllerHelper.fetchSelf(), opponent));
		log.debug("New game setup response: " + newGameResponse);
		return new ResponseEntity<GameMetaInfo>(newGameResponse,newGameResponse.getGameHttpResponseStatus());
	}

	/**
	 * @see in.game.controller.ProtocolApi#handleSalvoFireFromOpponent(java.lang.String, java.util.Map)
	 */
	@RequestMapping(path = "/spaceship/protocol/game/{gameId}", method = RequestMethod.PUT, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<SalvoImpact> handleSalvoFireFromOpponent(@PathVariable String gameId,
			@RequestBody Map<String, String[]> salvo) {
		SalvoImpact salvoImpact = this.gameEngine.handleSalvoFire(gameId,new SalvoFireRequest(salvo.get(SALVO_FIRE_KEY), controllerHelper.fetchSelf()));
		log.debug("Response from handle salvo fire engine: " + salvoImpact);
		return new ResponseEntity<SalvoImpact>(salvoImpact,salvoImpact.getResponseStatus());
	}
	
	
	/**
	 * @see in.game.controller.ProtocolApi#fireBackOnOpponent(java.lang.String)
	 */
	@RequestMapping(path = "/spaceship/protocol/game/{gameId}/fire-back", method = RequestMethod.PUT, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<SalvoImpact> fireBackOnOpponent(@PathVariable String gameId) {
		log.debug("Initiating fire back on opponent for game: " + gameId);
		return this.gamePlayClientService.fireSalvoOnOpponent(gameId);
	}
}
