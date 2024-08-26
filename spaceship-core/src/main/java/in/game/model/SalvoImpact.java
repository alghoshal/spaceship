package in.game.model;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SalvoImpact {
	
	Map<String, String> salvo;
	GameMetaInfo game;
	
	@JsonProperty("error_message")
	String errorMessage;
	
	@JsonIgnore
	boolean errorFlag;
	
	@JsonIgnore
	HttpStatus responseStatus;
	
	public Map<String, String> getSalvo() {
		return salvo;
	}
	public void setSalvo(Map<String, String> salvo) {
		this.salvo = salvo;
	}
	public GameMetaInfo getGame() {
		return game;
	}
	public void setGame(GameMetaInfo game) {
		this.game = game;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public boolean isErrorFlag() {
		return errorFlag;
	}
	public void setErrorFlag(boolean errorFlag) {
		this.errorFlag = errorFlag;
	}
	public HttpStatus getResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(HttpStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}	
}
