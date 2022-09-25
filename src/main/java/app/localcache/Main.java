package app.localcache;

import app.localcache.cache.LocalCache;
import app.localcache.listener.CacheListener;
import app.localcache.mock.MockService;

public class Main {
    public static void main(String[] args) {
        CacheListener cacheListener = new CacheListener("localhost:2181");
        LocalCache localCache = new LocalCache();
        new MockService(localCache).execute();
        Runtime.getRuntime().addShutdownHook(new Thread(cacheListener::close));
        cacheListener.listen("/operation-config", localCache::evictAll);
    }
}
