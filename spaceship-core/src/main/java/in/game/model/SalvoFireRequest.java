package in.game.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Captures a Salvo fire
 * 
 * @author aghoshal
 */
public class SalvoFireRequest {
	String[] salvo;
	User self;
	
	public SalvoFireRequest(){}
	
	public SalvoFireRequest(String[] salvos, User self){
		this.salvo = salvos;
		this.self = self;
	}
	
	public String[] getSalvo() {
		return salvo;
	}
	public void setSalvo(String[] salvos) {
		this.salvo = salvos;
	}
	public User getSelf() {
		return self;
	}
	public void setSelf(User self) {
		this.self = self;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
