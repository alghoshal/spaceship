package in.game.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import in.game.model.Spaceship;
import in.game.model.SpaceshipOrientation;

public class SpaceShipFactoryImplTest {
	private static final Logger log = LoggerFactory.getLogger(SpaceShipFactoryImplTest.class);
	
	SpaceshipFactory factory;
	
	@Before
	public void setUp(){
		factory = new SpaceshipFactoryImpl();
	}
	
	@Test
	public void testCreateWingerEastWestOrientation(){
		Spaceship winger = factory.createWinger(SpaceshipOrientation.FACING_EAST);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		assertEquals(5,shape.length);
		assertEquals(3,shape[0].length);
		log.debug(winger.printShape());
		
		winger = factory.createWinger(SpaceshipOrientation.FACING_WEST);
		assertNotNull(winger);
		shape = winger.getShape();
		assertEquals(5,shape.length);
		assertEquals(3,shape[0].length);
		log.debug(winger.printShape());
	}
	
	@Test
	public void testCreateWingerNorthSouthOrientation(){
		Spaceship winger = factory.createWinger(SpaceshipOrientation.FACING_SOUTH);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(5,shape[0].length);
	}
	
	@Test
	public void testCreateAngle(){
		Spaceship winger = factory.createAngle(SpaceshipOrientation.FACING_SOUTH);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(4,shape[0].length);
		
		winger = factory.createAngle(SpaceshipOrientation.FACING_WEST);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(4,shape.length);
		assertEquals(3,shape[0].length);
		
		winger = factory.createAngle(SpaceshipOrientation.FACING_NORTH);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(4,shape[0].length);
		
	}
	
	@Test
	public void testCreateAClass(){
		Spaceship winger = factory.createAClass(SpaceshipOrientation.FACING_SOUTH);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(4,shape[0].length);
		
		winger = factory.createAClass(SpaceshipOrientation.FACING_WEST);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(4,shape.length);
		assertEquals(3,shape[0].length);
		
		winger = factory.createAClass(SpaceshipOrientation.FACING_NORTH);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(4,shape[0].length);
		
	}
	
	@Test
	public void testCreateBClass(){
		Spaceship winger = factory.createBClass(SpaceshipOrientation.FACING_SOUTH);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(5,shape[0].length);
		
		winger = factory.createBClass(SpaceshipOrientation.FACING_WEST);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(5,shape.length);
		assertEquals(3,shape[0].length);
		
		winger = factory.createBClass(SpaceshipOrientation.FACING_NORTH);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(3,shape.length);
		assertEquals(5,shape[0].length);
		
	}
	
	@Test
	public void testCreateSClass(){
		Spaceship winger = factory.createSClass(SpaceshipOrientation.FACING_SOUTH);
		assertNotNull(winger);
		char[][] shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(4,shape.length);
		assertEquals(5,shape[0].length);
		
		winger = factory.createSClass(SpaceshipOrientation.FACING_WEST);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(5,shape.length);
		assertEquals(4,shape[0].length);
		
		winger = factory.createSClass(SpaceshipOrientation.FACING_NORTH);
		assertNotNull(winger);
		shape = winger.getShape();
		log.debug(winger.printShape());
		assertEquals(4,shape.length);
		assertEquals(5,shape[0].length);
		
	}
	
}
