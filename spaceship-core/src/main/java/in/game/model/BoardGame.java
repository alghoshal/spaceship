package in.game.model;

import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.game.game.rules.GameRules;
import in.game.game.rules.GameRulesFactory;

/**
 * Captures any 2D board game.
 * The (X,Y) co-ordinates of the game 
 * are represented as hexadecimal digits.
 * 
 * @author aghoshal
 */
public class BoardGame {
	@JsonProperty("game_id")
	String gameId;
	
	User self, opponent;
	boolean turnSelf=true;
	boolean active=true;
	boolean autoplayOn;
	String won;
	GameRules gameRules;
	boolean rulesMentioned;
	int selfOnBoardEntitiesAlive;
	int opponentOnBoardEntitiesAlive;
	boolean opponentOnBoardEntityDestroyed;
	boolean selfOnBoardEntityDestroyed;
	
	public BoardGame(){}
	
	public BoardGame(User self, User opponent, int gridSizeX, int gridSizeY){
		this.self = self;
		this.opponent = opponent;
		char[][] boardSelf= new char[gridSizeX][gridSizeY];
		this.self.setBoard(boardSelf);
		char[][] boardOpponent= new char[gridSizeX][gridSizeY];
		this.opponent.setBoard(boardOpponent);
		this.gameId = UUID.randomUUID().toString();
		this.gameRules = GameRulesFactory.fetchRule(GameRules.STANDARD); 
	}

	/**
	 * Resets values on the board
	 * @param board
	 */
	public void resetBoard(char[][] board, char value) {
		for (int i = 0; i < board.length; i++){
			for (int j = 0; j < board[i].length; j++){
				board[i][j]=value;
			}
		}
	}

	public BoardGame(User player1, User player2){
		this(player1,player2,16,16);
	}
	
	/**
	 * Does hex-> int conversion
	 * 
	 * @param input
	 * @return
	 */
	Pair<Integer, Integer> parseCoordinates(String input){
		String[] splits = input.split("x");
		Integer xCoord = Integer.valueOf(splits[0],16);
		Integer yCoord = Integer.valueOf(splits[1],16);
		
		return new Pair<Integer, Integer>(xCoord, yCoord);
	}
	
	@JsonProperty("player_turn")
	public String getPlayerTurn() {
		if(StringUtils.isEmpty(won)){
			User currentPlayer = (turnSelf)?self:opponent;
			return currentPlayer.getUserId();
		}
		return null;
	}
	
	public void flipTurn(){
		setTurnSelf(getGameRules().flipTurn(this));
	}

	public void setTurnSelf(boolean turnSelf) {
		this.turnSelf = turnSelf;
	}
	
	public boolean isTurnSelf() {
		return turnSelf;
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

	public char[][] getBoardSelf() {
		return this.getSelf().getBoard();
	}


	public char[][] getBoardOpponent() {
		return this.opponent.getBoard();
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public String getWon() {
		return won;
	}

	public void setWon(String won) {
		this.won = won;
	}

	public boolean isAutoplayOn() {
		return autoplayOn;
	}

	public void setAutoplayOn(boolean autoplayOn) {
		this.autoplayOn = autoplayOn;
	}
	
	public GameRules getGameRules() {
		return gameRules;
	}

	public void setGameRules(GameRules rules) {
		this.gameRules = rules;
	}
	
	public boolean isRulesMentioned() {
		return rulesMentioned;
	}

	public void setRulesMentioned(boolean rulesMentioned) {
		this.rulesMentioned = rulesMentioned;
	}
	
	public int getSelfOnBoardEntitiesAlive() {
		return selfOnBoardEntitiesAlive;
	}

	public void setSelfOnBoardEntitiesAlive(int selfOnBoardEntitiesAlive) {
		this.selfOnBoardEntitiesAlive = selfOnBoardEntitiesAlive;
	}

	public int getOpponentOnBoardEntitiesAlive() {
		return opponentOnBoardEntitiesAlive;
	}

	public void setOpponentOnBoardEntitiesAlive(int opponentOnBoardEntitiesAlive) {
		this.opponentOnBoardEntitiesAlive = opponentOnBoardEntitiesAlive;
	}
	
	public boolean isOpponentOnBoardEntityDestroyed() {
		return opponentOnBoardEntityDestroyed;
	}

	public void setOpponentOnBoardEntityDestroyed(boolean opponentOnBoardEntityDestroyed) {
		this.opponentOnBoardEntityDestroyed = opponentOnBoardEntityDestroyed;
	}
	
	public boolean isSelfOnBoardEntityDestroyed() {
		return selfOnBoardEntityDestroyed;
	}

	public void setSelfOnBoardEntityDestroyed(boolean selfOnBoardEntityDestroyed) {
		this.selfOnBoardEntityDestroyed = selfOnBoardEntityDestroyed;
	}

	/**
	 * @param assignedBoard
	 * @return
	 */
	public String printBoard(char[][] assignedBoard ){
		StringBuilder builder = new StringBuilder();
		for (char[] cs : assignedBoard) {
			builder.append("\n");
			for (char c : cs) {
				builder.append(c+" ");
			}
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
