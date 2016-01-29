package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class ChannelStats {
	
	private StreamProcessor strProcessor;
	private StreamEnhancer strEnhancer;
	private MixerOut mixOut;
	
	public ChannelStats(StreamProcessor strProcessor, StreamEnhancer strEnhancer, MixerOut mixOut) {
		this.strProcessor = strProcessor;
		this.strEnhancer = strEnhancer;
		this.mixOut = mixOut;
	}

	public StreamProcessor getStrProcessor() {
		return strProcessor;
	}

	public StreamEnhancer getStrEnhancer() {
		return strEnhancer;
	}
	
	public MixerOut getMixerOut() {
		return mixOut;
	}
	
	
}
