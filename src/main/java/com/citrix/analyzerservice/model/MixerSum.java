package com.citrix.analyzerservice.model;

public class MixerSum {

	private double[] quantum;
	private double[] nConferenceId;
	private double[] nSpeakers;
	
	public MixerSum(double[] quantum, double[] nConferenceId, double[] nSpeakers) {
		this.quantum = quantum;
		this.nConferenceId = nConferenceId;
		this.nSpeakers = nSpeakers;
	}

	public double[] getQuantum() {
		return quantum;
	}

	public double[] getnConferenceId() {
		return nConferenceId;
	}

	public double[] getnSpeakers() {
		return nSpeakers;
	}

	
	
}
