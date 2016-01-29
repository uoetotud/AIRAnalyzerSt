package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class ConferenceScore {

	private int avgPLIndicator;
	private int avgLevelIndicator;
	
	public ConferenceScore(int avgPLIndicator, int avgLevelIndicator) {
		this.avgPLIndicator = avgPLIndicator;
		this.avgLevelIndicator = avgLevelIndicator;
	}

	public int getAvgPLIndicator() {
		return avgPLIndicator;
	}

	public int getAvgLevelIndicator() {
		return avgLevelIndicator;
	}

	public void setAvgPLIndicator(int avgPLIndicator) {
		this.avgPLIndicator = avgPLIndicator;
	}

	public void setAvgLevelIndicator(int avgLevelIndicator) {
		this.avgLevelIndicator = avgLevelIndicator;
	}
	
}
