package com.citrix.analyzerservice.model;

import java.time.LocalDateTime;

public class ConferenceStats {
	
	private Mixer mixer;
	
	public ConferenceStats(Mixer mixer) {
		this.mixer = mixer;
	}

	public Mixer getMixer() {
		return mixer;
	}
	
	

}
