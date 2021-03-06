package com.citrix.analyzerservice.dtprocessor;

import java.util.List;

import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ConferenceScore;

/**
 * @author Xi Luo
 *
 */
public interface IDtProcessor {
	
	boolean checkUpdate();
	
	ConferenceScore calcConferenceScore(String confIds, List<ChannelScore> chanScores);
	
	ChannelScore calcChannelScore(String confId, String chanId);
	
	boolean updateConfList(List<String> confIds, List<ConferenceScore> confScores);
	
	boolean updateChanList(String confId, List<ChannelScore> chanScores);
	
}
