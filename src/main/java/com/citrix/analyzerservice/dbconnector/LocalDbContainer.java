package com.citrix.analyzerservice.dbconnector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;
import com.citrix.analyzerservice.model.ConferenceStats;
import com.citrix.analyzerservice.model.MixerOut;
import com.citrix.analyzerservice.model.MixerSum;
import com.citrix.analyzerservice.model.StreamEnhancer;
import com.citrix.analyzerservice.model.StreamProcessor;

public class LocalDbContainer implements IDbConnector {

	private final Logger logger = Logger.getLogger(LocalDbContainer.class);
	private LocalDateTime defaultTime = LocalDateTime.MIN;
	
	private String defaultPath = Main.configs.get("File_Directory");	
	private String maxStatsReadLine = Main.configs.get("MAX_Stats_Read_Line");
			
	public LocalDbContainer() {}
	
	@Override
	public Map<String, List<String>> findUpdatedConfIds() {
		Map<String, List<String>> updatedConfIds = new HashMap<String, List<String>>();
		List<String> newConfIds = new ArrayList<String>();
		List<String> oldConfIds = new ArrayList<String>();
		
		List<String> allConfIds = getAllConfIds(defaultPath);
		if (allConfIds == null || allConfIds.isEmpty()) {
			logger.warn("No conference found in system.");
		}
		
		List<String> proConfIds = getProConfIds(new StringBuilder(defaultPath).append("ConfList.txt").toString());
		if (proConfIds == null || proConfIds.isEmpty()) {
			logger.warn("No conference found in list.");
		}
		
		// get newly added conference IDs
		for (String s : allConfIds)
			if (!proConfIds.contains(s))
				newConfIds.add(s);
		if (!newConfIds.isEmpty())
			updatedConfIds.put("newConfIds", newConfIds);
		
		// get newly deleted conference IDs
		for (String s : proConfIds)
			if (!allConfIds.contains(s))
				oldConfIds.add(s);
		if (!oldConfIds.isEmpty())
			updatedConfIds.put("oldConfIds", oldConfIds);
		
		return updatedConfIds;
	}
	
	@Override
	public List<LocalDbConference> findConferenceList() {
		List<String> confIds = getAllConfIds(defaultPath);
		if (confIds == null || confIds.isEmpty()) {
			logger.warn("No conference found.");
			return new ArrayList<LocalDbConference>();
		}
		
		List<LocalDbConference> conferences = new ArrayList<LocalDbConference>();
				
		for (int i=0; i<confIds.size(); i++) {
			LocalDbConference conference = findConference(confIds.get(i), false);
			conferences.add(conference);
		}
		
		return conferences;
	}
	
