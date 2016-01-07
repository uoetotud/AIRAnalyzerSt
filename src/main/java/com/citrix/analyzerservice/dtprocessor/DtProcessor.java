package com.citrix.analyzerservice.dtprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtcollector.DtCollector;
import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;

public class DtProcessor extends TimerTask implements IDtProcessor {
	
	// Constants definitions
	final double cVADLevelDiff = 20;
	final int cQuantumSizeMs = 10;
	final int cTime = 1; // time in sec
	
	private static final Logger logger = Logger.getLogger(DtProcessor.class);
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	Map<String, List<String>> updatedConfIds = null;
	
	public DtProcessor() {}
	
	public void run() {
		logger.info("Start data processor...");
		
		if (checkUpdate()) {
			logger.info("Data updated.");
			
			// remove conference list from cache
			if (DtCollector.cache != null && DtCollector.cache.contains("ConferenceList")) {
				logger.info("Conference list in cache removed.");
				DtCollector.cache.remove("ConferenceList");
			}
			
			logger.info("Start processing...");
			
			if (updatedConfIds.containsKey("newConfIds")) {
				List<String> newConfIds = updatedConfIds.get("newConfIds");
				logger.info("Found " + newConfIds.size() + " new conferences.");
				
				List<ConferenceScore> confScores = new ArrayList<ConferenceScore>();
				ConferenceScore confScore = new ConferenceScore(0, 0);
				
				for (String newConfId : newConfIds) {
					
//					List<LocalDbChannel> channels = ldc.findConfChannels(newConfId);
					List<String> channelIds = ldc.getConfChannelIds(newConfId);
					List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
					
					for (String channelId : channelIds) {					
						ChannelScore chanScore = calChannelScore(newConfId, channelId);						
						chanScores.add(chanScore);
						
						// update channel with new score in cache
						if (DtCollector.cache != null && DtCollector.cache.size() > 0)
							updateChanScoreInCache(channelId, chanScore);
					}
					
					// update channel file
					if (!updateChanList(newConfId, chanScores))
						logger.error("Cannot update ChanList.");
					
					confScore = calConferenceScore(newConfId, chanScores);
					confScores.add(confScore);
					
					// update conference with new score in cache
					if (DtCollector.cache != null && DtCollector.cache.size() > 0)
						updateConfScoreInCache(newConfId, confScore);
				}		
				
				// update conference file
				if (!updateConfList(newConfIds, confScores))
					logger.error("Cannot update ConfList.");
			}
			
			if (updatedConfIds.containsKey("oldConfIds")) {
				List<String> oldConfIds = updatedConfIds.get("oldConfIds");
				logger.info("Found " + oldConfIds.size() + " deprecated conferences.");
				
				for (String oldConfId : oldConfIds) {
					
					// remove conference (including its channels) from cache
					if (DtCollector.cache != null && DtCollector.cache.size() > 0)
						removeConfernceFromCache(oldConfId);
					
					// update conference file & channel file
					if (!ldc.updateFile("conference", oldConfId))
						logger.error("Cannot delete items in ConfList.");
					if (!ldc.updateFile("channel", oldConfId))
						logger.error("Cannot delete items in ChanList.");
				}
			}
			
			logger.info("Processing completed.");
		} else {
			logger.info("No data update.");
		}		
		
		logger.info("DataProcessor stopped.");		
	}
	
	@Override
	public boolean checkUpdate() {
		
		updatedConfIds = ldc.findUpdatedConfIds();
		
		if (updatedConfIds == null || updatedConfIds.isEmpty())
			return false;
		
		return true;
	}
	
