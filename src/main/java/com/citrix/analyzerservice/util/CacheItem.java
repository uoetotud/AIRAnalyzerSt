package com.citrix.analyzerservice.util;

public class CacheItem<V> {

	private V cacheObject;
	private long timeStamp; // for LRU
	private int hitCount; // for LFU
	
	public CacheItem(V cacheObject, long timeStamp) {
		this.cacheObject = cacheObject;
		this.timeStamp = timeStamp;
		this.hitCount = 0;
	}
	
	public V getCacheObject() {
		return cacheObject;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}	
	
}
