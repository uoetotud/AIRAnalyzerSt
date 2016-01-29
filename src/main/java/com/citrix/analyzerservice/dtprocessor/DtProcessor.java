package com.citrix.analyzerservice.dtprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;

/**
 * @author Xi Luo
 *
 */
public class DtProcessor extends TimerTask implements IDtProcessor {
	
	/* define constants */
	final double cVADLevelDiff = 20;
	final int cQuantumSizeMs = 10;
	final int cTime = 1; // time in second
	
	private static final Logger logger = Logger.getLogger(DtProcessor.class);
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	Map<String, List<String>> updatedConfIds = null;
	
	public DtProcessor() {}
	
	/**
	* This method is the "main" function of DtProcessor.
	* It is responsible for check file updates, calculate conference/channel scores, 
	* update ConfList.txt, ChanList.txt files and cache objects if modified.
	*/
	public void run() {
		logger.info("Start data processor...");
		
		if (checkUpdate()) {
			logger.info("Data updated.");
			
			/* remove conference list from cache */
			if (Main.cache != null && Main.cache.contains("ConferenceList")) {
				logger.info("Conference list in cache removed.");
				Main.cache.remove("ConferenceList");
			}
			
			logger.info("Start processing...");
			
			if (updatedConfIds.containsKey("newConfIds")) {
				List<String> newConfIds = updatedConfIds.get("newConfIds");
				logger.info(new StringBuilder("Found ").append(Integer.toString(newConfIds.size())).append(" new conference."));
				
				List<ConferenceScore> confScores = new ArrayList<ConferenceScore>();
				ConferenceScore confScore = new ConferenceScore(0, 0);
				
				for (String newConfId : newConfIds) {

					List<String> channelIds = ldc.findConfChannelIds(newConfId);
					List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
					
					for (String channelId : channelIds) {					
						ChannelScore chanScore = calcChannelScore(newConfId, channelId);						
						chanScores.add(chanScore);
						
						/* update channel with new score in cache */
						if (Main.cache != null && Main.cache.size() > 0) {
							updateChanScoreInCache(channelId, chanScore);
							logger.debug("Updated channel in cache with score.");
						}
					}
					
					/* update channel file */
					if (!updateChanList(newConfId, chanScores))
						logger.error("Cannot update ChanList.");
					else
						logger.debug("Added new channel in ChanList.txt file.");
					
					confScore = calcConferenceScore(newConfId, chanScores);
					confScores.add(confScore);
					
					/* update conference with new score in cache */
					if (Main.cache != null && Main.cache.size() > 0) {
						updateConfScoreInCache(newConfId, confScore);
						logger.debug("Updated conference in cache with score.");
					}
				}		
				
				/* update conference file */
				if (!updateConfList(newConfIds, confScores))
					logger.error("Cannot update ConfList.");
				else
					logger.debug("Added new conference in ConfList.txt file.");
			}
			
			if (updatedConfIds.containsKey("oldConfIds")) {
				List<String> oldConfIds = updatedConfIds.get("oldConfIds");
				logger.info(new StringBuilder("Found ").append(Integer.toString(oldConfIds.size())).append(" deprecated conference."));
				
				for (String oldConfId : oldConfIds) {
					
					/* remove conference (including its channels) from cache */
					if (Main.cache != null && Main.cache.size() > 0) {
						removeConfernceFromCache(oldConfId);
						logger.debug("Removed conference with its channels in cache.");
					}
					
					/* update conference file */
					if (!ldc.updateFile("conference", oldConfId))
						logger.error("Cannot delete items in ConfList.");
					else
						logger.debug("Removed conference in ConfList.txt file.");
					
					/* update channel file */
					if (!ldc.updateFile("channel", oldConfId))
						logger.error("Cannot delete items in ChanList.");
					else
						logger.debug("Removed channel in ChanList.txt file.");
				}
			}
			
			logger.info("Processing completed.");
		} else {
			logger.info("No data update.");
		}		
		
		logger.info("DataProcessor stopped.");		
	}
	
	/**
	* This method is used to check if files are updated in the directory.
	* @return boolean: files are updated or not
	*/
	@Override
	public boolean checkUpdate() {
		
		updatedConfIds = ldc.findUpdatedConfIds();
		
		if (updatedConfIds == null || updatedConfIds.isEmpty())
			return false;
		
		return true;
	}
	
	/**
	* This method is used to update ConfList.txt file.
	* @param confIds: the IDs of new conferences in list
	* @param confScores: the scores of new conference in list
	* @return boolean: update succeeds or not
	*/
	@Override
	public boolean updateConfList(List<String> confIds, List<ConferenceScore> confScores) {
		
		if (confIds.size() != confScores.size())
			return false;
		
		int size = confIds.size();
		String[] conferenceItems = new String[size];
		
		for (int i=0; i<size; i++) {
			String confId = confIds.get(i);
			conferenceItems[i] = new StringBuilder(confId).append(", ").append(ldc.findConferenceTimestamp(confId))
					.append(", ").append(Integer.toString(ldc.findConfChannels(confId).size())).append(", ")
					.append(Integer.toString(confScores.get(i).getAvgPLIndicator())).append(", ")
					.append(Integer.toString(confScores.get(i).getAvgLevelIndicator())).toString();
		}
		
		if (ldc.writeFile("conference", conferenceItems))
			return true;
		else
			return false;
	}
	