	@Override
	public LocalDbConference findConference(String confId, boolean showAll) {
		
		if (confId == null || confId.length() <= 0) {
			logger.error("Please provide conference uuid.");
			return null;
		}
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.warn(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		LocalDbConference conference = null;
		
		String folderPath = new StringBuilder(defaultPath).append(folder.get(0)).toString();
		
		// Get timestamp
		LocalDateTime timestamp = findConferenceTimestamp(folder.get(0));
		
		// Get startTime and endTime
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
				
		// Get conference statistics
		ConferenceStats stats = null;
		
		if (showAll)
			stats = getConferenceStats(folderPath);
		
		// Get conference score
		ConferenceScore score = getConferenceScore(confId);
		if (score == null) {
			logger.warn(new StringBuilder("Conference ").append(confId).append(" score is not ready."));
			score = new ConferenceScore(-1, -1);
		}
				
		// Get conference channels
		List<LocalDbChannel> channels = findConfChannels(confId);
		
		conference = new LocalDbConference(confId, timestamp, startTime, endTime, channels.size(), stats, score, channels);
		
		return conference;
	}
	
	@Override
	public List<LocalDbChannel> findConfChannels(String confId) {
		if (confId == null || confId.length() <= 0) {
			logger.error("Please provide a valid conference uuid.");
			return null;
		}
			
		List<LocalDbChannel> channels = new ArrayList<LocalDbChannel>();
		List<String> channelIds = findConfChannelIds(confId);
		
		if (channelIds != null && !channelIds.isEmpty())
			for (String channelId : channelIds) {
				channels.add(findChannel(confId, channelId, false));
			}
		
		return channels;
	}
	
	@Override
	public LocalDateTime findConferenceTimestamp(String folder) {
		
		// if confId instead of folderPath is passed in
		if (folder.length() < 50) {
			logger.trace("Received conference uuid to find conference timestamp, getting it's path...");
			String confId = folder;
			List<String> folders = getFileNameFromId(defaultPath, confId, "folder");
			if (folder.isEmpty()) {
				logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
				return null;
			}
			
			folder = folders.get(0);
		}
		
		String dateTimeStr = new StringBuilder(Integer.toString(20)).append(folder.substring(47)).toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
		
		return dateTime;
	}
	
	@Override
	public List<String> findConfChannelIds(String confId) {
		List<String> channels = new ArrayList<String>();
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		File path = new File(new StringBuilder(defaultPath).append(folder.get(0)).toString());
		File[] fileLists = path.listFiles();
		
		if (fileLists.length <= 0) {
			logger.error(new StringBuilder("Cannot find channels in conference through ").append(path).append('.'));
			return null;
		}
		
		for (int i=0; i<fileLists.length; i++)
			if (fileLists[i].isFile() && fileLists[i].getName().startsWith("MixerInChannel"))
				channels.add(fileLists[i].getName().substring(37, 72));
		
		return channels;
	}
	
	@Override
	public LocalDbChannel findChannel(String confId, String chanId, boolean showAll) {
		
		if (chanId == null || chanId.length() <= 0) {
			logger.error("Please provide channel uuid.");
			return null;
		}
		
		if (confId == null || confId.length() <= 0) {
			logger.debug("Conference uuid not provided. Get it from channel uuid...");
			confId = getChannelConference(chanId);
			if (confId == null || confId.length() <= 0) {
				logger.error("Channel does not exists.");
				return null;
			}
				
		}
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		String folderPath = new StringBuilder(defaultPath).append(folder.get(0)).toString();
		
		// Get startTime and endTime
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
		
		// Get channel statistics
		ChannelStats stats = null;
		
		if (showAll) {
			logger.debug(new StringBuilder("Fetching channel ").append(chanId).append(" statistics..."));
			List<String> files = getFileNameFromId(folderPath, chanId, "file");
			if (files != null && !files.isEmpty())
				stats = getChannelStats(folderPath, files);
			else
				logger.error(new StringBuilder("Cannot find channel ").append(chanId).append(" statistics."));
		}
		
		// Get channel score
		ChannelScore score = getChannelScore(chanId);
		if (score == null) {
			logger.warn(new StringBuilder("Channel ").append(chanId).append(" score is not ready."));
			score = new ChannelScore(-1, -1, -1);
		}
		
		LocalDbChannel channel = new LocalDbChannel(chanId, startTime, endTime, stats, score);	
		
		return channel;
	}
	
	@Override
	public ChannelStats findChannelStats(String confId, String chanId) {
		
		if (confId == null || confId.length() <= 0 || chanId == null || chanId.length() <= 0) {
			logger.error("Please provide conference and channel uuid.");
			return null;
		}
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		String folderPath = new StringBuilder(defaultPath).append(folder.get(0)).toString();		
		List<String> files = getFileNameFromId(folderPath, chanId, "file");
		if (files == null || files.isEmpty()) {
			logger.error(new StringBuilder("Cannot find channel ").append(chanId).append('.'));
			return null;
		}
		
		ChannelStats stats = getChannelStats(folderPath, files);
		
		return stats;
	}
	
	@Override
	public LocalDateTime findChannelTimestamp(String confId, String chanId) {
		
		if (confId == null || confId.length() <= 0 || chanId == null || chanId.length() <= 0) {
			logger.error("Please provide conference and channel uuid.");
			return null;
		}
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		String folderPath = new StringBuilder(defaultPath).append(folder.get(0)).toString();		
		List<String> files = getFileNameFromId(folderPath, chanId, "file");
		if (files == null || files.isEmpty()) {
			logger.error(new StringBuilder("Cannot find channel ").append(chanId).append('.'));
			return null;
		}
		
		int index = files.get(0).length()-17;
		String dateTimeStr = new StringBuilder(Integer.toString(20)).append(files.get(0).substring(index, index+13)).toString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
		
		return dateTime;
	}
	
	private ChannelStats getChannelStats(String folderPath, List<String> files) {
		
		if (files.isEmpty()) {
			logger.error("Conference folder is empty.");
			return null;
		}
		
		if (maxStatsReadLine == null || maxStatsReadLine.isEmpty())
			maxStatsReadLine = "all";
		
		ChannelStats stats = null;
		StreamProcessor sp = null;
		StreamEnhancer se = null;
		MixerOut mo = null;
		Map<String, double[]> strProData = new HashMap<String, double[]>();
		Map<String, double[]> strEnhData = new HashMap<String, double[]>();
		Map<String, double[]> mixOutData = new HashMap<String, double[]>();
		
		for (int i=0; i<files.size(); i++) {
			String fileName = files.get(i);
			if (fileName.contains("StreamProcessor"))
				strProData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
			if (fileName.contains("StreamEnhancer"))
				strEnhData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
			if (fileName.contains("MixerOutChannel"))
				mixOutData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
		}
		
		if (strProData != null && !strProData.isEmpty()) {
			sp = new StreamProcessor(strProData.get("SeqNr"), strProData.get("muted"), strProData.get("RTP_streamBegin"), 
					strProData.get("RTP_isDelayed"), strProData.get("RTP_isReordered"), strProData.get("TimeStamp"), 
					strProData.get("Media_BufSize"), strProData.get("IATJitter"), strProData.get("NS_speechPowerIn"), 
					strProData.get("NS_speechPowerOut"), strProData.get("NS_noisePowerIn"), strProData.get("NS_noisePowerOut"), 
					strProData.get("AGC_speechLevelOut"), strProData.get("AGC_noiseLevelOut"), strProData.get("AGC_vadState"));
		} else {
			logger.warn("Cannot find StreamProcessor data.");
			sp = new StreamProcessor(new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999});
		}
		
		if (strEnhData != null && !strEnhData.isEmpty()) {
			se = new StreamEnhancer(strEnhData.get("QNr"), strEnhData.get("QuantumsInJitterBuffer[Q]"), strEnhData.get("PacketScaleFast"), 
					strEnhData.get("PacketScaleSlow"), strEnhData.get("PacketScaleFactor"), strEnhData.get("QuantumUnderrunCounter [Q]"), 
					strEnhData.get("QuantumType"), strEnhData.get("PopTimeDelta"));
		} else {
			logger.warn("Cannot find StreamEnhancer data.");
			se = new StreamEnhancer(new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999});
		}
		
