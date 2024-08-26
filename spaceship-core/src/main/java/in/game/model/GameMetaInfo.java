package in.game.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds meta-info about the game 
 * 
 * @author aghoshal
 */
public class GameMetaInfo {
	
	public GameMetaInfo(String userId, String fullName, String gameId, String starting){
		this.userId = userId;
		this.fullName = fullName;
		this.gameId = gameId;
		this.starting = starting;
	}
	
	public GameMetaInfo(){}
	
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("full_name")
	String fullName;
	
	@JsonProperty("game_id")
	String gameId;
	
	@JsonProperty("starting")
	String starting;
	
	@JsonProperty("player_turn")
	String playerTurn;
	
	@JsonProperty("rules")
	String rules;
	
	@JsonProperty("won")
	String won;
	
	@JsonIgnore
	HttpStatus gameHttpResponseStatus;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getStarting() {
		return starting;
	}

	public void setStarting(String starting) {
		this.starting = starting;
	}
	
	public String getPlayerTurn() {
		return playerTurn;
	}

	public void setPlayerTurn(String playerTurn) {
		this.playerTurn = playerTurn;
	}

	public String getRules() {
		return rules;
	}

	public void setRules(String rules) {
		this.rules = rules;
	}
	
	public String getWon() {
		return won;
	}

	public void setWon(String won) {
		this.won = won;
	}
	
	public HttpStatus getGameHttpResponseStatus() {
		return gameHttpResponseStatus;
	}

	public void setGameHttpResponseStatus(HttpStatus gameHttpResponseStatus) {
		this.gameHttpResponseStatus = gameHttpResponseStatus;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
