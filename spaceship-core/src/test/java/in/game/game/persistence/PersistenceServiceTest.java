package in.game.game.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import in.game.model.SpaceshipGame;

public class PersistenceServiceTest {
	
	PersistenceService<SpaceshipGame> cache;
	SpaceshipGame game;
	String gameId="123";
	
	@Before
	public void setUp(){
		cache = new InMemoryCache<SpaceshipGame>();
		game= new SpaceshipGame();
		game.setGameId(gameId);
	}
	
	@Test
	public void testFetchKey(){
		
		String key="abc";
		cache.save(key, game);
		assertEquals(game,cache.lookUpByKey(key));
		
		assertNull(cache.lookUpByKey("InvalidKey"));
	}

	
	@Test
	public void testGetAll(){
		
		String key="abc";
		cache.save(key, game);
		String key2="key2";
		SpaceshipGame game1 = new SpaceshipGame();
		cache.save(key2, game1);

		Collection<SpaceshipGame> games = cache.getAll();
		assertEquals(2,games.size());
		SpaceshipGame[] gamesArr = new SpaceshipGame[games.size()]; 
		gamesArr = games.toArray(gamesArr);
		
		assertEquals(game1, gamesArr[0]);
		assertEquals(game, gamesArr[1]);
	}

}
