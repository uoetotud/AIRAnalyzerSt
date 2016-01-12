package com.citrix.analyzerservice.dtcollector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.model.CacheItem;

@SuppressWarnings("rawtypes")
public class DtCollector {
	
	private static final Logger logger = Logger.getLogger(DtCollector.class);
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	@SuppressWarnings("unchecked")
	public List<LocalDbConference> getConferenceList(String size, String from, String to) {
		
		List<LocalDbConference> conferenceList = null;

		if (Main.cacheIsEnabled) {
			String key = "ConferenceList";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference list cached - return directly.");
				
				conferenceList = (List<LocalDbConference>) Main.cache.fetch(key).getCacheObject();
			} else {
				logger.info("Conference list NOT cached.");
				
				conferenceList = ldc.findConferenceList();
				if (conferenceList != null && !conferenceList.isEmpty()) {
					logger.info("Collected " + conferenceList.size() + " conferences.");
					Main.cache.put(key, new CacheItem(conferenceList, System.currentTimeMillis()));
				} else {
					logger.info("No channels found.");
					return null;
				}
			}
		} else {
			conferenceList = ldc.findConferenceList();
			if (conferenceList != null && !conferenceList.isEmpty())
				logger.info("Collected " + conferenceList.size() + " conferences.");
			else {
				logger.info("No channels found.");
				return null;
			}
		}
		
		if (size.equalsIgnoreCase("all") && from.equalsIgnoreCase("any") && to.equalsIgnoreCase("any"))		
			return conferenceList;
		else
			return filter(conferenceList, size, from, to);
	}
	
	@SuppressWarnings("unchecked")
	public LocalDbConference getConferenceSummary(String confId) {
		
		LocalDbConference conference = null;
		
		if (Main.cacheIsEnabled) {
			String key = confId + "_summary";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference " + confId + " summary cached - return directly.");
				
				conference = (LocalDbConference) Main.cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " summary NOT cached.");
				
				conference = ldc.findConference(confId, false);
				if (conference != null) {
					logger.info("Collected conference " + confId + " summary.");
					Main.cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
				}					
			}
		} else {
			conference = ldc.findConference(confId, false);
			if (conference != null)
				logger.info("Collected conference " + confId + " summary.");
		}			
		
		return conference;
	}
	
	@SuppressWarnings("unchecked")
	public LocalDbConference getConferenceDetails(String confId) {
		
		LocalDbConference conference = null;
		
		if (Main.cacheIsEnabled) {
			String key = confId + "_details";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference " + confId + " details cached - return directly.");
				
				conference = (LocalDbConference) Main.cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " details NOT cached.");
				
				conference = ldc.findConference(confId, true);
				if (conference != null) {
					logger.info("Collected conference " + confId + " details.");
					Main.cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
				}
			}
		} else {
			conference = ldc.findConference(confId, true);
			if (conference != null)
				logger.info("Collected conference " + confId + " details.");
		}	
		
		return conference;
	}
	
	@SuppressWarnings("unchecked")
	public List<LocalDbChannel> getConfChannels(String confId) {
		
		List<LocalDbChannel> channels = null;

		if (Main.cacheIsEnabled) {
			String key = confId + "_channels";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference " + confId + " channels cached - return directly.");
				
				channels = (List<LocalDbChannel>) Main.cache.fetch(key).getCacheObject();		
			} else {
				logger.info("Conference " + confId + " channels NOT cached.");
				
				channels = ldc.findConfChannels(confId);
				if (channels != null && !channels.isEmpty()) {
					logger.info("Collected " + channels.size() + " channels for conference " + confId + ".");
					Main.cache.put(key, new CacheItem(channels, System.currentTimeMillis()));
				} else
					logger.info("No channels found.");
			}
		} else {
			channels = ldc.findConfChannels(confId);
			if (channels != null && !channels.isEmpty())
				logger.info("Collected " + channels.size() + " channels for conference " + confId + ".");
			else
				logger.info("No channels found.");
		}		
		
		return channels;
	}
	
	@SuppressWarnings("unchecked")
	public LocalDbChannel getChannelSummary(String chanId) {
		
		LocalDbChannel channel = null;
		
		if (Main.cacheIsEnabled) {
			String key = chanId + "_summary";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference " + chanId + " summary cached - return directly.");
				
				channel = (LocalDbChannel) Main.cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " summary NOT cached.");
				
				channel = ldc.findChannel(null, chanId, false);
				if (channel != null) {
					logger.info("Collected channel " + chanId + " summary.");
					Main.cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
				}
			}
		} else {
			channel = ldc.findChannel(null, chanId, false);
			if (channel != null)
				logger.info("Collected channel " + chanId + " summary.");
		}		
		
		return channel;
	}
	
	@SuppressWarnings("unchecked")
	public LocalDbChannel getChannelDetails(String chanId) {
		
		LocalDbChannel channel = null;
		
		if (Main.cacheIsEnabled) {
			String key = chanId + "_details";
			if (Main.cache != null && Main.cache.contains(key)) {
				logger.info("Conference " + chanId + " details cached - return directly.");
				
				channel = (LocalDbChannel) Main.cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " details NOT cached.");
				
				channel = ldc.findChannel(null, chanId, true);
				if (channel != null) {
					logger.info("Collected channel " + chanId + " details.");
					Main.cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
				}
			}
		} else {
			channel = ldc.findChannel(null, chanId, true);
			if (channel != null)
				logger.info("Collected channel " + chanId + " details.");
		}
		
		return channel;
	}

	private List<LocalDbConference> filter(List<LocalDbConference> confList, String size, String from, String to) {
		List<LocalDbConference> subConfList = new LinkedList<LocalDbConference>();
		LocalDbConference conf = null;
		
		int count = size.equalsIgnoreCase("all") ? confList.size() : Integer.parseInt(size);
		if (count != confList.size())
			logger.info("Display " + count + " conferences.");
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
		LocalDateTime after = from.equalsIgnoreCase("any") ? null : LocalDateTime.parse(from, formatter);
		LocalDateTime before = to.equalsIgnoreCase("any") ? null : LocalDateTime.parse(to, formatter);
		if (after != null || before != null)
			logger.info("Display conferences from " + from + " to " + to + ".");
		
		for (int i=0; i<confList.size(); i++) {
			conf = confList.get(i);
			
			if ((after != null && conf.getTimestamp().isBefore(after)) || before != null && conf.getTimestamp().isAfter(before))
				continue;
			
			if (subConfList.size() < count)
				subConfList.add(conf);
			else
				break;
		}
		
		return subConfList;
	}
	
}
