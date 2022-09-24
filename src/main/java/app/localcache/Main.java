package app.localcache;

import app.localcache.cache.LocalCache;
import app.localcache.listener.CacheListener;
import app.localcache.mock.MockService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CacheListener cacheListener = new CacheListener("localhost:2181");
        LocalCache localCache = new LocalCache();
        MockService mockService = new MockService(localCache);
        mockService.execute();

        try {
            cacheListener.listen("/operation-config", localCache::evictAll);
        } finally {
            cacheListener.close();
        }
    }
}
