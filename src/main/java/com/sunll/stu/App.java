package com.sunll.stu;

import com.sunll.stu.model.Person;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.xml.XmlConfiguration;


import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App {

    public static  void testMethod1() {
        CacheManager cacheManager
                = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)))
                .build();//build 可传入boolean参数 true 代表配置并初始化(即init()方法)
        cacheManager.init();// 在使用cache前必须调用init()方法,除非.build(true)的情况

        Cache<Long, String> preConfigured =
                cacheManager.getCache("preConfigured", Long.class, String.class);

        Cache<Long, String> myCache = cacheManager.createCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)).build());

        myCache.put(1L, "da one!");
        String value = myCache.get(1L);

        cacheManager.removeCache("preConfigured");

        cacheManager.close();
    }


    /**
     *  UserCacherManager
     */
    public static void testUserManagedCache() {
        UserManagedCache<Long, String> userManagedCache =
                UserManagedCacheBuilder.newUserManagedCacheBuilder(Long.class, String.class)
                        .build(false);
        userManagedCache.init();

        userManagedCache.put(1L, "da one!");

        String value = userManagedCache.get(1L);

        userManagedCache.close();
    }


    /**
     * offheap
     * 	If you wish to use off-heap, you’ll have to define a resource pool, giving the memory size you want to allocate.
     *
     *  The example above allocates a very small amount of off-heap.
     *  Remember that data stored off-heap will have to be serialized and deserialized - and is thus slower than heap.
     *  You should thus favor off-heap for large amounts of data where on-heap would have too severe an impact on garbage collection.
     *
     * 	Do not forget to define in the java options the -XX:MaxDirectMemorySize option, according to the off-heap size you intend to use.
     */
    public static void testOffhead() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().withCache("tieredCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, EntryUnit.ENTRIES)
                                .offheap(10, MemoryUnit.MB))
        )
                .build(true);

        cacheManager.close();
    }


    /**
     *  disk persistence
     */
    public static void testDiskPeristence() {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence("/" + File.separator + "myData"))
                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, EntryUnit.ENTRIES)
                                .disk(10, MemoryUnit.MB, true))
                )
                .build(true);

        persistentCacheManager.close();
    }


    /**
     * tree tiers
     */
    public static void testTreeTiers() {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence("/" + File.separator + "myData"))
                .withCache("threeTieredCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, EntryUnit.ENTRIES)// define a resource pool for heap
                                        .offheap(1, MemoryUnit.MB)// define a reource pool for off-heap
                                        .disk(20, MemoryUnit.MB)// define a resource pool for the disk
                        )
                ).build(true);

        persistentCacheManager.close();
    }


    /**
     * Byte-sized heap
     * You can also size the heap tier using memory units instead of entry count.
     */
    public static void testByteSized() {
        CacheConfiguration<Long, String> usesConfiguredInCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(10, MemoryUnit.KB) // You can also size the heap tier in bytes.
                                                 // This will limit the amount of heap used by that tier for storing key-value pairs.
                                                 //Note that there is a cost associated to sizing objects.
                        .offheap(10, MemoryUnit.MB))
                .withSizeOfMaxObjectGraph(1000)
                .withSizeOfMaxObjectSize(1000, MemoryUnit.B) // The sizing mechanism can be configured along two axis:
                                                             // The first one specifies the maximum number of objects to traverse while walking the object graph,
                                                             //the second defines the maximum size of a single object.
                                                             // If the sizing goes above any of these two limits, the mutative operation on the cache will be ignored.
                .build();

        CacheConfiguration<Long, String> usesDefaultSizeOfEngineConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                        .heap(10, MemoryUnit.KB)
                        .offheap(10, MemoryUnit.MB))
                .build();

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withDefaultSizeOfMaxObjectSize(500, MemoryUnit.B)
                .withDefaultSizeOfMaxObjectGraph(2000) // A default configuration can be provided at CacheManager level to be used by the caches unless defined explicitly
                .withCache("usesConfiguredInCache", usesConfiguredInCacheConfig)
                .withCache("usesDefaultSizeOfEngine", usesDefaultSizeOfEngineConfig)
                .build(true);

        Cache<Long, String> usesConfiguredInCache = cacheManager.getCache("usesConfiguredInCache", Long.class, String.class);

        usesConfiguredInCache.put(1L, "one");
        //assertThat(usesConfiguredInCache.get(1L), equalTo("one"));

        Cache<Long, String> usesDefaultSizeOfEngine = cacheManager.getCache("usesDefaultSizeOfEngine", Long.class, String.class);

        usesDefaultSizeOfEngine.put(1L, "one");
        //assertThat(usesDefaultSizeOfEngine.get(1L), equalTo("one"));

        cacheManager.close();
    }


    /**
     * 数据刷新
     */
    public static void testDataFreshness() {
        CacheConfiguration<Long, String> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                ResourcePoolsBuilder.heap(100))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(20, TimeUnit.SECONDS)))// 设置过期时间
                .build();

        CacheConfiguration<String, Person> cachePersonConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Person.class,
                ResourcePoolsBuilder.heap(100))
                .withExpiry(Expirations.timeToLiveExpiration(Duration.of(20, TimeUnit.SECONDS)))// 设置过期时间
                .build();



        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("persionCache", cachePersonConfiguration)
                .build(true);

        Cache<String, Person> cachePerson = cacheManager.getCache("persionCache", String.class, Person.class);

        cachePerson.put("sun", new Person(20, "long"));

        Person person = cachePerson.get("sun");
        System.out.println(person.getAge());
    }



    public static void testxml() {
        final URL myUrl = App.class.getClass().getResource("/test-ehcache.xml");
        Configuration xmlConfig = new XmlConfiguration(myUrl);
        CacheManager myCacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        myCacheManager.init();

        Cache<String, String> fooCache = myCacheManager.getCache("foo", String.class, String.class);

        fooCache.put("test-key", "hahhahaha");

        Object value = fooCache.get("test-key");
        System.out.println(value);
    }


    public static void main( String[] args ) {

        //testMethod1();
        //testUserManagedCache();

        //testxml();

        testDataFreshness();

    }
}
