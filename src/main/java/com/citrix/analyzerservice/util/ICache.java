package com.citrix.analyzerservice.util;

public interface ICache<K, V> {

	public V get(K key);
	public void put(K key, V value);
	public void remove(K key);
	public int size();
	public void cleanup();
	
}
