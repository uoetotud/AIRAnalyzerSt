package com.citrix.analyzerservice.util;

public interface ICache<K, V> {

	V get(K key);
	void put(K key, V value);
	void remove(K key);
	boolean contains(K key);
	int size();
	void cleanup();
	
}
