package com.citrix.analyzerservice.dbconnector;

import java.time.LocalDateTime;

import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;

/**
 * @author Xi Luo
 *
 */
public class LocalDbChannel {

	private String uuid;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private ChannelStats stats;
	private ChannelScore score;
	
	public LocalDbChannel(String uuid, LocalDateTime startTime, LocalDateTime endTime, ChannelStats stats, ChannelScore score) {
		this.uuid = uuid;
		this.startTime = startTime;
		this.endTime = endTime;
		this.stats = stats;
		this.score = score;
	}

	public String getUuid() {
		return uuid;
	}
	
	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public ChannelStats getStats() {
		return stats;
	}

	public ChannelScore getScore() {
		return score;
	}
	
	public void setScore(ChannelScore score) {
		this.score = score;
	}
	
}
