package problemas.redesocial;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache LRU de posts populares (maximo 50 en memoria).
 * LinkedHashMap en modo acceso + removeEldestEntry = LRU clasico.
 * Se protege con synchronized porque LinkedHashMap no es concurrente
 * y queremos que la eviction sea consistente.
 */
public class CacheLRU {

    private static final int CAPACIDAD = 50;

    private final Map<String, Post> cache = new LinkedHashMap<>(CAPACIDAD, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Post> eldest) {
            return size() > CAPACIDAD;
        }
    };

    public void put(Post post) {
        synchronized (cache) {
            cache.put(post.getId(), post);
        }
    }

    public Post get(String id) {
        synchronized (cache) {
            return cache.get(id);
        }
    }

    public int size() {
        synchronized (cache) {
            return cache.size();
        }
    }

    public double tasaAcierto(long peticiones, long aciertos) {
        return peticiones == 0 ? 0 : (aciertos * 100.0 / peticiones);
    }
}
