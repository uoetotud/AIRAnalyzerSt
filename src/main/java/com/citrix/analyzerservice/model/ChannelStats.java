package com.citrix.analyzerservice.model;

public class ChannelStats {
	
	private StreamProcessor strProcessor;
	private StreamEnhancer strEnhancer;
	
	public ChannelStats(StreamProcessor strProcessor, StreamEnhancer strEnhancer) {
		this.strProcessor = strProcessor;
		this.strEnhancer = strEnhancer;
	}

	public StreamProcessor getStrProcessor() {
		return strProcessor;
	}

	public StreamEnhancer getStrEnhancer() {
		return strEnhancer;
	}
	
	
}
