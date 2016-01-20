package com.citrix.analyzerservice.model;

public class MixerOut {

	private double[] SeqNr;
	private double[] TimeStamp;
	
	public MixerOut(double[] seqNr, double[] timeStamp) {
		SeqNr = seqNr;
		TimeStamp = timeStamp;
	}

	public double[] getSeqNr() {
		return SeqNr;
	}

	public double[] getTimeStamp() {
		return TimeStamp;
	}
}
