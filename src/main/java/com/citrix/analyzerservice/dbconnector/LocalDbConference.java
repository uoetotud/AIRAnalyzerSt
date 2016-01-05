package com.citrix.analyzerservice.dbconnector;

import java.time.LocalDateTime;
import java.util.Collection;

import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ConferenceScore;
import com.citrix.analyzerservice.model.ConferenceStats;

public class LocalDbConference {

	private String uuid;
	private LocalDateTime timestamp;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private int channelNo;
	private ConferenceStats stats;
	private ConferenceScore score;
	private Collection<LocalDbChannel> channels;
	
	public LocalDbConference(String uuid, LocalDateTime timestamp, LocalDateTime startTime, LocalDateTime endTime, 
			int channelNo, ConferenceStats stats, ConferenceScore score, Collection<LocalDbChannel> channels) {
		this.uuid = uuid;
		this.timestamp = timestamp;
		this.startTime = startTime;
		this.endTime = endTime;
		this.channelNo = channelNo;
		this.stats = stats;
		this.score = score;
		this.channels = channels;
	}	

	public String getUuid() {
		return uuid;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public int getChannelNo() {
		return channelNo;
	}

	public ConferenceStats getStats() {
		return stats;
	}
	
	public void setStats(ConferenceStats stats) {
		this.stats = stats;
	}

	public ConferenceScore getScore() {
		return score;
	}

	public Collection<LocalDbChannel> getChannels() {
		return channels;
	}
	
}
