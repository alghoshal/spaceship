package in.game.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NewGameRequest {
	User self, opponent;
	
	@JsonIgnore
	String gameId;

	@JsonIgnore
	String playerTurn;

	public NewGameRequest(){}
	
	public NewGameRequest(User self, User opponent){
		this.self = self;
		this.opponent = opponent;
	}
	
	public User getSelf() {
		return self;
	}

	public void setSelf(User self) {
		this.self = self;
	}

	public User getOpponent() {
		return opponent;
	}

	public void setOpponent(User opponent) {
		this.opponent = opponent;
	}
	
	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getPlayerTurn() {
		return playerTurn;
	}

	public void setPlayerTurn(String playerTurn) {
		this.playerTurn = playerTurn;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
