package com.citrix.analyzerservice.dtprocessor;

import java.util.List;

import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ConferenceScore;

public interface IDtProcessor {
	
	boolean checkUpdate();
	
	ConferenceScore calConferenceScore(String confIds, List<ChannelScore> chanScores);
	
	ChannelScore calChannelScore(String confId, String chanId);
	
	boolean updateConfList(List<String> confIds, List<ConferenceScore> confScores);
	
	boolean updateChanList(String confId, List<ChannelScore> chanScores);
	
}
