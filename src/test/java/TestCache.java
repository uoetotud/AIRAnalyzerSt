import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.util.Cache;

public class TestCache {
	
	@SuppressWarnings("rawtypes")
	static Cache<String, CacheItem> cache = null;

	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test Cache ###");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		System.out.println("\n### End Test Cache ###");
		System.out.println("=====================================================");
	}
	
	@Before
	public void setUp() {}

	@After
	public void tearDown() {
		cache = null;
		System.out.println("-----------------------------------------------------");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testGetPutRemoveContainsSizeFunctions() throws InterruptedException {
		System.out.println("\n** Test Get/Put/Remove/Contains/Size functions **");
		cache = new Cache<String, CacheItem>("LRU", 10, 10, 5);
		assertEquals(cache.size(), 0);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		assertNotNull(cache.get("key3"));
		assertTrue(cache.contains("key1"));
		System.out.println(cache.size() + " cache items added.");
		
		// Item 4, 5 added
		Thread.sleep(2000);		
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
		assertTrue(cache.contains("key4"));
		System.out.println("2 more cache items added. Cache size: " + cache.size());
			
		// Item 4 removed
		Thread.sleep(2000);
		cache.remove("key4");
		assertEquals(cache.size(), 4);
		assertNull(cache.get("key4"));
		assertFalse(cache.contains("key4"));
		System.out.println("1 cache item removed. Cache size: " + cache.size());
		
		// Item 6 added
		Thread.sleep(2000);
		cache.put("key6", new CacheItem("value6", System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
		assertTrue(cache.contains("key6"));
		System.out.println("1 cache items added. Cache size: " + cache.size());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testCacheItemExpire() throws InterruptedException {
		System.out.println("\n** Test cache item expires **");
		cache = new Cache<String, CacheItem>("LRU", 2, 3, 5);
		
		// Item 1, 2 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		assertEquals(cache.size(), 2);
		System.out.println(cache.size() + " cache items added.");
		
		// Item 1, 2 expired
		Thread.sleep(5000);
		assertEquals(cache.size(), 0);
		System.out.println("Cache size: " + cache.size());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testLruCache() throws InterruptedException {
		System.out.println("\n** Test LRU cache **");
		cache = new Cache<String, CacheItem>("LRU", 10, 5, 3);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(cache.size() + " cache items added.");
		
		// Timestamp of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.fetch("key2");
		assertNotNull(ci2);
		System.out.println("'key2' cache item accessed, timestamp: " + ci2.getTimeStamp());
		
		Thread.sleep(1000);
		CacheItem ci1 = (CacheItem) cache.fetch("key1");
		assertNotNull(ci1);
		System.out.println("'key1' cache item accessed, timestamp: " + ci1.getTimeStamp());
		
		Thread.sleep(1000);
		CacheItem ci3 = (CacheItem) cache.fetch("key3");
		assertNotNull(ci3);
		System.out.println("'key3' cache item accessed, timestamp: " + ci3.getTimeStamp());
		
		assertTrue(ci3.getTimeStamp() > ci1.getTimeStamp());
		assertTrue(ci1.getTimeStamp() > ci2.getTimeStamp());
		
		// Item 4 added - replace item 2
		Thread.sleep(1000);
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println("'key4' cache item added.");

		CacheItem ci4 = (CacheItem) cache.fetch("key4");
		assertNotNull(ci4);
		System.out.println("'key4' cache item accessed, timestamp: " + ci4.getTimeStamp());
		ci2 = (CacheItem) cache.get("key2");
		assertNull(ci2);
		
		// Item 5 added - replace item 1
		Thread.sleep(1000);
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println("'key4' cache item added.");
		
		CacheItem ci5 = (CacheItem) cache.fetch("key5");
		assertNotNull(ci5);
		System.out.println("'key5' cache item accessed, timestamp: " + ci5.getTimeStamp());
		ci1 = (CacheItem) cache.get("key1");
		assertNull(ci1);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testLfuCache() throws InterruptedException {
		System.out.println("\n** Test LFU cache **");
		cache = new Cache<String, CacheItem>("LFU", 15, 5, 3);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(cache.size() + " cache items added.");
		
		// HitCount of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.fetch("key2");
		assertNotNull(ci2);
		assertEquals(ci2.getHitCount(), 2);
		System.out.println("'key2' cache item accessed, hitCount: " + ci2.getHitCount());
		
		int i = 0;
		Thread.sleep(1000);
		CacheItem ci1 = null;
		for (i=0; i<2; i++) {
			ci1 = (CacheItem) cache.fetch("key1");
			System.out.println("'key1' cache item accessed, hitCount: " + ci1.getHitCount());
		}
		assertNotNull(ci1);
		assertEquals(ci1.getHitCount(), 3);
		
		Thread.sleep(1000);
		CacheItem ci3 = null;
		for (i=0; i<3; i++) {
			ci3 = (CacheItem) cache.fetch("key3");
			System.out.println("'key3' cache item accessed, hitCount: " + ci3.getHitCount());
		}
		assertNotNull(ci3);
		assertEquals(ci3.getHitCount(), 4);

		// Item 4 added - replace item 2
		Thread.sleep(1000);
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println("'key4' cache item added.");

		CacheItem ci4 = (CacheItem) cache.fetch("key4");
		assertNotNull(ci4);
		System.out.println("'key4' cache item accessed, hitCount: " + ci4.getHitCount());
		ci2 = (CacheItem) cache.get("key2");
		assertNull(ci2);
		
		// Item 5 added - replace item 4
		Thread.sleep(1000);
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println("'key5' cache item added.");
		
		CacheItem ci5 = (CacheItem) cache.fetch("key5");
		assertNotNull(ci5);
		System.out.println("'key5' cache item accessed, hitCount: " + ci5.getHitCount());
		ci4 = (CacheItem) cache.get("key4");
		assertNull(ci4);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Test
	public void testConcurrency() throws InterruptedException {
		System.out.println("\n** Test concurrency support **");
		
		System.out.println("Test 1000000 cache items with 1 thread.");
		Cache<String, CacheItem> cache1 = new Cache<String, CacheItem>("LRU", 10, 10, 1000000);
		long startTime1 = System.nanoTime();
		for (int i=0; i<1000000; i++) {
			Integer no = (int) Math.ceil(Math.random() * 5500000);
			String key = "key"+String.valueOf(no);
			CacheItem ci = cache1.get(key);
			cache1.put(key, new CacheItem("value"+String.valueOf(no), System.currentTimeMillis()));
		}
		long endTime1 = System.nanoTime();
		long time1 = (endTime1 - startTime1) / 1000000L;
		
		System.out.println("Test 1000000 cache items with 10 threads.");
		Cache<String, CacheItem> cache2 = new Cache<String, CacheItem>("LRU", 10, 10, 1000000);		
		ExecutorService es = Executors.newFixedThreadPool(10);
		long startTime2 = System.nanoTime();
		for (int i=0; i<10; i++) {
			es.execute(new Runnable() {
				public void run() {
					for (int j=0; j<100000; j++) {
						Integer no = (int) Math.ceil(Math.random() * 5500000);
						String key = "key"+String.valueOf(no);
						CacheItem ci = cache2.get(key);
						cache2.put(key, new CacheItem("value"+String.valueOf(no), System.currentTimeMillis()));
					}
				}
			});
		}		
		es.shutdown();
		es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		long endTime2 = System.nanoTime();
		long time2 = (endTime2 - startTime2) / 1000000L;
		
		System.out.println("Execution time using 1 thread: " + time1 + " ms.");
		System.out.println("Execution time using 10 threads: " + time2 + " ms.");
		System.out.println("\nNOTE: this test is only for concurrency support, not performance."
				+ " Multi-thread may be slower than single thread because of overhead.");
	}
}
