package in.game.game.persistence;

import java.util.Collection;

import org.springframework.stereotype.Service;

/**
 * Provides persistence services
 * @author aghoshal
 */
@Service
public interface PersistenceService<T> {
	/**
	 * Persists object
	 * @param object
	 * @return
	 */
	T save(String key, T object);
	
	/**
	 * Returns the persisted instance
	 * @param key
	 * @return
	 */
	T lookUpByKey(String key);
	
	/**
	 * Returns all instances persisted
	 * @return
	 */
	Collection<T> getAll();

}
