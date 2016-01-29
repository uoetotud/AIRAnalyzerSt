package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class CacheItem<V> {

	private V cacheObject;
	private long timeStamp; 
	private long lastAccessed; // for LRU
	private int hitCount; // for LFU
	
	public CacheItem(V cacheObject, long timeStamp) {
		this.cacheObject = cacheObject;
		this.timeStamp = timeStamp;
		this.lastAccessed = timeStamp;
		this.hitCount = 1;
	}
	
	public V getCacheObject() {
		return cacheObject;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public long getLastAccessed() {
		return lastAccessed;
	}

	public void setLastAccessed(long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}	
	
}
