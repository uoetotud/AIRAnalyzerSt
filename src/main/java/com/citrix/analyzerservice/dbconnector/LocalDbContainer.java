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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;
import com.citrix.analyzerservice.model.ConferenceStats;
import com.citrix.analyzerservice.model.Mixer;
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
		
		List<String> allConfIds = findAllConfIds(defaultPath);
		if (allConfIds == null || allConfIds.isEmpty()) {
			logger.warn("No conference found in system.");
		}
		
		List<String> proConfIds = findProConfIds(defaultPath + "/ConfList.txt");
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
		List<String> confIds = findAllConfIds(defaultPath);
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
			logger.warn("Cannot find conference " + confId + ".");
			return null;
		}
		
		LocalDbConference conference = null;
		
		String folderPath = defaultPath + folder.get(0);
		
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
			logger.warn("Conference " + confId + " score is not ready.");
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
		List<String> channelIds = getConfChannelIds(confId);
		
		if (channelIds != null && !channelIds.isEmpty())
			for (String channelId : channelIds) {
				channels.add(findChannel(confId, channelId, false));
			}
		
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
			logger.error("Cannot find conference " + confId + ".");
			return null;
		}
		
		String folderPath = defaultPath + folder.get(0);
		
		// Get startTime and endTime
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
		
		// Get channel statistics
		ChannelStats stats = null;
		
		if (showAll) {
			logger.debug("Fetching channel " + chanId + " statistics...");
			List<String> files = getFileNameFromId(folderPath, chanId, "file");
			if (files != null && !files.isEmpty())
				stats = findChannelStats(folderPath, files);
			else
				logger.error("Cannot find channel " + chanId + " statistics.");
		}
		
		// Get channel score
		ChannelScore score = getChannelScore(chanId);
		if (score == null) {
			logger.warn("Channel " + chanId + " score is not ready.");
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
			logger.error("Cannot find conference " + confId + ".");
			return null;
		}
		
		String folderPath = defaultPath + folder.get(0);		
		List<String> files = getFileNameFromId(folderPath, chanId, "file");
		if (files == null || files.isEmpty()) {
			logger.error("Cannot find channel " + chanId + ".");
			return null;
		}
		
		ChannelStats stats = findChannelStats(folderPath, files);
		
		return stats;
	}
	
	private ChannelStats findChannelStats(String folderPath, List<String> files) {
		
		if (files.isEmpty()) {
			logger.error("Conference folder is empty.");
			return null;
		}
		
		if (maxStatsReadLine == null || maxStatsReadLine.isEmpty())
			maxStatsReadLine = "all";
		
		ChannelStats stats = null;
		StreamProcessor sp = null;
		Map<String, double[]> strProData = new HashMap<String, double[]>();
//		Map<String, double[]> strEnhData = null;
		
		for (int i=0; i<files.size(); i++) {
			strProData = convertDataStructure(readFile(folderPath+"/"+files.get(i), ",", maxStatsReadLine));
//			strEnhData = convertDataStructure(readFile(path, ","));
		}
		
		if (strProData != null && !strProData.isEmpty())
			sp = new StreamProcessor(strProData.get("SeqNr"), strProData.get("NS_speechPowerOut"), strProData.get("NS_noisePowerOut"));
		else
			logger.warn("Cannot find StreamProcessor data.");
		StreamEnhancer se = null;
		if (sp != null)
			stats = new ChannelStats(sp, se);
		
		return stats;
	}

	private List<List<String>> readFile(String path, String delimiter, String size) {
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
				String[] splitData = line.split("\\s*"+delimiter+"\\s*");
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
			logger.error("Cannot find file in " + path + ".");
		} catch (IOException e) {
			logger.error("Error in reading file in " + path + ".");
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					logger.error("Cannot close file in " + path + ".");
				}
		}
		
		return data;
		
	}
	
	@Override
	public boolean writeFile(String type, String[] content) {
		
		String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
		logger.debug("Writing " + fileName + "...");
		
		try {			
			File file = new File(defaultPath + fileName);
			boolean newlyCreated = false;
			
			if (!file.exists()) {
				logger.warn(fileName + " not exists. Creating a new one...");
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
			logger.error("Error in writing file " + fileName + ".");
		}
		
		return false;
	}
	
	@Override
	public boolean updateFile(String type, String confId) {
		
		String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
		logger.debug("Updating " + fileName + "...");
		
		try {			
			File origFile = new File(defaultPath + fileName);
			
			if (!origFile.exists()) {
				logger.error(fileName + " not exists.");
				return false;
			}
			
			File tempFile = new File(origFile.getAbsolutePath().replace(fileName, "Temp"+fileName));
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
				logger.error("Cannot delete original " + fileName + ".");
			
			if (!tempFile.renameTo(origFile))
				logger.error("Cannot rename new file to " + fileName + ".");
			
			return true;
			
		} catch (IOException e) {
			logger.error("Error in file I/O.");
		}
		
		
		return false;
	}
	
	private List<String> findAllConfIds(String path) {
		List<String> confs = getFolderNames(path);
		List<String> confIds = new ArrayList<String>();
		
		for (int i=0; i<confs.size(); i++) {
			String confName = confs.get(i);
			confIds.add(confName.substring(confName.indexOf('_')+1));
		}
		
		return confIds;
	}
	
	private List<String> findProConfIds(String path) {
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
			logger.warn("Cannot find folder in " + path + ".");
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
			if (type.equalsIgnoreCase("mixer"))
				if (fileLists[i].isFile() && fileLists[i].getName().contains("MixerSumStream") && fileLists[i].getName().endsWith(".txt"))
					files.add(fileLists[i].getName()); 
		}
		
//		if (files.isEmpty())
//			logger.warn("Cannot find file from uuid " + uuid + " in " + path + ".");
		
		return files;
	}
	
	private Map<String, double[]> convertDataStructure(List<List<String>> data) {
		Map<String, double[]> convertedData = new LinkedHashMap<String, double[]>();
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
	
	@Override
	public List<String> getConfChannelIds(String confId) {
		List<String> channels = new ArrayList<String>();
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error("Cannot find conference " + confId + ".");
			return null;
		}
		
		File path = new File(defaultPath + folder.get(0));
		File[] fileLists = path.listFiles();
		
		if (fileLists.length <= 0) {
			logger.error("Cannot find channels in conference through " + path + ".");
			return null;
		}
		
		for (int i=0; i<fileLists.length; i++)
			if (fileLists[i].isFile() && fileLists[i].getName().startsWith("MixerInChannel"))
				channels.add(fileLists[i].getName().substring(37, 72));
		
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
				logger.error("Cannot find conference " + confId + ".");
				return null;
			}
			
			folder = folders.get(0);
		}
		
		String dateTimeStr = "20" + folder.substring(0, 13);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
		LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
		
		return dateTime;
	}
	
	private ConferenceStats getConferenceStats(String folderPath) {
		
		if (folderPath == null || folderPath.length() <= 0)
			return null;
		
		if (maxStatsReadLine == null || maxStatsReadLine.isEmpty())
			maxStatsReadLine = "all";
		
		ConferenceStats stats = null;
		Mixer m = null;
		Map<String, double[]> mixData = null;

		mixData = convertDataStructure(readFile(folderPath + "/" + getFileNameFromId(folderPath, "", "mixer").get(0), ",", maxStatsReadLine));
		
		if (mixData != null && !mixData.isEmpty())
			m = new Mixer(mixData.get("quantum"), mixData.get("nConferenceId"), mixData.get("nSpeakers"));
		else
			logger.warn("Cannot find Mixer data.");
		if (m != null)
			stats = new ConferenceStats(m);
		
		return stats;
	}
	
	private ConferenceScore getConferenceScore(String confId) {
		ConferenceScore score = null;
		List<List<String>> confs = readFile(defaultPath+"/ConfList.txt", ",", "all");
		for (List<String> conf : confs)
			if (conf.get(0).equals(confId))
				score = new ConferenceScore(Integer.parseInt(conf.get(3)), Integer.parseInt(conf.get(4)));
		
		return score;
	}
	
	private ChannelScore getChannelScore(String chanId) {
		ChannelScore score = null;
		List<List<String>> chans = readFile(defaultPath+"/ChanList.txt", ",", "all");
		for (List<String> chan : chans)
			if (chan.get(1).equals(chanId))
				score = new ChannelScore(Integer.parseInt(chan.get(2)), Integer.parseInt(chan.get(3)), Double.parseDouble(chan.get(4)));
		
		return score;
	}
	
	private String getChannelConference(String chanId) {
		List<String> allConfIds = findAllConfIds(defaultPath);
		
		for (int i=0; i<allConfIds.size(); i++) {
			String confId = allConfIds.get(i);
			List<String> channelIds = getConfChannelIds(confId);
			for (String channelId : channelIds)
				if (channelId.equals(chanId))
					return confId;
		}	
		
		return null;
	}
}
