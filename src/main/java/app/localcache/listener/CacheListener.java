package app.localcache.listener;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_UPDATED;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.INITIALIZED;

public class CacheListener {
    private static final Logger logger = LoggerFactory.getLogger(CacheListener.class);
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private static final String NAMESPACE = "local-cache";
    private final CuratorFramework zkClient;
    private CuratorCache curatorCache;

    public CacheListener(String host) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(5000, 3);
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(host)
                .retryPolicy(retryPolicy)
                .namespace(NAMESPACE)
                .build();
        zkClient.start();
    }

    public void listen(String path, LambdaCallback callback) throws InterruptedException {
        logger.info("cache listener is connecting to zookeeper");
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                .forPathChildrenCache(NAMESPACE, zkClient, (client, event) -> handleDataChange(event, path, callback))
                .build();
        curatorCache = CuratorCache.builder(zkClient, path).build();
        curatorCache.listenable().addListener(curatorCacheListener);
        curatorCache.start();
        countDownLatch.await();
    }

    private void handleDataChange(PathChildrenCacheEvent event, String path, LambdaCallback callback) {
        countDownLatch.countDown();
        if (event.getType() == INITIALIZED) {
            logger.info("cache listener initialized");
        } else if (event.getType() == CHILD_UPDATED) {
            boolean isSubscribedPath = event.getData().getPath().equals(path);
            if (isSubscribedPath) {
                logger.info("data updated, path={}, data={}", path, new String(event.getData().getData()));
                callback.execute();
            }
        }
    }

    public void close() {
        logger.info("cache listener is shutting down");
        curatorCache.close();
        zkClient.close();
    }
}