	/**
	* This method is used to update ChanList.txt file.
	* @param confIds: the IDs of new conferences in list
	* @param chanScores: the scores of channels of new conferences in list
	* @return boolean: update succeeds or not
	*/
	@Override
	public boolean updateChanList(String confId, List<ChannelScore> chanScores) {
		
		List<String> channelIds = ldc.findConfChannelIds(confId);
		if (channelIds == null || channelIds.isEmpty())
			return false;
		
		if (channelIds.size() != chanScores.size())
			return false;
		
		int size = channelIds.size();
		String[] channelItems = new String[size];
		
		for (int i=0; i<size; i++) {
			channelItems[i] = new StringBuilder(confId).append(", ").append(channelIds.get(i)).append(", ")
					.append(Integer.toString(chanScores.get(i).getAvgPLIndicator())).append(", ")
					.append(Integer.toString(chanScores.get(i).getAvgLevelIndicator())).append(", ")
					.append(Double.toString(chanScores.get(i).getAvgPacketLoss())).toString();
		}
		
		if (ldc.writeFile("channel", channelItems))
			return true;
		else
			return false;
	}
	
	/**
	* This method is used to calculate conference scores.
	* @param confId: the ID of conference
	* @param chanScores: the scores of channels of conference in list
	* @return ConferenceScore: conference scores
	*/
	@Override
	public ConferenceScore calcConferenceScore(String confId, List<ChannelScore> chanScores) {
		
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
	
	/**
	* This method is used to calculate channel scores.
	* @param confId: the ID of conference for the channel
	* @param chanId: the ID of channel
	* @return ChannelScore: channel scores
	*/
	@Override
	public ChannelScore calcChannelScore(String confId, String chanId) {
		
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
	
	/**
	* This method is used to remove conference object from cache.
	* @param oldConfId: the ID of conference that is removed
	*/
	private void removeConfernceFromCache(String oldConfId) {
		String rm = " in cache removed.";
		String ocs = new StringBuilder(oldConfId).append("_summary").toString();
		String ocd = new StringBuilder(oldConfId).append("_details").toString();
		String occ = new StringBuilder(oldConfId).append("_channels").toString();
		
		// remove conference summary cache
		if (Main.cache.contains(ocs)) {
			Main.cache.remove(ocs);
			logger.info(new StringBuilder(ocs).append(rm));
		}
		
		// remove conference details cache
		if (Main.cache.contains(ocd)) {
			Main.cache.remove(ocd);
			logger.info(new StringBuilder(ocd).append(rm));
		}
		
		// remove conference channels cache
		if (Main.cache.contains(occ)) {
			Main.cache.remove(occ);
			logger.info(new StringBuilder(occ).append(rm));
		}
		
		// remove each channel cache of this conference
		List<String> channelIds = ldc.findConfChannelIds(oldConfId);
		for (String channelId : channelIds) {
			String cs = new StringBuilder(channelId).append("_summary").toString();
			String cd = new StringBuilder(channelId).append("_details").toString();
			
			if (Main.cache.contains(cs)) {
				Main.cache.remove(cs);
				logger.info(new StringBuilder(cs).append(rm));
			}
			
			if (Main.cache.contains(cd)) {
				Main.cache.remove(cd);
				logger.info(new StringBuilder(cd).append(rm));
			}
		}
	}
	
	/**
	* This method is used to update conference object in cache with newly calculated scores.
	* @param confId: the ID of conference that is updated
	* @param confScore: the scores of conference that is newly calculated
	*/
	private void updateConfScoreInCache(String confId, ConferenceScore confScore) {
		String ud = " in cache score udpated.";
		String cs = new StringBuilder(confId).append("_summary").toString();
		String cd = new StringBuilder(confId).append("_details").toString();
		
		// update conference summary cache with new score
		if (Main.cache.contains(cs)) {
			((LocalDbConference) ((CacheItem<?>) Main.cache.get(cs)).getCacheObject()).setScore(confScore);
			logger.info(new StringBuilder(cs).append(ud));
		}
		
		// update conference details cache with new score
		if (Main.cache.contains(cd)) {
			((LocalDbConference) ((CacheItem<?>) Main.cache.get(cd)).getCacheObject()).setScore(confScore);
			logger.info(new StringBuilder(cd).append(ud));
		}
	}
	
	/**
	* This method is used to update channel object in cache with newly calculated scores.
	* @param chanId: the ID of channel that is updated
	* @param chanScore: the scores of channel that is newly calculated
	*/
	private void updateChanScoreInCache(String chanId, ChannelScore chanScore) {
		String ud = " in cache score udpated.";
		String cs = new StringBuilder(chanId).append("_summary").toString();
		String cd = new StringBuilder(chanId).append("_details").toString();
		
		// update channel summary cache with new score
		if (Main.cache.contains(cs)) {
			((LocalDbChannel) ((CacheItem<?>) Main.cache.get(cs)).getCacheObject()).setScore(chanScore);
			logger.info(new StringBuilder(cs).append(ud));
		}
		
		// update channel details cache with new score
		if (Main.cache.contains(cd)) {
			((LocalDbChannel) ((CacheItem<?>) Main.cache.get(cd)).getCacheObject()).setScore(chanScore);
			logger.info(new StringBuilder(cd).append(ud));
		}
	}

}