	@Override
	public boolean updateConfList(List<String> confIds, List<ConferenceScore> confScores) {
		
		if (confIds.size() != confScores.size())
			return false;
		
		int size = confIds.size();
		String[] conferenceItems = new String[size];
		
		for (int i=0; i<size; i++) {
			String confId = confIds.get(i);
			conferenceItems[i] = confId + ", " + ldc.findConferenceTimestamp(confId) + ", " + ldc.findConfChannels(confId).size() + ", " +
					Integer.toString(confScores.get(i).getAvgPLIndicator()) + ", " + Integer.toString(confScores.get(i).getAvgLevelIndicator());
		}
		
		if (ldc.writeFile("conference", conferenceItems))
			return true;
		else
			return false;
	}
	
	@Override
	public boolean updateChanList(String confId, List<ChannelScore> chanScores) {
		
		List<LocalDbChannel> channels = ldc.findConfChannels(confId);
		if (channels == null || channels.isEmpty())
			return false;
		
		if (channels.size() != chanScores.size())
			return false;
		
		int size = channels.size();
		String[] channelItems = new String[size];
		
		for (int i=0; i<size; i++) {
			channelItems[i] = confId + ", " + channels.get(i).getUuid() + ", " + Integer.toString(chanScores.get(i).getAvgPLIndicator()) + ", " +
					Integer.toString(chanScores.get(i).getAvgLevelIndicator()) + ", " + Double.toString(chanScores.get(i).getAvgPacketLoss());
		}
		
		if (ldc.writeFile("channel", channelItems))
			return true;
		else
			return false;
	}
	
	@Override
	public ConferenceScore calConferenceScore(String confId, List<ChannelScore> chanScores) {
		
		List<LocalDbChannel> channels = ldc.findConfChannels(confId);
		if (channels == null || channels.isEmpty())
			return null;
		
		if (channels.size() != chanScores.size()) {
			logger.error("Channels number and ChannelScores number not matched.");
			return null;
		}
		
		ConferenceScore score = new ConferenceScore(0, 0);
		
		for (int i=0; i<channels.size(); i++) {			
			ChannelScore chanScore = chanScores.get(i);
			
			if (chanScore.getAvgLevelIndicator() > score.getAvgLevelIndicator())
				score.setAvgLevelIndicator(chanScore.getAvgLevelIndicator());
			
			if (chanScore.getAvgPLIndicator() > score.getAvgPLIndicator())
				score.setAvgPLIndicator(chanScore.getAvgPLIndicator());			
		}
		
		return score;
	}
	
