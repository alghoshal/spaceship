package in.game.factory;

import org.springframework.stereotype.Component;

import in.game.model.Spaceship;
import in.game.model.SpaceshipOrientation;
import in.game.model.SpaceshipType;

@Component
public class SpaceshipFactoryImpl implements SpaceshipFactory {
	
	/**
	 * @see in.game.factory.SpaceshipFactory#createWinger(in.game.model.SpaceshipOrientation)
	 */
	public Spaceship createWinger(SpaceshipOrientation spaceshipOrientation) {
		// Base orientation Facing_East = Facing_West
		char[][] shape = { { '*', '.', '*' }, { '*', '.', '*' }, { '.', '*', '.' }, { '*', '.', '*' },
				{ '*', '.', '*' } };
		if (SpaceshipOrientation.FACING_NORTH.equals(spaceshipOrientation)
				|| SpaceshipOrientation.FACING_SOUTH.equals(spaceshipOrientation)) {
			// One turn right Facing_North = Facing_South
			shape = rotateRight(shape);
		}
		return new Spaceship(SpaceshipType.WINGER, shape, spaceshipOrientation);
	}
	
	/**
	 * @see in.game.factory.SpaceshipFactory#createAngle(in.game.model.SpaceshipOrientation)
	 */
	public Spaceship createAngle(SpaceshipOrientation spaceshipOrientation){
		// Base orientation Facing_East
		char[][] shape   = 
			{		{ '*', '.', '.' }, 
					{ '*', '.', '.' }, 
					{ '*', '.', '.' }, 
					{ '*', '*', '*' }
			};
		if(!SpaceshipOrientation.FACING_EAST.equals(spaceshipOrientation)){
			shape = reorientShip(spaceshipOrientation, shape);
		}
		return new Spaceship(SpaceshipType.ANGLE, shape, spaceshipOrientation);
	}

	/**
	 * @see in.game.factory.SpaceshipFactory#createAClass(in.game.model.SpaceshipOrientation)
	 */
	public Spaceship createAClass(SpaceshipOrientation spaceshipOrientation) {
		// Base orientation Facing_East
		char[][] shape   = 
			{		{ '.', '*', '.' }, 
					{ '*', '.', '*' }, 
					{ '*', '*', '*' }, 
					{ '*', '.', '*' }
			};
		if(!SpaceshipOrientation.FACING_EAST.equals(spaceshipOrientation)){
			shape = reorientShip(spaceshipOrientation, shape);
		}
		return new Spaceship(SpaceshipType.A_CLASS, shape, spaceshipOrientation);
	}
	
	/**
	 * @see in.game.factory.SpaceshipFactory#createBClass(in.game.model.SpaceshipOrientation)
	 */
	public Spaceship createBClass(SpaceshipOrientation spaceshipOrientation) {
		// Base orientation Facing_East
		char[][] shape   = 
			{		{ '*', '*', '.' }, 
					{ '*', '.', '*' }, 
					{ '*', '*', '.' }, 
					{ '*', '.', '*' },
					{ '*', '*', '.' }
			};
		if(!SpaceshipOrientation.FACING_EAST.equals(spaceshipOrientation)){
			shape = reorientShip(spaceshipOrientation, shape);
		}
		return new Spaceship(SpaceshipType.B_CLASS, shape, spaceshipOrientation);
	}
	
	/**
	 * @see in.game.factory.SpaceshipFactory#createSClasss(in.game.model.SpaceshipOrientation)
	 */
	public Spaceship createSClass(SpaceshipOrientation spaceshipOrientation) {
		// Base orientation Facing_East
		char[][] shape   = 
			{		{ '.', '*', '*','.' }, 
					{ '*', '.', '.','.' }, 
					{ '.', '*', '*','.' }, 
					{ '.', '.', '.' ,'*'},
					{ '.', '*', '*','.' }
			};
		if(!SpaceshipOrientation.FACING_EAST.equals(spaceshipOrientation)){
			shape = reorientShip(spaceshipOrientation, shape);
		}
		return new Spaceship(SpaceshipType.S_CLASS, shape, spaceshipOrientation);
	}

	
	/**
	 * Reorients shape as specified
	 * @param spaceshipOrientation
	 * @param shape
	 * @return
	 */
	char[][] reorientShip(SpaceshipOrientation spaceshipOrientation, char[][] shape) {
		if (SpaceshipOrientation.FACING_SOUTH.equals(spaceshipOrientation)) {
			// One turn right
			shape = rotateRight(shape);
		}
		else if (SpaceshipOrientation.FACING_WEST.equals(spaceshipOrientation)) {
			// Two turns right
			shape = rotateRight(rotateRight(shape));
		}
		else if (SpaceshipOrientation.FACING_NORTH.equals(spaceshipOrientation)){
			// Three turns right
			shape = rotateRight(rotateRight(rotateRight(shape)));
		}
		return shape;
	}

	/**
	 * Rotates the shape left
	 * @param shape
	 * @return
	 */
	char[][] rotateRight(char[][] shape) {
		return  swapColumns(transpose(shape));
	}

	/**
	 * Swaps every column->i with column->(m-i),
	 * of a given nXm 2d matrix
	 * @param transposed
	 * @return
	 */
	char[][] swapColumns(char[][] transposed) {
		char[][] rotatedShape = new char[transposed.length][transposed[0].length];
		int i,j;
		int m = transposed[0].length;
		for (i=0;i<transposed.length;i++) {
			for(j=0;j<transposed[0].length;j++){
				rotatedShape[i][j] = transposed[i][m-j-1];
			}
		}
		return rotatedShape;
	}

	/**
	 * Transposes the matrix
	 * @param shape
	 * @return
	 */
	char[][] transpose(char[][] shape) {
		char[][] rotatedShape = new char[shape[0].length][shape.length];
		int i,j;
		for (i=0;i<shape.length;i++) {
			for(j=0;j<shape[0].length;j++){
				rotatedShape[j][i] = shape[i][j];
			}
		}
		return rotatedShape;
	}

}
