package com.citrix.analyzerservice.model;

/**
 * @author Xi Luo
 *
 */
public class MixerSum {

	private double[] quantum;
	private double[] nConferenceId;
	private double[] nSpeakers;
	private double[] speaker1;
	private double[] speaker2;
	private double[] speaker3;
	private double[] speaker4;	

	public MixerSum(double[] quantum, double[] nConferenceId, double[] nSpeakers, 
			double[] speaker1, double[] speaker2, double[] speaker3, double[] speaker4) {
		this.quantum = quantum;
		this.nConferenceId = nConferenceId;
		this.nSpeakers = nSpeakers;
		this.speaker1 = speaker1;
		this.speaker2 = speaker2;
		this.speaker3 = speaker3;
		this.speaker4 = speaker4;
	}

	public double[] getQuantum() {
		return quantum;
	}

	public double[] getNConferenceId() {
		return nConferenceId;
	}

	public double[] getNSpeakers() {
		return nSpeakers;
	}

	public double[] getSpeaker1() {
		return speaker1;
	}
	
	public double[] getSpeaker2() {
		return speaker2;
	}
	
	public double[] getSpeaker3() {
		return speaker3;
	}
	
	public double[] getSpeaker4() {
		return speaker4;
	}
	
}
