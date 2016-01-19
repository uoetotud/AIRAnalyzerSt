package com.citrix.analyzerservice.model;

public class ChannelScore {

	private int avgPLIndicator;
	private int avgLevelIndicator;
	private double avgPacketLoss;
	
	public ChannelScore(int avgPLIndicator, int avgLevelIndicator, double avgPacketLoss) {
		this.avgPLIndicator = avgPLIndicator;
		this.avgLevelIndicator = avgLevelIndicator;
		this.avgPacketLoss = avgPacketLoss;
	}

	public int getAvgPLIndicator() {
		return avgPLIndicator;
	}

	public int getAvgLevelIndicator() {
		return avgLevelIndicator;
	}

	public double getAvgPacketLoss() {
		return avgPacketLoss;
	}

}
