package in.game.controller.helper;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import in.game.game.GameEngine;
import in.game.model.GameMetaInfo;
import in.game.model.SpaceshipProtocol;
import in.game.model.User;

/**
 * Helpers for controller
 * 
 * @author aghoshal
 */
public class ControllerHelper {
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	GameEngine gameEngine;

	@Value("${spring.cloud.client.hostname}")
	String hostName;

	@Value("${server.port}")
	String port;

	@Value("${spring.application.instance_id}")
	String instanceId;
	
	@Value("${spaceship.user.full_name}")
	String userFullName;
	
	
	/**
	 * True if opponent is different from self
	 * @param opponent
	 * @param self
	 * @return
	 */
	public boolean isOpponentDifferentFromSelf(User opponent, User self) {
		return !self.getSpaceshipProtocol().equals(opponent.getSpaceshipProtocol());
	}
	
	/**
	 * Loads user self
	 * @return
	 */
	public User fetchSelf() {
		return new User(this.instanceId, this.userFullName, new SpaceshipProtocol(this.hostName, this.port));
	}
	
	/**
	 * Prepares message body
	 * 
	 * @param message
	 * @return
	 */
	public <T> HttpEntity<T> prepareRequestBody(T message, MediaType mediaType, Class<T> responseType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		return new HttpEntity<T>(message, headers);
	}
	
	/**
	 * 
	 * @param response
	 * @param newGameResponse
	 */
	public HttpServletResponse prepare303Response(HttpServletResponse response, GameMetaInfo newGameResponse, String spaceshipUserEndpointBase) {
		response.setStatus(HttpServletResponse.SC_SEE_OTHER);
		response.setHeader("Location", spaceshipUserEndpointBase + newGameResponse.getGameId());
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		response.setCharacterEncoding(null);
		return response;
	}
}
