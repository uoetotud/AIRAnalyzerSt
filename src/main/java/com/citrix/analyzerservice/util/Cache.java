package com.citrix.analyzerservice.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.model.CacheItem;

public class Cache<K, V> implements ICache<K, V> {
	
	private static final Logger logger = Logger.getLogger(Cache.class);
	
	// define cache objects sizes
//	private static final int CONFERENCE_SUMMARY = 1123;
//	private static final int CONFERENCE_DETAILS = 174029;
//	private static final int CHANNEL_SUMMARY = 298;
//	private static final int CHANNEL_DETAILS = 173213;
//	private static final int MAX_SIZE = 1000000000;
	
	// ONLY for testing purpose...
	private static final int CONFERENCE_SUMMARY = 1200;
	private static final int CONFERENCE_DETAILS = 180000;
	private static final int CHANNEL_SUMMARY = 300;
	private static final int CHANNEL_DETAILS = 175000;
	private static final int MAX_SIZE = 300000000;
	private static final int MIN_SIZE = 2000000;
	
	private String type = "";
	private long timeOut = 0;
	private int maxCacheSize = 0, usedCacheSize = 0;
	private ConcurrentHashMap<K, V> cacheMap = null;
	
	public Cache(String type, long timeOut, final long cleanInterval, int maxSize) {
		if (!type.equalsIgnoreCase("LRU") && !type.equalsIgnoreCase("LFU")) {
			type = "LRU";
			logger.warn("Cache type not specified or invalid - set to default type: LRU.");
		}
		
		this.type = type;
		this.timeOut = timeOut;
		if (maxSize > MAX_SIZE) {
			logger.warn("Cache size cannot be configured larger than 1GB - set to maximum size: 1GB.");
			this.maxCacheSize = MAX_SIZE;
		} else if (maxSize < MIN_SIZE){
			logger.warn("Cache size cannot be configured smaller than 2MB - set to minimum size: 2MB.");
			this.maxCacheSize = MIN_SIZE;
		} else {
			this.maxCacheSize = maxSize;
		}
		cacheMap = new ConcurrentHashMap<K, V>(this.maxCacheSize/CONFERENCE_SUMMARY);
		
		if (this.maxCacheSize >= 1000000000)
			logger.info("New " + type + " cache created (MaxSize: " + this.maxCacheSize/1000000000 + "GB; TimeOut: " + timeOut/1000 + 
				"s; CleanInterval: " + cleanInterval/1000 + "s)");
		else if (this.maxCacheSize >= 1000000)
			logger.info("New " + type + " cache created (MaxSize: " + this.maxCacheSize/1000000 + "MB; TimeOut: " + timeOut/1000 + 
					"s; CleanInterval: " + cleanInterval/1000 + "s)");
		else if (this.maxCacheSize >= 1000)
			logger.info("New " + type + " cache created (MaxSize: " + this.maxCacheSize/1000 + "KB; TimeOut: " + timeOut/1000 + 
					"s; CleanInterval: " + cleanInterval/1000 + "s)");
		else
			logger.info("New " + type + " cache created (MaxSize: " + this.maxCacheSize + "B; TimeOut: " + timeOut/1000 + 
					"s; CleanInterval: " + cleanInterval/1000 + "s)");
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(cleanInterval);
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
			ci.setLastAccessed(System.currentTimeMillis());
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
		
		if (cacheMap.containsKey(key)) {
			logger.warn("Cache already exits. No need to add.");
			return;
		}
		
		int newCacheObjSize = mapCacheObjectSize(key, value);
		int tempCacheSize = this.usedCacheSize + newCacheObjSize;
		
		while (tempCacheSize >= this.maxCacheSize) {
			K keyToRemove = null;
			
			if (type.equalsIgnoreCase("LRU")) {
				keyToRemove = findLruKey();
				logger.warn("LRU Cache is full. Replace item with key: " + keyToRemove.toString());
			} else if (type.equalsIgnoreCase("LFU")) {
				keyToRemove = findLfuKey();
				logger.warn("LFU Cache is full. Replace item with key: " + keyToRemove.toString());
			}
			
			// remove cache item in cache
			remove(keyToRemove);
			
			// release space in cache size
			tempCacheSize -= mapCacheObjectSize(keyToRemove, cacheMap.get(keyToRemove));
		}
		
		cacheMap.putIfAbsent(key, value);
		
		this.usedCacheSize = tempCacheSize;
		logger.info("Added new cache item, cache size: " + this.usedCacheSize + " bytes.");
	}

	@Override
	public void remove(K key) {
		if (key == null) {
			logger.error("Please provide a valid key.");
			return;
		}
		
		cacheMap.remove(key);
		
		this.usedCacheSize -= mapCacheObjectSize(key, cacheMap.get(key));
		logger.info("Removed cache item, cache size: " + this.usedCacheSize + " bytes.");
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
	
	@SuppressWarnings("rawtypes")
	private int mapCacheObjectSize(K key, V value) {
		int cacheObjctSize = 0;
		
		if (((String) key).equalsIgnoreCase("ConferenceList")) {
			int count = ((ArrayList) ((CacheItem) value).getCacheObject()).size();
			cacheObjctSize = CONFERENCE_SUMMARY * count;
		} else if (((String) key).endsWith("ConfSummary")) {
			cacheObjctSize = CONFERENCE_SUMMARY;
		} else if (((String) key).endsWith("ConfDetails")) {
			cacheObjctSize = CONFERENCE_DETAILS;
		} else if (((String) key).endsWith("ConfChannels")) {
			int count = ((ArrayList) ((CacheItem) value).getCacheObject()).size();
			cacheObjctSize = CHANNEL_SUMMARY * count;
		} else if (((String) key).endsWith("ChanSummary")) {
			cacheObjctSize = CHANNEL_SUMMARY;
		} else if (((String) key).endsWith("ChanDetails")) {
			cacheObjctSize = CHANNEL_DETAILS;
		}
		
		return cacheObjctSize;
	}
	
	private K findLruKey() {
		K lruKey = null, tempKey = null;
		
		for (Map.Entry<K, V> e : cacheMap.entrySet()) {
			tempKey = (K) e.getKey();
			
			if (lruKey == null || ((CacheItem<?>) cacheMap.get(lruKey)).getLastAccessed() > ((CacheItem<?>) e.getValue()).getLastAccessed())
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
