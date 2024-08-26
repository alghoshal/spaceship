package in.game.model.view;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import in.game.model.User;

/**
 * Transformed user view object
 * 
 * @author aghoshal
 */
@JsonIgnoreProperties({ "player_turn", "full_name", "spaceship_protocol", "rules", "shipDestroyed" })
public class UserView {
	
	String userId;
	char[][] board;
	
	public UserView() {
	}

	public UserView(User user) {
		if(null!=user){
			setUserId(user.getUserId());
			setBoard(user.getBoard());
		}
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public char[][] getBoard() {
		return board;
	}

	public void setBoard(char[][] board) {
		this.board = board;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}