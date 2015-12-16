package com.citrix.analyzerservice.dtprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;

public class DtProcessor extends TimerTask implements IDtProcessor {
	
	// Constants definitions
	final int kMaxStatsValues = 6000;
	final double cVADLevelDiff = 20;
	final int cQuantumSizeMs = 10;
	final int cTime = 1; // time in sec
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	List<String> newConfIds = null;
	
	public DtProcessor() {}
	
	public void run() {
		System.out.println("\nStart DataProcessor ...");
		
		if (checkUpdate()) {
			System.out.println("New data found. Start processing ...");

			List<ConferenceScore> confScores = new ArrayList<ConferenceScore>();
			ConferenceScore confScore = new ConferenceScore(0, 0);
			
			for (String confId : newConfIds) {
				
				List<LocalDbChannel> channels = ldc.findConfChannels(confId);
				List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
				
				for (LocalDbChannel channel : channels) {					
					ChannelScore chanScore = calChannelScore(confId, channel.getUuid());
					chanScores.add(chanScore);
				}
				
//				System.out.println(chanScores.size());
				updateChanList(confId, chanScores);
				
				confScore = calConferenceScore(confId, chanScores);
				confScores.add(confScore);
			}		
			
			updateConfList(newConfIds, confScores);
			
			System.out.println("Finish processing ...");
		}
		else
			System.out.println("No data update.");
		
		System.out.println("Stop DataProcessor ...");
		
	}
	
	@Override
	public boolean checkUpdate() {
		
		newConfIds = ldc.findNewConfIds();
		
		if (newConfIds == null || newConfIds.isEmpty())
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
		
		if (channels.size() != chanScores.size())
			return null;
		
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
		
//		double[] seqNr = Arrays.copyOf(stats.getStrProcessor().getSeqNr(), kMaxStatsValues);
//		double[] speechValues = Arrays.copyOf(stats.getStrProcessor().getNS_speechPowerOut(), kMaxStatsValues);
//		double[] noiseValues = Arrays.copyOf(stats.getStrProcessor().getNS_noisePowerOut(), kMaxStatsValues);
		
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
		double avgSpeechLevel = 0;
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
}
