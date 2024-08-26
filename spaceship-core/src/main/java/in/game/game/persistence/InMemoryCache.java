package in.game.game.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists to an in-memory cache
 * 
 * @author aghoshal
 *
 * @param <T>
 */
public class InMemoryCache<T> implements PersistenceService<T> {

	public Map<String, T> cache;
	
	public InMemoryCache(){
		this.cache= new ConcurrentHashMap<String,T>();
	}
	
	/**
	 * @see in.game.game.persistence.PersistenceService#save(java.lang.String, java.lang.Object)
	 */
	public T save(String key, T object) {
		return this.cache.put(key, object);
	}

	/**
	 * @see in.game.game.persistence.PersistenceService#lookUpByKey(java.lang.String)
	 */
	public T lookUpByKey(String key) {
		return this.cache.get(key);
	}

	/**
	 * @see in.game.game.persistence.PersistenceService#getAll()
	 */
	public Collection<T> getAll() {
		return this.cache.values();
	}
}
