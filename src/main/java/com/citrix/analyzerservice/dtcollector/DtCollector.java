package com.citrix.analyzerservice.dtcollector;

import java.util.List;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;

public class DtCollector {
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	public List<LocalDbConference> getConferenceList() {
		
		List<LocalDbConference> conferenceList = ldc.findConferenceList();
		
		return conferenceList;
	}
	
	public LocalDbConference getConferenceSummary(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, false);
		
		return conference;
	}
	
	public LocalDbConference getConferenceDetails(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, true);
		
		return conference;
	}
	
	public List<LocalDbChannel> getConfChannels(String confId) {
		
		List<LocalDbChannel> channelList = ldc.findConfChannels(confId);
		
		return channelList;
	}
	
	public LocalDbChannel getChannelSummary(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, false);
		
		return channel;
	}
	
	public LocalDbChannel getChannelDetails(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, true);
		
		return channel;
	}

}
