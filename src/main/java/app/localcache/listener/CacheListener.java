package app.localcache.listener;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;

public class CacheListener {
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private static final String NAMESPACE = "local-cache";
    private final CuratorFramework zkClient;

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
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                .forPathChildrenCache(NAMESPACE, zkClient, (client, event) -> {
                    if (event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED && event.getData().getPath().equals(path)) {
                        callback.execute();
                    }
                }).build();
        CuratorCache curatorCache = CuratorCache.builder(zkClient, path).build();
        curatorCache.listenable().addListener(curatorCacheListener);
        curatorCache.start();
        countDownLatch.await();
    }

    public void close() {
        zkClient.close();
    }
}
