package util;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.util.Cache;
import com.citrix.analyzerservice.util.CacheItem;

public class TestCache {

	public TestCache() {}
	
	@Test
	@Ignore
	public void TestGetPutRemoveSizeFunctions() throws InterruptedException {
		Cache<String, CacheItem> cache = new Cache<String, CacheItem>("LRU", 10, 5, 5);
		assertEquals(cache.size(), 0);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		assertNotNull(cache.get("key3"));
		
		// Item 4, 5 added
		Thread.sleep(2000);		
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
			
		// Item 4 removed
		Thread.sleep(2000);
		cache.remove("key4");
		assertEquals(cache.size(), 4);
		assertNull(cache.get("key4"));
		
		// Item 6 added
		Thread.sleep(2000);
		cache.put("key6", new CacheItem("value6", System.currentTimeMillis()));
		assertEquals(cache.size(), 5);
	}
	
	@Test
	@Ignore
	public void TestCachItemExpire() throws InterruptedException {
		Cache<String, CacheItem> cache = new Cache<String, CacheItem>("LRU", 2, 2, 5);
		
		// Item 1, 2 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		assertEquals(cache.size(), 2);
		
		// Item 1, 2 expired
		Thread.sleep(5000);
		assertEquals(cache.size(), 0);
	}
	
	@Test
	@Ignore
	public void TestLruCache() throws InterruptedException {
		Cache<String, CacheItem> cache = new Cache<String, CacheItem>("LRU", 10, 5, 3);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		
		// Timestamp of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.get("key2");
		assertNotNull(ci2);
		
		Thread.sleep(2000);
		CacheItem ci1 = (CacheItem) cache.get("key1");
		assertNotNull(ci1);
		
		Thread.sleep(2000);
		CacheItem ci3 = (CacheItem) cache.get("key3");
		assertNotNull(ci3);
		
		assertTrue(ci3.getTimeStamp() > ci1.getTimeStamp());
		assertTrue(ci1.getTimeStamp() > ci2.getTimeStamp());
		
		// Item 4 added - replace item 2
		Thread.sleep(2000);
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);

		CacheItem ci4 = (CacheItem) cache.get("key4");
		assertNotNull(ci4);
		ci2 = (CacheItem) cache.get("key2");
		assertNull(ci2);
		
		// Item 5 added - replace item 1
		Thread.sleep(2000);
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		CacheItem ci5 = (CacheItem) cache.get("key5");
		assertNotNull(ci5);
		ci1 = (CacheItem) cache.get("key1");
		assertNull(ci1);
	}
	
	@Test
	@Ignore
	public void TestLfuCache() throws InterruptedException {
		Cache<String, CacheItem> cache = new Cache<String, CacheItem>("LFU", 15, 5, 3);
		
		// Item 1, 2, 3 added
		cache.put("key1", new CacheItem("value1", System.currentTimeMillis()));
		cache.put("key2", new CacheItem("value2", System.currentTimeMillis()));
		cache.put("key3", new CacheItem("value3", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		
		// HitCount of item 1, 2, 3 updated
		CacheItem ci2 = (CacheItem) cache.get("key2");
		assertNotNull(ci2);
		assertEquals(ci2.getHitCount(), 1);
		
		int i = 0;
		Thread.sleep(2000);
		CacheItem ci1 = null;
		for (i=0; i<2; i++)
			ci1 = (CacheItem) cache.get("key1");
		assertNotNull(ci1);
		assertEquals(ci1.getHitCount(), 2);
		
		Thread.sleep(2000);
		CacheItem ci3 = null;
		for (i=0; i<3; i++)
			ci3 = (CacheItem) cache.get("key3");
		assertNotNull(ci3);
		assertEquals(ci3.getHitCount(), 3);

		// Item 4 added - replace item 2
		Thread.sleep(2000);
		cache.put("key4", new CacheItem("value4", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);

		CacheItem ci4 = (CacheItem) cache.get("key4");
		assertNotNull(ci4);
		ci2 = (CacheItem) cache.get("key2");
		assertNull(ci2);
		
		// Item 5 added - replace item 4
		Thread.sleep(2000);
		cache.put("key5", new CacheItem("value5", System.currentTimeMillis()));
		assertEquals(cache.size(), 3);
		CacheItem ci5 = (CacheItem) cache.get("key5");
		assertNotNull(ci5);
		ci4 = (CacheItem) cache.get("key4");
		assertNull(ci4);
	}
}
