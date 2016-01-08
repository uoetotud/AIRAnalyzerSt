package com.citrix.analyzerservice.dtcollector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.util.Cache;
import com.citrix.analyzerservice.util.Config;

@SuppressWarnings("rawtypes")
public class DtCollector {
	
	private static final Logger logger = Logger.getLogger(DtCollector.class);
	
	// Get cache configurations
	private static Map<String, String> configs = new Config().getPropValues();
	private static String cacheEnabled = configs.get("Cache_Enable");
	private static String cacheType = configs.get("Cache_Type");
	private static String cacheTimeOut = configs.get("Cache_TimeOut");
	private static String cacheCleanInterval = configs.get("Cache_Clean_Interval");
	private static String cacheSize = configs.get("Cache_Size");
	// End get configurations
	
	public static Cache<String, CacheItem> cache = null;
	private static boolean cacheIsEnabled = false;
	
	// Create cache if enabled (put here to ensure ONLY ONCE execution)
	static {
		if (cacheEnabled.equalsIgnoreCase("true")) {
			logger.debug("Cache enabled.");
			cache = new Cache<String, CacheItem>(cacheType, Long.parseLong(cacheTimeOut), Long.parseLong(cacheCleanInterval), Integer.parseInt(cacheSize));
			cacheIsEnabled = true;
		} else {
			logger.debug("Cache NOT enabled.");
		}
	}
	// End create cache
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	@SuppressWarnings("unchecked")
	public List<LocalDbConference> getConferenceList(String size, String from, String to) {
		
		List<LocalDbConference> conferenceList = null;

		if (cacheIsEnabled) {
			String key = "ConferenceList";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference list cached - return directly.");
				
				conferenceList = (List<LocalDbConference>) cache.fetch(key).getCacheObject();
			} else {
				logger.info("Conference list NOT cached.");
				
				conferenceList = ldc.findConferenceList();
				if (conferenceList != null && !conferenceList.isEmpty()) {
					logger.info("Collected " + conferenceList.size() + " conferences.");
					cache.put(key, new CacheItem(conferenceList, System.currentTimeMillis()));
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
		
		if (cacheIsEnabled) {
			String key = confId + "_summary";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " summary cached - return directly.");
				
				conference = (LocalDbConference) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " summary NOT cached.");
				
				conference = ldc.findConference(confId, false);
				if (conference != null) {
					logger.info("Collected conference " + confId + " summary.");
					cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
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
		
		if (cacheIsEnabled) {
			String key = confId + "_details";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " details cached - return directly.");
				
				conference = (LocalDbConference) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " details NOT cached.");
				
				conference = ldc.findConference(confId, true);
				if (conference != null) {
					logger.info("Collected conference " + confId + " details.");
					cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
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

		if (cacheIsEnabled) {
			String key = confId + "_channels";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " channels cached - return directly.");
				
				channels = (List<LocalDbChannel>) cache.fetch(key).getCacheObject();		
			} else {
				logger.info("Conference " + confId + " channels NOT cached.");
				
				channels = ldc.findConfChannels(confId);
				if (channels != null && !channels.isEmpty()) {
					logger.info("Collected " + channels.size() + " channels for conference " + confId + ".");
					cache.put(key, new CacheItem(channels, System.currentTimeMillis()));
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
		
		if (cacheIsEnabled) {
			String key = chanId + "_summary";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + chanId + " summary cached - return directly.");
				
				channel = (LocalDbChannel) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " summary NOT cached.");
				
				channel = ldc.findChannel(null, chanId, false);
				if (channel != null) {
					logger.info("Collected channel " + chanId + " summary.");
					cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
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
		
		if (cacheIsEnabled) {
			String key = chanId + "_details";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + chanId + " details cached - return directly.");
				
				channel = (LocalDbChannel) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " details NOT cached.");
				
				channel = ldc.findChannel(null, chanId, true);
				if (channel != null) {
					logger.info("Collected channel " + chanId + " details.");
					cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
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
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
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
