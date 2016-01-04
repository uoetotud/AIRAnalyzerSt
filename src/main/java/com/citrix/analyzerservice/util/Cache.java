package com.citrix.analyzerservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class Cache<K, V> implements ICache<K, V> {
	
	private static final Logger logger = Logger.getLogger(Cache.class);
	
	private String type;
	private long timeOut;
	private int maxSize;
	private ConcurrentHashMap<K, V> cacheMap;
	
	public Cache(String type, long timeOut, final long clearInterval, int maxSize) {
		logger.info("New " + type + " cache created - time out: " + timeOut + " seconds; MAX size: " + maxSize + 
				"; clear every " + clearInterval + " seconds.");
		
		this.type = type;
		this.timeOut = timeOut * 1000;
		this.maxSize = maxSize;
		cacheMap = new ConcurrentHashMap<K, V>(maxSize);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(clearInterval * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						// TODO - logging
					}
					cleanup();
				}
			}
		});
		
		t.setDaemon(true);
		t.start();

	}

	@Override
	public V get(K key) {
		if (key == null) {
			logger.error("Please provide a valid key.");
			return null;
		}
		
		CacheItem ci = (CacheItem) cacheMap.get(key);
		
		if (ci == null)
			return null;
		else {
			ci.setTimeStamp(System.currentTimeMillis());
			ci.setHitCount(ci.getHitCount() + 1);
			return (V) ci;
		}
	}

	@Override
	public void put(K key, V value) {
		if (key == null || value == null) {
			logger.error("Please provide a valid key/value pair to cache.");
			return;
		}
		
		if (cacheMap.size() < maxSize) {
			cacheMap.putIfAbsent(key, value);
		} else {
			if (cacheMap.containsKey(key)) {
				logger.warn("Cache already exits. No need to add.");
				return;
			}
			
			K keyToRemove = null;
			
			if (type.equalsIgnoreCase("LRU")) {
				keyToRemove = findLruKey();
				logger.warn("Cache is full. Replace LRU item with key: " + keyToRemove.toString());
			} else if (type.equalsIgnoreCase("LFU")) {
				keyToRemove = findLfuKey();
				logger.warn("Cache is full. Replace LFU item with key: " + keyToRemove.toString());
			}
			
			remove(keyToRemove);
			cacheMap.put(key, value);
		}
		
	}

	@Override
	public void remove(K key) {
		if (key == null) {
			logger.error("Please provide a valid key.");
			return;
		}
		
		cacheMap.remove(key);		
	}

	@Override
	public int size() {
		return cacheMap.size();
	}

	@Override
	public void cleanup() {
		long time = System.currentTimeMillis();
		List<K> keysToRemove = new ArrayList<K>();
		K key = null;
		CacheItem ci = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			key = e.getKey();
			ci = (CacheItem) e.getValue();
			
			if (ci != null && (time > (ci.getTimeStamp() + timeOut)))
				keysToRemove.add(key);
		}
		
		logger.info(keysToRemove.size() + " cache item(s) expired.");
		
		for (K k : keysToRemove) {
			cacheMap.remove(k);
		}
	}
	
	private K findLruKey() {
		K lruKey = null, tempKey = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			tempKey = (K) e.getKey();
			
			if (lruKey == null || ((CacheItem) cacheMap.get(lruKey)).getTimeStamp() > ((CacheItem) e.getValue()).getTimeStamp())
				lruKey = tempKey;
		}
		
		return lruKey;
	}
	
	private K findLfuKey() {
		K lfuKey = null, tempKey = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			tempKey = (K) e.getKey();
			
			if (lfuKey == null || ((CacheItem) cacheMap.get(lfuKey)).getHitCount() > ((CacheItem) e.getValue()).getHitCount())
				lfuKey = tempKey;
		}
		
		return lfuKey;
	}
}
