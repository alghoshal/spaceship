package in.game.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * @author aghoshal
 */
public class Spaceship implements OnBoardEntity{
	SpaceshipType spaceshipType;
	char[][] shape;
	SpaceshipOrientation orientation;
	
	/**
	 * @see in.game.model.OnBoardEntity#getEntityId()
	 */
	public int getEntityId() {
		return spaceshipType.ordinal();
	}
	
	public Spaceship(SpaceshipType spaceshipType, char[][] shape, SpaceshipOrientation orientation){
		this.spaceshipType = spaceshipType;
		this.shape = shape;
		this.orientation = orientation;
	}
	
	public Spaceship(SpaceshipType spaceshipType, char[][] shape){
		this(spaceshipType, shape, SpaceshipOrientation.FACING_EAST);
	}

	public void reOrient(SpaceshipOrientation orientation){}
	
	public char[][] getShape() {
		return shape;
	}
	public void setShape(char[][] shape) {
		this.shape = shape;
	}
	public SpaceshipOrientation getOrientation() {
		return orientation;
	}
	public void setOrientation(SpaceshipOrientation orientation) {
		this.orientation = orientation;
	}
	
	public SpaceshipType getSpaceshipType() {
		return spaceshipType;
	}

	public void setSpaceshipType(SpaceshipType spaceshipType) {
		this.spaceshipType = spaceshipType;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public String printShape(){
		StringBuilder builder = new StringBuilder(spaceshipType.name());
		builder.append(" - ").append(orientation).append("\n");
		for (char[] cs : shape) {
			for (char c : cs) {
				builder.append(c);
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}
}
