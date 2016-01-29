import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.util.Cache;

/**
 * @author Xi Luo
 *
 */
public class TestCache {
	
	@SuppressWarnings("rawtypes")
	static Cache<String, CacheItem> cache = null;
	
	// create fake entries
	static String entry1 = "ConfSummary";
	static String entry2 = "ConfDetails";
	static List<String> entry3 = new ArrayList<String>();
	static String entry4 = "ChanSummary";
	static String entry5 = "ChanDetails";
	static List<String> entry6 = new ArrayList<String>();

	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test Cache ###");
		
		entry3.add("Conf1");
		entry3.add("Conf2");
		entry3.add("Conf3");
		
		entry6.add("Chan1");
		entry6.add("Chan2");
		entry6.add("Chan3");
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
		cache.clear();
		System.out.println("-----------------------------------------------------");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testGetPutRemoveContainsSizeFunctions() throws InterruptedException {
		System.out.println("\n** Test Get/Put/Remove/Contains/Size functions **");
		cache = new Cache<String, CacheItem>("LRU", 100000, 100000, 500000);
		assertEquals(cache.size(), 0);
		
		// Item 1, 2, 3 added
		cache.put(entry1, new CacheItem(entry1, System.currentTimeMillis()));
		cache.put(entry2, new CacheItem(entry2, System.currentTimeMillis()));
		cache.put("ConferenceList", new CacheItem(entry3, System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		assertNotNull(cache.get("ConferenceList"));
		assertTrue(cache.contains(entry1));
		System.out.println(cache.size() + " cache items added.");
		
		// Item 4, 5 added
		Thread.sleep(2000);		
		cache.put(entry4, new CacheItem(entry4, System.currentTimeMillis()));
		cache.put(entry5, new CacheItem(entry5, System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
		assertTrue(cache.contains(entry4));
		System.out.println("2 more cache items added. Cache size: " + cache.size());
			
		// Item 4 removed
		Thread.sleep(2000);
		cache.remove(entry4);
		assertEquals(cache.size(), 4);
		assertNull(cache.get(entry4));
		assertFalse(cache.contains(entry4));
		System.out.println("1 cache item removed. Cache size: " + cache.size());
		
		// Item 6 added
		Thread.sleep(2000);
		cache.put("ConfChannels", new CacheItem(entry6, System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
		assertTrue(cache.contains("ConfChannels"));
		System.out.println("1 cache item added. Cache size: " + cache.size());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testCacheItemExpire() throws InterruptedException {
		System.out.println("\n** Test cache item expires **");
		cache = new Cache<String, CacheItem>("LRU", 2000, 3000, 500000);
		
		// Item 1, 2 added
		cache.put(entry1, new CacheItem(entry1, System.currentTimeMillis()));
		cache.put("ConferenceList", new CacheItem(entry3, System.currentTimeMillis()));
		assertEquals(cache.size(), 2);
		System.out.println(cache.size() + " cache items added.");
		System.out.println("Cache size: " + cache.size());
		
		// wait for 5s
		Thread.sleep(5000);

		// Item 1, 2 expired
		assertEquals(cache.size(), 0);
		System.out.println("Cache size: " + cache.size());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testLruCache() throws InterruptedException {
		System.out.println("\n** Test LRU cache **");
		cache = new Cache<String, CacheItem>("LRU", 10000, 5000, 300000);
		
		// Item 1, 2, 3 added
		cache.put(entry1, new CacheItem(entry1, System.currentTimeMillis()));
		cache.put(entry2, new CacheItem(entry2, System.currentTimeMillis()));
		cache.put("ConferenceList", new CacheItem(entry3, System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(cache.size() + " cache items added.");
		
		// Timestamp of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.fetch(entry2);
		assertNotNull(ci2);
		System.out.println(entry2 + " cache item accessed at: " + ci2.getLastAccessed());
		
		Thread.sleep(1000);
		CacheItem ci1 = (CacheItem) cache.fetch(entry1);
		assertNotNull(ci1);
		System.out.println(entry1 + " cache item accessed at: " + ci1.getLastAccessed());
		
		Thread.sleep(1000);
		CacheItem ci3 = (CacheItem) cache.fetch("ConferenceList");
		assertNotNull(ci3);
		System.out.println("ConferenceList cache item accessed at: " + ci3.getLastAccessed());
		
		assertTrue(ci3.getLastAccessed() > ci1.getLastAccessed());
		assertTrue(ci1.getLastAccessed() > ci2.getLastAccessed());
		
		// Item 5 added - replace item 2
		Thread.sleep(1000);
		cache.put(entry5, new CacheItem(entry5, System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(entry5 + " cache item added.");

		CacheItem ci5 = (CacheItem) cache.fetch(entry5);
		assertNotNull(ci5);
		ci2 = (CacheItem) cache.get(entry2);
		assertNull(ci2);
		
		// Item 4 added
		Thread.sleep(1000);
		cache.put(entry4, new CacheItem(entry4, System.currentTimeMillis()));
		assertEquals(cache.size(), 4);
		System.out.println(entry4 + " cache item added.");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testLfuCache() throws InterruptedException {
		System.out.println("\n** Test LFU cache **");
		cache = new Cache<String, CacheItem>("LFU", 15000, 5000, 300000);
		
		// Item 1, 2, 3 added
		cache.put(entry1, new CacheItem(entry1, System.currentTimeMillis()));
		cache.put(entry2, new CacheItem(entry2, System.currentTimeMillis()));
		cache.put("ConferenceList", new CacheItem(entry3, System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(cache.size() + " cache items added.");
		
		// HitCount of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.fetch(entry2);
		assertNotNull(ci2);
		assertEquals(ci2.getHitCount(), 2);
		System.out.println(entry2 + " cache item accessed, hitCount: " + ci2.getHitCount());
		
		int i = 0;
		Thread.sleep(1000);
		CacheItem ci1 = null;
		for (i=0; i<2; i++) {
			ci1 = (CacheItem) cache.fetch(entry1);
			System.out.println(entry1 + " cache item accessed, hitCount: " + ci1.getHitCount());
		}
		assertNotNull(ci1);
		assertEquals(ci1.getHitCount(), 3);
		
		Thread.sleep(1000);
		CacheItem ci3 = null;
		for (i=0; i<3; i++) {
			ci3 = (CacheItem) cache.fetch("ConferenceList");
			System.out.println("ConferenceList cache item accessed, hitCount: " + ci3.getHitCount());
		}
		assertNotNull(ci3);
		assertEquals(ci3.getHitCount(), 4);

		// Item 5 added - replace item 2
		Thread.sleep(1000);
		cache.put(entry5, new CacheItem(entry5, System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		System.out.println(entry5 + " cache item added.");

		CacheItem ci5 = (CacheItem) cache.fetch(entry5);
		assertNotNull(ci5);
		System.out.println(entry5 + " cache item accessed, hitCount: " + ci5.getHitCount());
		ci2 = (CacheItem) cache.get(entry2);
		assertNull(ci2);
		
		// Item 6 added
		Thread.sleep(1000);
		cache.put("ConfChannels", new CacheItem(entry6, System.currentTimeMillis()));
		assertEquals(cache.size(), 4);
		System.out.println(entry6 + " cache item added.");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Test
	public void testConcurrency() throws InterruptedException {
		System.out.println("\n** Test concurrency support **");
		
		System.out.println("Test 100000 cache items with 1 thread.");
		Cache<String, CacheItem> cache1 = new Cache<String, CacheItem>("LRU", 10000, 10000, 300000000);
		long startTime1 = System.nanoTime();
		for (int i=0; i<100000; i++) {
			Integer no = (int) Math.ceil(Math.random() * 550000);
			String key = String.valueOf(no) + "ChanSummary";
			CacheItem ci = cache1.get(key);
			cache1.put(key, new CacheItem("value"+String.valueOf(no), System.currentTimeMillis()));
		}
		long endTime1 = System.nanoTime();
		long time1 = (endTime1 - startTime1) / 1000000L;
		
		System.out.println("Test 100000 cache items with 10 threads.");
		Cache<String, CacheItem> cache2 = new Cache<String, CacheItem>("LRU", 10000, 10000, 300000000);		
		ExecutorService es = Executors.newFixedThreadPool(10);
		long startTime2 = System.nanoTime();
		for (int i=0; i<10; i++) {
			es.execute(new Runnable() {
				public void run() {
					for (int j=0; j<10000; j++) {
						Integer no = (int) Math.ceil(Math.random() * 550000);
						String key = String.valueOf(no) + "ChanSummary";
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
