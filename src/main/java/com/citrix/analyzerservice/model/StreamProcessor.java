package com.citrix.analyzerservice.model;

public class StreamProcessor {

	private double[] SeqNr;
	private double[] Muted;
	private double[] RTP_streamBegin;
	private double[] RTP_isDelayed;
	private double[] RTP_isReordered;
	private double[] TimeStamp;
	private double[] Media_BufSize;
	private double[] IATJitter;
	private double[] NS_speechPowerIn;
	private double[] NS_speechPowerOut;
	private double[] NS_noisePowerIn;
	private double[] NS_noisePowerOut;
	private double[] AGC_speechLevelOut;
	private double[] AGC_noiseLevelOut;
	private double[] AGC_vadState;
	
	public StreamProcessor(double[] seqNr, double[] muted, double[] rTP_streamBegin, double[] rTP_isDelayed,
			double[] rTP_isReordered, double[] timeStamp, double[] media_BufSize, double[] iATJitter,
			double[] nS_speechPowerIn, double[] nS_speechPowerOut, double[] nS_noisePowerIn, double[] nS_noisePowerOut,
			double[] aGC_speechLevelOut, double[] aGC_noiseLevelOut, double[] aGC_vadState) {
		SeqNr = seqNr;
		Muted = muted;
		RTP_streamBegin = rTP_streamBegin;
		RTP_isDelayed = rTP_isDelayed;
		RTP_isReordered = rTP_isReordered;
		TimeStamp = timeStamp;
		Media_BufSize = media_BufSize;
		IATJitter = iATJitter;
		NS_speechPowerIn = nS_speechPowerIn;
		NS_speechPowerOut = nS_speechPowerOut;
		NS_noisePowerIn = nS_noisePowerIn;
		NS_noisePowerOut = nS_noisePowerOut;
		AGC_speechLevelOut = aGC_speechLevelOut;
		AGC_noiseLevelOut = aGC_noiseLevelOut;
		AGC_vadState = aGC_vadState;
	}

	public double[] getSeqNr() {
		return SeqNr;
	}

	public double[] getMuted() {
		return Muted;
	}

	public double[] getRTP_streamBegin() {
		return RTP_streamBegin;
	}

	public double[] getRTP_isDelayed() {
		return RTP_isDelayed;
	}

	public double[] getRTP_isReordered() {
		return RTP_isReordered;
	}

	public double[] getTimeStamp() {
		return TimeStamp;
	}

	public double[] getMedia_BufSize() {
		return Media_BufSize;
	}

	public double[] getIATJitter() {
		return IATJitter;
	}

	public double[] getNS_speechPowerIn() {
		return NS_speechPowerIn;
	}

	public double[] getNS_speechPowerOut() {
		return NS_speechPowerOut;
	}

	public double[] getNS_noisePowerIn() {
		return NS_noisePowerIn;
	}

	public double[] getNS_noisePowerOut() {
		return NS_noisePowerOut;
	}

	public double[] getAGC_speechLevelOut() {
		return AGC_speechLevelOut;
	}

	public double[] getAGC_noiseLevelOut() {
		return AGC_noiseLevelOut;
	}

	public double[] getAGC_vadState() {
		return AGC_vadState;
	}
	
}
