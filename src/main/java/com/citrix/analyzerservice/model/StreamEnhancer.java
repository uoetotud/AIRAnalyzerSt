package com.citrix.analyzerservice.model;

public class StreamEnhancer {

	private double NS_speechPowerOut;
	private double NS_noisePowerOut;

	public StreamEnhancer(double NS_speechPowerOut, double NS_noisePowerOut) {
		this.NS_speechPowerOut = NS_speechPowerOut;
		this.NS_speechPowerOut = NS_noisePowerOut;
	}

	@SuppressWarnings("unused")
	private double getNS_speechPowerOut() {
		return NS_speechPowerOut;
	}

	@SuppressWarnings("unused")
	private double getNS_noisePowerOut() {
		return NS_noisePowerOut;
	}
	
}
