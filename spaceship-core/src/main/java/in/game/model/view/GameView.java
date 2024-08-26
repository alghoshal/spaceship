package in.game.model.view;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.game.model.SpaceshipGame;
import in.game.model.GameMetaInfo;

/**
 * Transformed game view object
 * 
 * @author aghoshal
 */
public class GameView {

	@JsonProperty("game")
	GameMetaInfo gameMetaInfo;
	
	UserView self, opponent;
	
	public GameView() {}

	public GameView(SpaceshipGame game) {
		if (null != game) {
			setSelf(new UserView(game.getSelf()));
			setOpponent(new UserView(game.getOpponent()));
			this.gameMetaInfo = new GameMetaInfo();
			this.gameMetaInfo.setPlayerTurn(game.getPlayerTurn());
			this.gameMetaInfo.setWon(game.getWon());
			this.gameMetaInfo.setRules(game.isRulesMentioned()?game.getGameRules().getName():null);
		}
	}

	public GameMetaInfo getGameMetaInfo() {
		return gameMetaInfo;
	}

	public void setGameMetaInfo(GameMetaInfo gameSnapshot) {
		this.gameMetaInfo = gameSnapshot;
	}

	public UserView getSelf() {
		return self;
	}

	public void setSelf(UserView self) {
		this.self = self;
	}

	public UserView getOpponent() {
		return opponent;
	}

	public void setOpponent(UserView opponent) {
		this.opponent = opponent;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
