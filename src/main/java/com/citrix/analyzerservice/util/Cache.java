package com.citrix.analyzerservice.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.model.CacheItem;

public class Cache<K, V> implements ICache<K, V> {
	
	private static final Logger logger = Logger.getLogger(Cache.class);
	
	// define cache objects sizes
	private static final int CONFERENCE_SUMMARY = 1300;
	private static final int CONFERENCE_DETAILS = 150000;
	private static final int CHANNEL_SUMMARY = 300;
	private static final int CHANNEL_DETAILS = 1210000;
	private static final int MAX_SIZE = 300000000;
	private static final int MIN_SIZE = 2000000;
	
	private String type = "";
	private long timeOut = 0;
	private int maxCacheSize = 0, usedCacheSize = 0;
	private ConcurrentHashMap<K, V> cacheMap = null;
	
	public Cache(String _type, long _timeOut, final long _cleanInterval, int _maxSize) {
		if (!_type.equalsIgnoreCase("LRU") && !_type.equalsIgnoreCase("LFU")) {
			type = "LRU";
			logger.warn("Cache type not specified or invalid - set to default type: LRU.");
		}
		
		type = _type;
		timeOut = _timeOut;
		if (_maxSize > MAX_SIZE) {
			logger.warn("Cache size cannot be configured larger than 1GB - set to maximum size: 1GB.");
			maxCacheSize = MAX_SIZE;
		} else if (_maxSize < MIN_SIZE){
			logger.warn("Cache size cannot be configured smaller than 2MB - set to minimum size: 2MB.");
			maxCacheSize = MIN_SIZE;
		} else {
			maxCacheSize = _maxSize;
		}
		cacheMap = new ConcurrentHashMap<K, V>();
		
		String s = "";
		if (maxCacheSize >= 1000000000)
			s = new StringBuilder(Integer.toString(maxCacheSize/1000000000)).append("GB").toString();
		else if (maxCacheSize >= 1000000)
			s = new StringBuilder(Integer.toString(maxCacheSize/1000000)).append("MB").toString();
		else if (maxCacheSize >= 1000)
			s = new StringBuilder(Integer.toString(maxCacheSize/1000)).append("KB").toString();
		else
			s = new StringBuilder(Integer.toString(maxCacheSize)).append("B").toString();
		logger.info(new StringBuilder("New ").append(type).append(" cache created (MaxSize: ").append(s).append("; TimeOut: ")
				.append(Long.toString(timeOut/1000)).append("s; CleanInterval: ").append(Long.toString(_cleanInterval/1000)).append("s)"));
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(_cleanInterval);
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
		int tempCacheSize = usedCacheSize + newCacheObjSize;
		
		while (tempCacheSize >= maxCacheSize) {
			K keyToRemove = null;
			
			if (type.equalsIgnoreCase("LRU")) {
				keyToRemove = findLruKey();
				logger.warn(new StringBuilder("LRU Cache is full. Replace item with key: ").append(keyToRemove.toString()));
			} else if (type.equalsIgnoreCase("LFU")) {
				keyToRemove = findLfuKey();
				logger.warn(new StringBuilder("LFU Cache is full. Replace item with key: ").append(keyToRemove.toString()));
			}
			
			// remove cache item in cache
			remove(keyToRemove);
			
			// release space in cache size
			tempCacheSize -= mapCacheObjectSize(keyToRemove, cacheMap.get(keyToRemove));
		}
		
		cacheMap.putIfAbsent(key, value);
		
		usedCacheSize = tempCacheSize;
		logger.info(new StringBuilder("Added new cache item, cache size: ").append(Integer.toString(usedCacheSize)).append(" bytes."));
	}

	@Override
	public void remove(K key) {
		if (key == null) {
			logger.error("Please provide a valid key.");
			return;
		}
		
		cacheMap.remove(key);
		
		usedCacheSize -= mapCacheObjectSize(key, cacheMap.get(key));
		logger.info(new StringBuilder("Removed cache item, cache size: ").append(Integer.toString(usedCacheSize)).append(" bytes."));
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
				logger.warn(new StringBuilder("Cache ").append(key).append(" expired."));
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
