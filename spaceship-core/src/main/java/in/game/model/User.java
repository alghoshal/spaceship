package in.game.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	@JsonProperty("user_id")
	String userId;
	
	@JsonProperty("full_name")
	String fullName;
	
	@JsonProperty("spaceship_protocol")
	SpaceshipProtocol spaceshipProtocol;

	char[][] board;
	
	@JsonProperty("rules")
	String rules;
	
	public User(){}
	
	public User(String userId,String fullName,SpaceshipProtocol spaceshipProtocol){
		this.userId = userId;
		this.fullName = fullName;
		this.spaceshipProtocol = spaceshipProtocol;
	}
	
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

	public SpaceshipProtocol getSpaceshipProtocol() {
		return spaceshipProtocol;
	}

	public void setSpaceshipProtocol(SpaceshipProtocol spaceshipProtocol) {
		this.spaceshipProtocol = spaceshipProtocol;
	}
	
	public char[][] getBoard() {
		return board;
	}

	public void setBoard(char[][] board) {
		this.board = board;
	}

	public String getRules() {
		return rules;
	}

	public void setRules(String rules) {
		this.rules = rules;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
