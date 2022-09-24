package app.localcache.mock;

import app.localcache.cache.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MockService {
    private static final Logger logger = LoggerFactory.getLogger(MockService.class);
    private final LocalCache localCache;

    public MockService(LocalCache localCache) {
        this.localCache = localCache;
    }

    public void execute() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            AtomicInteger counter = new AtomicInteger();
            while (true) {
                String value = localCache.get("test-key", () -> "test-value" + counter.incrementAndGet());
                logger.info("cache_key=test-key, cache_value={}, cache_version={}", value, counter.get());
                Thread.sleep(1000);
            }
        });
    }
}
