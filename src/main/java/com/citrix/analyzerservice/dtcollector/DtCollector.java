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
		
		if (!conferenceList.isEmpty())
			logger.info("Collected " + conferenceList.size() + " conferences.");
		else
			logger.info("No conference is found.");
		
		return conferenceList;
	}
	
	public LocalDbConference getConferenceSummary(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, false);
		
		if (conference != null)
			logger.info("Collected conference " + confId + " summary.");
		else
			logger.info("Conference not found.");			
		
		return conference;
	}
	
	public LocalDbConference getConferenceDetails(String confId) {
		
		LocalDbConference conference = ldc.findConference(confId, true);
		
		if (conference != null)
			logger.info("Collected conference " + confId + " details.");
		else
			logger.info("Conference not found.");
		
		return conference;
	}
	
	public List<LocalDbChannel> getConfChannels(String confId) {
		
		List<LocalDbChannel> channelList = ldc.findConfChannels(confId);
		
		if (!channelList.isEmpty())
			logger.info("Collected " + channelList.size() + " channels for conference " + confId + ".");
		else
			logger.info("No channel is found.");
		
		return channelList;
	}
	
	public LocalDbChannel getChannelSummary(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, false);
		
		if (channel != null)
			logger.info("Collected channel " + chanId + " summary.");
		else
			logger.info("Channel not found.");
		
		return channel;
	}
	
	public LocalDbChannel getChannelDetails(String chanId) {
		
		LocalDbChannel channel = ldc.findChannel(null, chanId, true);		

		if (channel != null)
			logger.info("Collected channel " + chanId + " details.");
		else
			logger.info("Channel not found.");
		
		return channel;
	}

}
