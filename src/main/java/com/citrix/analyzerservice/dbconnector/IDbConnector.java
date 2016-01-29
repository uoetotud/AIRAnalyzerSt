package com.citrix.analyzerservice.dbconnector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.citrix.analyzerservice.model.ChannelStats;

/**
 * @author Xi Luo
 *
 */
public interface IDbConnector {

	Map<String, List<String>> findUpdatedConfIds();
	
	List<LocalDbConference> findConferenceList();
	
	LocalDbConference findConference(String confId, boolean showAll);	
	List<LocalDbChannel> findConfChannels(String folderPath);
	LocalDateTime findConferenceTimestamp(String folder);
	List<String> findConfChannelIds(String path);
	
	LocalDbChannel findChannel(String confId, String chanId, boolean showAll);
	ChannelStats findChannelStats(String confId, String chanId);
	LocalDateTime findChannelTimestamp(String confId, String chanId);
	
	List<List<String>> readFile(String path, String delimiter, String size);
	boolean writeFile(String type, String[] content);	
	boolean updateFile(String type, String confId);
}