	@Override
	public ChannelScore calChannelScore(String confId, String chanId) {
		
		int i = 0, j = 0, size = 0, counter = 0;
		
		ChannelStats stats = ldc.findChannelStats(confId, chanId);
		if (stats == null)
			return null;
		
		double[] seqNr = stats.getStrProcessor().getSeqNr();
		double[] speechValues = stats.getStrProcessor().getNS_speechPowerOut();
		double[] noiseValues = stats.getStrProcessor().getNS_noisePowerOut();
		
		size = speechValues.length;
		
		// convert speech and noise values into levels
		double[] speechLevels = new double[size];
		for (i=0; i<size; i++)
			speechLevels[i] = 10 * Math.log10( (speechValues[i] + 0.0001) / (32768 * 32768));
		
		double[] noiseLevels = new double[size];
		for (i=0; i<size; i++)
			noiseLevels[i] = 10 * Math.log10( (noiseValues[i] + 0.0001) / (32768 * 32768));
		
		// determine coarse activity information (required for level estimation)
		boolean[] vadState = new boolean[size];
		for (i=0; i<size; i++) {
			double tmpLevelDiff = speechLevels[i] - noiseLevels[i];
			if (tmpLevelDiff > cVADLevelDiff)
				vadState[i] = true;
			else
				vadState[i] = false;
		}
		
		// average channel-based speech and noise levels
		// -----------------------------------------------------------------------------------------
		/* overall results */
		@SuppressWarnings("unused")
		double avgSpeechLevel = 0;
		@SuppressWarnings("unused")
		double avgNoiseLevel = 0;
		counter = 0;
		
		for (i=0; i<size; i++) {
			if (vadState[i]) {
				counter++;
				avgSpeechLevel += speechLevels[i];
			}
			
			avgNoiseLevel += noiseLevels[i];
		}
		
		// normalize results
		if (counter > 0)
			avgSpeechLevel /= counter;
		if (noiseValues.length > 0)
			avgNoiseLevel /= size;
		/* end overall results */
		
		/* short-time results */
		int nPackets = (int) (cTime * 1000 / cQuantumSizeMs);
		int nBlocks = (int) Math.floor(speechValues.length / nPackets);
		List<Double> shortTimeSpeechLevel = new ArrayList<Double>();
		List<Double> shortTimeNoiseLevel = new ArrayList<Double>();
		double tmpSTSpeechLevel = 0;
		double tmpSTNoiseLevel = 0;
		counter = 0;
		
		for (i=0; i < nBlocks; i++) {
			for (j=0; j < nPackets; j++) {
				int idx = j + i * nPackets;
				if (vadState[idx]) {
					counter++;
					tmpSTSpeechLevel += speechLevels[idx];
				}
				tmpSTNoiseLevel += noiseLevels[idx];
			}
			
			// short-time noise levels
			tmpSTNoiseLevel /= nPackets;
			shortTimeNoiseLevel.add(tmpSTNoiseLevel);
			// short-time speech level
			if (tmpSTSpeechLevel <= 1e-10) {
		    	shortTimeSpeechLevel.add(tmpSTNoiseLevel);
			} else {
				if (counter > 0)
					tmpSTSpeechLevel /= counter;
				shortTimeSpeechLevel.add(tmpSTSpeechLevel);
			}
			// reset tmp variables
			tmpSTSpeechLevel = 0;
			tmpSTNoiseLevel = 0;
			counter = 0;
		}
		/* short-time results */
		// -----------------------------------------------------------------------------------------
		
		// average channel-based loss rates
		// -----------------------------------------------------------------------------------------
		/* overall loss rates */
		double avgPacketLoss = 0;
		int tmpAvgLoss = 0;
		for (i=0; i<size-1; i++) {
			int tmpSeqNrDiff = (int) (seqNr[i+1] - seqNr[i]);
			if (tmpSeqNrDiff != 1) {
				if (tmpSeqNrDiff > 1) {
					tmpAvgLoss += (tmpSeqNrDiff - 1);
				} else {
					tmpAvgLoss++;
				}
			}
			
		}
		
		// normalize results
		avgPacketLoss = (double) tmpAvgLoss / (size + tmpAvgLoss);
		/* overall loss rates */
		
		/* short-time loss rates */
		// commented out...
		/* short-time loss rates */
		// -----------------------------------------------------------------------------------------
		
		// indicators for speech/noise levels
		// -----------------------------------------------------------------------------------------
		/* global indicator */
		int avgLevelIndicator = 0;
		// commented out...
		/* global indicator */
		
		/* short-time indicator */
		List<Integer> shortTimeLevelIndicator = new ArrayList<Integer>();
		double stSpeechNoiseDiff = 0;
		int shortTimeLevelIndicatorSum = 0;
		
		for (i=0; i<nBlocks; i++) {
			stSpeechNoiseDiff = shortTimeSpeechLevel.get(i) - shortTimeNoiseLevel.get(i);
			if (stSpeechNoiseDiff <= Double.MIN_VALUE) {
				if ((shortTimeNoiseLevel.get(i) >= -60) && (shortTimeNoiseLevel.get(i) < -45)) {
					shortTimeLevelIndicator.add(1);
				} else if (shortTimeNoiseLevel.get(i) >= 45) {
					shortTimeLevelIndicator.add(2);
				} else {
					shortTimeLevelIndicator.add(0);
				}
			} else {
				if (stSpeechNoiseDiff >= 10 && stSpeechNoiseDiff < 20) {
					shortTimeLevelIndicator.add(1);
				} else if (stSpeechNoiseDiff < 10) {
					shortTimeLevelIndicator.add(2);
				} else {
					shortTimeLevelIndicator.add(0);
				}
			}
			
			shortTimeLevelIndicatorSum += shortTimeLevelIndicator.get(shortTimeLevelIndicator.size()-1);
				
		}
		
		if (nBlocks > 0)
			avgLevelIndicator = shortTimeLevelIndicatorSum / nBlocks;
		/* short-time indicator */
		// -----------------------------------------------------------------------------------------
		
		// indicators for packet loss
		// -----------------------------------------------------------------------------------------
		/* global indicator */
		int avgPLIndicator = 0;
		
		if (avgPacketLoss >= 0.05 && avgPacketLoss < 0.1) {
			avgPLIndicator = 1;
		} else if (avgPacketLoss >= 0.1) {
			avgPLIndicator = 2;
		}		
		/* global indicator */
		
		/* short-time indicator */
		// commented out...
		/* short-time indicator */
		// -----------------------------------------------------------------------------------------		
		
		ChannelScore score = new ChannelScore(avgPLIndicator, avgLevelIndicator, avgPacketLoss*100);
		
		return score;
	}
	
