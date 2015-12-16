package com.citrix.analyzerservice.model;

public class StreamEnhancer {

	private double NS_speechPowerOut;
	private double NS_noisePowerOut;

	public StreamEnhancer(double NS_speechPowerOut, double NS_noisePowerOut) {
		this.NS_speechPowerOut = NS_speechPowerOut;
		this.NS_speechPowerOut = NS_noisePowerOut;
	}

	private double getNS_speechPowerOut() {
		return NS_speechPowerOut;
	}

	private double getNS_noisePowerOut() {
		return NS_noisePowerOut;
	}
	
}
