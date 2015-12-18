package com.citrix.analyzerservice.dtcollector;

import java.util.List;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;

public class DtCollector {
	
	private static final Logger logger = Logger.getLogger(DtCollector.class);
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	public List<LocalDbConference> getConferenceList() {
		
		List<LocalDbConference> conferenceList = ldc.findConferenceList();		
		logger.info("Collected " + conferenceList.size() + " conferences.");
		
		return conferenceList;
	}
	
	public LocalDbConference getConferenceSummary(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, false);		
		logger.info("Collected conference " + confId + " summary.");
		
		return conference;
	}
	
	public LocalDbConference getConferenceDetails(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, true);
		logger.info("Collected conference " + confId + " details.");
		
		return conference;
	}
	
	public List<LocalDbChannel> getConfChannels(String confId) {
		
		List<LocalDbChannel> channelList = ldc.findConfChannels(confId);		
		logger.info("Collected " + channelList.size() + " channels for conference " + confId + ".");
		
		return channelList;
	}
	
	public LocalDbChannel getChannelSummary(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, false);		
		logger.info("Collected channel " + chanId + " summary.");
		
		return channel;
	}
	
	public LocalDbChannel getChannelDetails(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, true);		
		logger.info("Collected channel " + chanId + " details.");
		
		return channel;
	}

}