	private void removeConfernceFromCache(String oldConfId) {
		// remove conference summary cache
		if (DtCollector.cache.contains(oldConfId+"_summary")) {
			DtCollector.cache.remove(oldConfId+"_summary");
			logger.info(oldConfId + "_summary in cache removed.");
		}
		
		// remove conference details cache
		if (DtCollector.cache.contains(oldConfId+"_details")) {
			DtCollector.cache.remove(oldConfId+"_details");
			logger.info(oldConfId + "_details in cache removed.");
		}
		
		// remove conference channels cache
		if (DtCollector.cache.contains(oldConfId+"_channels")) {
			DtCollector.cache.remove(oldConfId+"_channels");
			logger.info(oldConfId + "_channels in cache removed.");
		}
		
		// remove each channel cache of this conference
		List<String> channelIds = ldc.getConfChannelIds(oldConfId);
		for (String channelId : channelIds) {
			if (DtCollector.cache.contains(channelId+"_summary")) {
				DtCollector.cache.remove(channelId+"_summary");
				logger.info(channelId + "_summary in cache removed.");
			}
			
			if (DtCollector.cache.contains(channelId+"_details")) {
				DtCollector.cache.remove(channelId+"_details");
				logger.info(channelId + "_details in cache removed.");
			}
		}
	}
	
	private void updateConfScoreInCache(String confId, ConferenceScore confScore) {
		// update conference summary cache with new score
		if (DtCollector.cache.contains(confId + "_summary")) {
			((LocalDbConference) ((CacheItem<?>) DtCollector.cache.get(confId + "_summary")).getCacheObject()).setScore(confScore);
			logger.info(confId + "_summary in cache score updated.");
		}
		
		// update conference details cache with new score
		if (DtCollector.cache.contains(confId + "_details")) {
			((LocalDbConference) ((CacheItem<?>) DtCollector.cache.get(confId + "_details")).getCacheObject()).setScore(confScore);
			logger.info(confId + "_details in cache score updated.");
		}
	}
	
	private void updateChanScoreInCache(String channelId, ChannelScore chanScore) {
		// update channel summary cache with new score
		if (DtCollector.cache.contains(channelId + "_summary")) {
			((LocalDbChannel) ((CacheItem<?>) DtCollector.cache.get(channelId + "_summary")).getCacheObject()).setScore(chanScore);
			logger.info(channelId + "_summary in cache score updated.");
		}
		
		// update channel details cache with new score
		if (DtCollector.cache.contains(channelId + "_details")) {
			((LocalDbChannel) ((CacheItem<?>) DtCollector.cache.get(channelId + "_details")).getCacheObject()).setScore(chanScore);
			logger.info(channelId + "_details in cache score updated.");
		}
	}

}
