package app.localcache.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class LocalCache {
    private static final Logger logger = LoggerFactory.getLogger(LocalCache.class);
    private final Cache<String, String> cache;

    public LocalCache() {
        cache = CacheBuilder.newBuilder().build();
    }

    public String get(String key, Callable<String> loader) {
        try {
            return cache.get(key, loader);
        } catch (ExecutionException e) {
            throw new RuntimeException("get cache failed", e);
        }
    }

    public void evictAll() {
        logger.info("cache evicted");
        cache.invalidateAll();
    }
}
