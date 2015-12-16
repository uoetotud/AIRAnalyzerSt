package com.citrix.analyzerservice.model;

public class Mixer {

	private double[] quantum;
	private double[] nConferenceId;
	private double[] nSpeakers;
	
	public Mixer(double[] quantum, double[] nConferenceId, double[] nSpeakers) {
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
