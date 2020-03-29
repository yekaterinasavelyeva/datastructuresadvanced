package roadgraph;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import geography.GeographicPoint;

public class SimpleCacheManager {
	private static SimpleCacheManager instance;
	private static Object monitor = new Object();
	private Map<AbstractMap.SimpleImmutableEntry<GeographicPoint, GeographicPoint>, List<GeographicPoint>> cache = Collections.synchronizedMap(new HashMap<>());

	private SimpleCacheManager() {
	}

	public void put(AbstractMap.SimpleImmutableEntry<GeographicPoint, GeographicPoint> cacheKey, List<GeographicPoint> value) {
		cache.put(cacheKey, value);
		System.out.println("*********** Info put to cache ***********");
	}

	public List<GeographicPoint> get(AbstractMap.SimpleImmutableEntry<GeographicPoint, GeographicPoint> cacheKey) {
		return cache.get(cacheKey);
	}

	public void clear(AbstractMap.SimpleImmutableEntry<GeographicPoint, GeographicPoint> cacheKey) {
		cache.put(cacheKey, null);
	}

	public void clear() {
		cache.clear();
	}

	public static SimpleCacheManager getInstance() {
		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new SimpleCacheManager();
				}
			}
		}
		return instance;
	}
}
