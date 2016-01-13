package com.citrix.analyzerservice.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.model.CacheItem;

public class Cache<K, V> implements ICache<K, V> {
	
	private static final Logger logger = Logger.getLogger(Cache.class);
	
	private String type;
	private long timeOut;
	private int maxSize;
	private ConcurrentHashMap<K, V> cacheMap;
	
	public Cache(String type, long timeOut, final long cleanInterval, int maxSize) {
		if (!type.equalsIgnoreCase("LRU") && !type.equalsIgnoreCase("LFU")) {
			type = "LRU";
			logger.warn("Cache type not specified or invalid - use default type: LRU.");
		}
		
		logger.info("New " + type + " cache created (MaxSize: " + maxSize + "; TimeOut: " + timeOut/1000 + 
				"s; CleanInterval: " + cleanInterval/1000 + "s)");
		
		this.type = type;
		this.timeOut = timeOut * 1000;
		this.maxSize = maxSize;
		cacheMap = new ConcurrentHashMap<K, V>(maxSize);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(cleanInterval * 1000);
					} catch (InterruptedException e) {
						logger.error("Thread for waiting to clean up cache interrupted.");
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
		
		return (V) cacheMap.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V fetch(K key) {
		if (key == null) {
			logger.error("Please provide a valid key.");
			return null;
		}
		
		CacheItem<?> ci = (CacheItem<?>) cacheMap.get(key);
		
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
				logger.warn("LRU Cache is full. Replace item with key: " + keyToRemove.toString());
			} else if (type.equalsIgnoreCase("LFU")) {
				keyToRemove = findLfuKey();
				logger.warn("LFU Cache is full. Replace item with key: " + keyToRemove.toString());
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
	public boolean contains(K key) {
		return cacheMap.containsKey(key);
	}

	@Override
	public int size() {
		return cacheMap.size();
	}

	@Override
	public void cleanup() {
		long time = System.currentTimeMillis();
		K key = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			key = e.getKey();
			
			if (time > (((CacheItem<?>) e.getValue()).getTimeStamp() + timeOut)) {
				logger.warn("Cache " + key + " expired.");
				cacheMap.remove(key);
			}
		}
	}
	
	@Override
	public void clear() {
		cacheMap.clear();
		logger.warn("Cache cleared.");
	}
	
	private K findLruKey() {
		K lruKey = null, tempKey = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			tempKey = (K) e.getKey();
			
			if (lruKey == null || ((CacheItem<?>) cacheMap.get(lruKey)).getTimeStamp() > ((CacheItem<?>) e.getValue()).getTimeStamp())
				lruKey = tempKey;
		}
		
		return lruKey;
	}
	
	private K findLfuKey() {
		K lfuKey = null, tempKey = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			tempKey = (K) e.getKey();
			
			if (lfuKey == null || ((CacheItem<?>) cacheMap.get(lfuKey)).getHitCount() > ((CacheItem<?>) e.getValue()).getHitCount())
				lfuKey = tempKey;
		}
		
		return lfuKey;
	}
}
