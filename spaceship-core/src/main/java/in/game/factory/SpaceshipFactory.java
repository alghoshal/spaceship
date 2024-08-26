package in.game.factory;

import org.springframework.stereotype.Component;

import in.game.model.Spaceship;
import in.game.model.SpaceshipOrientation;

/**
 * Builds spaceships
 * @author aghoshal
 */
@Component
public interface SpaceshipFactory {
	/**
	 * Creates a winger ship as per the orientation:
	 * Facing East & West:
	 * 	* *
	 *  * *
	 * 	 *
	 *  * *
	 *  * *
	 *  
	 *  Facing North & South:
	 *  ** **
	 *    *  
	 *  ** **
	 * @param spaceshipOrientation
	 */
	public Spaceship createWinger(SpaceshipOrientation spaceshipOrientation);
	
	/**
	 * Creates Angle ship as per the orientation:
	 * 	*
	 * 	*
	 * 	*
	 * 	****
	 * 
	 * @param spaceshipOrientation
	 * @return
	 */
	public Spaceship createAngle(SpaceshipOrientation spaceshipOrientation);
	
	/**
	 * Creates A-class as per the orientation:
	 * 
	 *   *
	 *  * *
	 *  ***
	 *  * *
	 *  
	 * @param spaceshipOrientation
	 * @return
	 */
	public Spaceship createAClass(SpaceshipOrientation spaceshipOrientation);
		
	
	/**
	 * Creates B-class as per the orientation:
	 * 
	 *  **
	 *  * *
	 *  **
	 *  * *
	 *  ** 
	 *  
	 * @param spaceshipOrientation
	 * @return
	 */
	public Spaceship createBClass(SpaceshipOrientation spaceshipOrientation);
	
	/**
	 * Create S-class as per the orientation:
	 * 
	 *   **
	 *  *
	 *   **
	 *     *
	 *   **
	 *   
	 * @param spaceshipOrientation
	 * @return
	 */
	public Spaceship createSClass(SpaceshipOrientation spaceshipOrientation);
}
