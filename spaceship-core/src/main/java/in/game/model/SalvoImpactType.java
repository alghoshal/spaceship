package in.game.model;

/**
 * Salvo impact types
 * @author aghoshal
 */
public enum SalvoImpactType {
	MISS("miss",'-'),HIT("hit",'X'),EMPTY(" ",'.'),BODY("body",'*'),KILL("kill",'X');
	
	String impactName;
	char impactValue;
	
	SalvoImpactType(String impactName, char impactValue){
		this.impactName = impactName;
		this.impactValue = impactValue;
	}
	
	public String getImpactName(){
		return impactName;
	}
	
	public char getImpactValue(){
		return impactValue;
	}
}