		if (mixOutData != null && !mixOutData.isEmpty()) {
			mo = new MixerOut(mixOutData.get("seqNo"), mixOutData.get("timestamp"));
		} else {
			logger.warn("Cannot find MixerOut data.");
			mo = new MixerOut(new double[] {-999999}, new double[] {-999999});
		}
		
		stats = new ChannelStats(sp, se, mo);
		
		return stats;
	}

	@Override
	public List<List<String>> readFile(String path, String delimiter, String size) {
		BufferedReader br = null;
		String line = "";
		List<List<String>> data = new ArrayList<List<String>>();
		int count = 0, lineNo = 0;
		
		if (!size.equalsIgnoreCase("all"))
			lineNo = Integer.parseInt(size);
		
		try {
			br = new BufferedReader(new FileReader(path));
			
			while ((line = br.readLine()) != null) {
				ArrayList<String> dataLine = new ArrayList<String>();
				String[] splitData = line.split(new StringBuilder("\\s*").append(delimiter).append("\\s*").toString());
				for (int i=0; i<splitData.length; i++)
					if (!(splitData[i] == null) || !(splitData[i].length() == 0))
						dataLine.add(splitData[i].trim());
				data.add(dataLine);
				
				if (lineNo != 0) {
					count++;
					if (count > lineNo)
						break;
				}				
			}
			
		} catch (FileNotFoundException e) {
			logger.error(new StringBuilder("Cannot find file in ").append(path).append('.'));
		} catch (IOException e) {
			logger.error(new StringBuilder("Error in reading file in ").append(path).append('.'));
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					logger.error(new StringBuilder("Cannot close file in ").append(path).append('.'));
				}
		}
		
		return data;
		
	}
	
	@Override
	public boolean writeFile(String type, String[] content) {
		
		String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
		logger.debug(new StringBuilder("Writing ").append(fileName).append("..."));
		
		try {			
			File file = new File(new StringBuilder(defaultPath).append(fileName).toString());
			boolean newlyCreated = false;
			
			if (!file.exists()) {
				logger.warn(new StringBuilder(fileName).append(" not exists. Creating a new one..."));
				file.createNewFile();
				newlyCreated = true;
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
			
			if (newlyCreated) {
				if (type.equalsIgnoreCase("conference")) {
					bw.write("[uuid], [timestamp], [channelNo], [avgPLIndicator], [avgLevelIndicator]");
					bw.newLine();
				} else if (type.equalsIgnoreCase("channel")) {
					bw.write("[confId], [uuid], [avgPLIndicator], [avgLevelIndicator], [avgPacketLoss]");
					bw.newLine();
				} else {
					bw.close();
					return false;
				}
			}
			
			for (int i=0; i<content.length; i++) {
				bw.write(content[i]);
				bw.newLine();
			}
			bw.close();
			
			return true;
			
		} catch (IOException e) {
			logger.error(new StringBuilder("Error in writing file ").append(fileName).append('.'));
		}
		
		return false;
	}
	
	@Override
	public boolean updateFile(String type, String confId) {
		
		String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
		logger.debug(new StringBuilder("Updating ").append(fileName).append("..."));
		
		try {			
			File origFile = new File(new StringBuilder(defaultPath).append(fileName).toString());
			
			if (!origFile.exists()) {
				logger.error(new StringBuilder(fileName).append(" not exists."));
				return false;
			}
			
			File tempFile = new File(origFile.getAbsolutePath().replace(fileName, new StringBuilder("Temp").append(fileName)));
			BufferedReader br = new BufferedReader(new FileReader(origFile.getAbsolutePath()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
			String line = null;
			
			while ((line = br.readLine()) != null)
				if (!line.trim().startsWith(confId)) {
					bw.write(line);
					bw.newLine();
				}
			bw.close();
			br.close();
			
			if (!origFile.delete())
				logger.error(new StringBuilder("Cannot delete original ").append(fileName).append('.'));
			
			if (!tempFile.renameTo(origFile))
				logger.error(new StringBuilder("Cannot rename new file to ").append(fileName).append('.'));
			
			return true;
			
		} catch (IOException e) {
			logger.error("Error in file I/O.");
		}
		
		
		return false;
	}
	
	private List<String> getAllConfIds(String path) {
		List<String> confs = getFolderNames(path);
		List<String> confIds = new ArrayList<String>();
		
		for (int i=0; i<confs.size(); i++) {
			String confName = confs.get(i);
			int indexOfConfId = confName.indexOf('_')+1;
			confIds.add(confName.substring(indexOfConfId, indexOfConfId+35));
		}
		
		return confIds;
	}
	
	private List<String> getProConfIds(String path) {
		List<String> confIds = new ArrayList<String>();
		
		BufferedReader br = null;
		String line = "";
		
		try {
			br = new BufferedReader(new FileReader(path));
			line = br.readLine();
			
			while ((line = br.readLine()) != null) {
				int index = line.indexOf(',');
				confIds.add(line.substring(0, index));
			}
			
		} catch (FileNotFoundException e) {
			logger.warn("Cannot find ConfList.txt.");
		} catch (IOException e) {
			logger.error("Error in reading ConfList file.");
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Cannot close ConfList file.");
				}
		}
		
		return confIds;
	}
	
	private List<String> getFolderNames(String path) {
		List<String> folders = new ArrayList<String>();
		File folder = new File(path);
		File[] fileLists = folder.listFiles();
		
		if (fileLists.length <= 0) {
			logger.warn(new StringBuilder("Cannot find folder in ").append(path).append('.'));
			return new ArrayList<String>();
		}
		
		for (int i=0; i<fileLists.length; i++)
			if (fileLists[i].isDirectory())
				folders.add(fileLists[i].getName());
		
		return folders;
	}
	
	private List<String> getFileNameFromId(String path, String uuid, String type) {
		List<String> files = new ArrayList<String>();
		File folder = new File(path);
		File[] fileLists = folder.listFiles();
		
		if (fileLists.length <= 0)
			return new ArrayList<String>();
		
		for (int i=0; i<fileLists.length; i++) {
			if (type.equalsIgnoreCase("folder"))
				if (fileLists[i].isDirectory() && fileLists[i].getName().contains(uuid))
					files.add(fileLists[i].getName());
			if (type.equalsIgnoreCase("file"))
				if (fileLists[i].isFile() && fileLists[i].getName().contains(uuid) && fileLists[i].getName().endsWith(".txt"))
					files.add(fileLists[i].getName());
			if (type.equalsIgnoreCase("mixersum"))
				if (fileLists[i].isFile() && fileLists[i].getName().contains("MixerSumStream") && fileLists[i].getName().endsWith(".txt"))
					files.add(fileLists[i].getName()); 
		}
		
		return files;
	}
	
	private Map<String, double[]> convertDataStructure(List<List<String>> data) {
		Map<String, double[]> convertedData = new HashMap<String, double[]>();
		int dataRowNo = data.size();
		int dataColNo = data.get(0).size();
		int i = 0;
		
		double[][] dataMatrix = new double[dataColNo][dataRowNo-1];
		
		for (i=1; i<dataRowNo; i++) {
			for (int j=0; j<dataColNo; j++) {
				dataMatrix[j][i-1] = Double.parseDouble(data.get(i).get(j));
			}
		}
		
		for (i=0; i<data.get(0).size(); i++) {
			convertedData.put(data.get(0).get(i).substring(1, data.get(0).get(i).length()-1), dataMatrix[i]);			
		}
		
		return convertedData;
	}
	
	private ConferenceStats getConferenceStats(String folderPath) {
		
		if (folderPath == null || folderPath.length() <= 0)
			return null;
		
		if (maxStatsReadLine == null || maxStatsReadLine.isEmpty())
			maxStatsReadLine = "all";
		
		ConferenceStats stats = null;
		MixerSum ms = null;
		Map<String, double[]> mixSumData = null;

		mixSumData = convertDataStructure(readFile(new StringBuilder(folderPath).append('/')
				.append(getFileNameFromId(folderPath, "", "mixersum").get(0)).toString(), ",", maxStatsReadLine));
		
		if (mixSumData != null && !mixSumData.isEmpty())
			ms = new MixerSum(mixSumData.get("quantum"), mixSumData.get("nConferenceId"), mixSumData.get("nSpeakers"));
		else
			logger.warn("Cannot find Mixer data.");
		if (ms != null)
			stats = new ConferenceStats(ms);
		
		return stats;
	}
	
	private ConferenceScore getConferenceScore(String confId) {
		ConferenceScore score = null;
		List<List<String>> confs = readFile(new StringBuilder(defaultPath).append("ConfList.txt").toString(), ",", "all");
		for (List<String> conf : confs)
			if (conf.get(0).equals(confId))
				score = new ConferenceScore(Integer.parseInt(conf.get(3)), Integer.parseInt(conf.get(4)));
		
		return score;
	}
	
	private ChannelScore getChannelScore(String chanId) {
		ChannelScore score = null;
		List<List<String>> chans = readFile(new StringBuilder(defaultPath).append("ChanList.txt").toString(), ",", "all");
		for (List<String> chan : chans)
			if (chan.get(1).equals(chanId))
				score = new ChannelScore(Integer.parseInt(chan.get(2)), Integer.parseInt(chan.get(3)), Double.parseDouble(chan.get(4)));
		
		return score;
	}
	
	private String getChannelConference(String chanId) {
		List<String> allConfIds = getAllConfIds(defaultPath);
		
		for (int i=0; i<allConfIds.size(); i++) {
			String confId = allConfIds.get(i);
			List<String> channelIds = findConfChannelIds(confId);
			for (String channelId : channelIds)
				if (channelId.equals(chanId))
					return confId;
		}	
		
		return null;
	}
}
