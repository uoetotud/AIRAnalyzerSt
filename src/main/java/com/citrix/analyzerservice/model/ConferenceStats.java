package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class ConferenceStats {
	
	private MixerSum mixSum;
	
	public ConferenceStats(MixerSum mixSum) {
		this.mixSum = mixSum;
	}

	public MixerSum getMixerSum() {
		return mixSum;
	}	

}
