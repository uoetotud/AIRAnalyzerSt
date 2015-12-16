package com.citrix.analyzerservice.model;

public class StreamProcessor {

	private double[] SeqNr;
	private double[] NS_speechPowerOut;
	private double[] NS_noisePowerOut;	

	public StreamProcessor(double[] seqNr, double[] nS_speechPowerOut, double[] nS_noisePowerOut) {
		SeqNr = seqNr;
		NS_speechPowerOut = nS_speechPowerOut;
		NS_noisePowerOut = nS_noisePowerOut;
	}

	public double[] getSeqNr() {
		return SeqNr;
	}

	public double[] getNS_speechPowerOut() {
		return NS_speechPowerOut;
	}

	public double[] getNS_noisePowerOut() {
		return NS_noisePowerOut;
	}
	
}
