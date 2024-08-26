package in.game.controller.helper;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import in.game.model.GameMetaInfo;

public class ControllerHelperTest{
	ControllerHelper controllerHelper;
	
	String spaceshipUserEndpointBase="/spaceship/user/game/";

	@Before
	public void setUp() {
		controllerHelper = new ControllerHelper();
	}
	
	@Test
	public void testPing(){
		String gameId = "123";
		MockHttpServletResponse response = new MockHttpServletResponse();
		GameMetaInfo newGameResponse = new GameMetaInfo();
		newGameResponse.setGameId(gameId);;
		HttpServletResponse responseResult = controllerHelper.prepare303Response(response, newGameResponse, "/spaceship/user/game/");
		
		assertEquals(spaceshipUserEndpointBase+gameId,responseResult.getHeader("Location"));
		assertEquals(HttpServletResponse.SC_SEE_OTHER,responseResult.getStatus());
		assertEquals(MediaType.TEXT_HTML_VALUE, responseResult.getContentType());
		assertEquals(null,responseResult.getCharacterEncoding());
	}
	
	@Test
	public void testPrepareRequestBody(){
		String message = "something";
		HttpEntity<String> responseEntity = 
				controllerHelper.prepareRequestBody(message,  MediaType.APPLICATION_JSON, String.class);
		assertEquals(message, responseEntity.getBody());
		assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
	}
}
