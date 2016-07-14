package com.sunll.stu;

import org.ehcache.PersistentCacheManager;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import java.net.URI;

/**
 * Created by sunll on 16/7/13.
 */
public class ClusterTest {

    public static void test() {

        final CacheManagerBuilder<PersistentCacheManager> clusteredCacheManagerBuilder =
                CacheManagerBuilder.newCacheManagerBuilder()
                        .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9510/my-application")).autoCreate()
                                .defaultServerResource("primary-server-resource")
                                .resourcePool("resource-pool-a", 28, MemoryUnit.MB, "secondary-server-resource")
                                .resourcePool("resource-pool-b", 32, MemoryUnit.MB))
                        .withCache("clustered-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredDedicated("primary-server-resource", 32, MemoryUnit.MB))))
                        .withCache("shared-cache-1", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredShared("resource-pool-a"))))
                        .withCache("shared-cache-2", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .with(ClusteredResourcePoolBuilder.clusteredShared("resource-pool-a"))));
        final PersistentCacheManager cacheManager = clusteredCacheManagerBuilder.build(true);

        cacheManager.close();
    }

}
