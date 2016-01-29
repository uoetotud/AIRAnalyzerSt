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

/**
 * @author Xi Luo
 *
 */
public class LocalDbContainer implements IDbConnector {

	private final Logger logger = Logger.getLogger(LocalDbContainer.class);
	private LocalDateTime defaultTime = LocalDateTime.MIN;
	
	/* get configuration */
	private String defaultPath = Main.configs.get("File_Directory");	
	private String maxStatsReadLine = Main.configs.get("MAX_Stats_Read_Line");
			
	public LocalDbContainer() {}
	
	/**
	* This method is used to get the ID of all updated (newly added or deleted) conferences from disk.
	* @return Map: updated conference IDs in hashmap
	*/
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
		
		/* get newly added conference IDs */
		for (String s : allConfIds)
			if (!proConfIds.contains(s))
				newConfIds.add(s);
		if (!newConfIds.isEmpty())
			updatedConfIds.put("newConfIds", newConfIds);
		
		/* get lately deleted conference IDs */
		for (String s : proConfIds)
			if (!allConfIds.contains(s))
				oldConfIds.add(s);
		if (!oldConfIds.isEmpty())
			updatedConfIds.put("oldConfIds", oldConfIds);
		
		return updatedConfIds;
	}
	
	/**
	* This method is used to get the list of all available conferences (without statistics) from disk.
	* @return List: all conferences in arraylist
	*/
	@Override
	public List<LocalDbConference> findConferenceList() {
		List<String> confIds = getAllConfIds(defaultPath);
		if (confIds == null || confIds.isEmpty()) {
			logger.warn("No conference found.");
			return new ArrayList<LocalDbConference>();
		}
		
		List<LocalDbConference> conferences = new ArrayList<LocalDbConference>();
		
		/* get conferences */
		for (int i=0; i<confIds.size(); i++) {
			LocalDbConference conference = findConference(confIds.get(i), false);
			conferences.add(conference);
		}
		
		return conferences;
	}
	
	/**
	* This method is used to get a specific conference from disk.
	* @param confId: the ID of conference
	* @param showAll: whether to show the statistics of conference (true for show, false for NOT show)
	* @return LocalDbConference: the conference
	*/
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
		
		/* get timestamp */
		LocalDateTime timestamp = findConferenceTimestamp(folder.get(0));
		
		/* get startTime and endTime */
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
				
		/* get statistics */
		ConferenceStats stats = null;
		
		if (showAll)
			stats = getConferenceStats(folderPath);
		
		/* get score */
		ConferenceScore score = getConferenceScore(confId);
		if (score == null) {
			logger.warn(new StringBuilder("Conference ").append(confId).append(" score is not ready."));
			score = new ConferenceScore(-1, -1);
		}
				
		/* get channels */
		List<LocalDbChannel> channels = findConfChannels(confId);
		
		conference = new LocalDbConference(confId, timestamp, startTime, endTime, channels.size(), stats, score, channels);
		
		return conference;
	}
	
	/**
	* This method is used to get the list of channels for a specific conference from disk.
	* @param confId: the ID of conference
	* @return List: the channels (without statistics) of conference
	*/
	@Override
	public List<LocalDbChannel> findConfChannels(String confId) {
		if (confId == null || confId.length() <= 0) {
			logger.error("Please provide a valid conference uuid.");
			return null;
		}
			
		List<LocalDbChannel> channels = new ArrayList<LocalDbChannel>();
		List<String> channelIds = findConfChannelIds(confId);
		
		/* get channels */
		if (channelIds != null && !channelIds.isEmpty())
			for (String channelId : channelIds) {
				channels.add(findChannel(confId, channelId, false));
			}
		
		return channels;
	}
	
	/**
	* This method is used to get the timestamp for a specific conference from disk.
	* @param folder: the path OR the ID of conference
	* @return LocalDateTime: the timestamp of conference
	*/
	@Override
	public LocalDateTime findConferenceTimestamp(String folder) {
		
		/* convert to folder path IF conference uuid is passed in */
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
	
	/**
	* This method is used to get the list of channels IDs for a specific conference from disk.
	* @param confId: the ID of conference
	* @return List: all channel IDs of conference in arraylist
	*/
	@Override
	public List<String> findConfChannelIds(String confId) {
		List<String> channels = new ArrayList<String>();
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty()) {
			logger.error(new StringBuilder("Cannot find conference ").append(confId).append('.'));
			return null;
		}
		
		/* get folder's path and all files in it */
		File path = new File(new StringBuilder(defaultPath).append(folder.get(0)).toString());
		File[] fileList = path.listFiles();
		
		if (fileList.length <= 0) {
			logger.error(new StringBuilder("Cannot find channels in conference through ").append(path).append('.'));
			return null;
		}
		
		/* get channel IDs */
		for (int i=0; i<fileList.length; i++)
			if (fileList[i].isFile() && fileList[i].getName().startsWith("MixerInChannel"))
				channels.add(fileList[i].getName().substring(37, 72));
		
		return channels;
	}
	
	/**
	* This method is used to get a specific channel from disk.
	* @param confId: the ID of conference
	* @param chanId: the ID of channel
	* @param showAll: whether to show the statistics of channel (true for show, false for NOT show)
	* @return LocalDbChannel: the channel
	*/
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
		
		/* get startTime and endTime */
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
		
		/* get statistics */
		ChannelStats stats = null;
		
		if (showAll) {
			logger.debug(new StringBuilder("Fetching channel ").append(chanId).append(" statistics..."));
			List<String> files = getFileNameFromId(folderPath, chanId, "file");
			if (files != null && !files.isEmpty())
				stats = getChannelStats(folderPath, files);
			else
				logger.error(new StringBuilder("Cannot find channel ").append(chanId).append(" statistics."));
		}
		
		/* get score */
		ChannelScore score = getChannelScore(chanId);
		if (score == null) {
			logger.warn(new StringBuilder("Channel ").append(chanId).append(" score is not ready."));
			score = new ChannelScore(-1, -1, -1);
		}
		
		LocalDbChannel channel = new LocalDbChannel(chanId, startTime, endTime, stats, score);	
		
		return channel;
	}
	
	/**
	* This method is used to get the statistics for a specific channel from disk.
	* @param confId: the ID of conference
	* @param chanId: the ID of channel
	* @return ChannelStats: the statistics of channel
	*/
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
	
	/**
	* This method is used to get the timestamp for a specific channel from disk.
	* @param confId: the ID of conference
	* @param chanId: the ID of channel
	* @return LocalDateTime: the timestamp of channel
	*/
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
	
	/**
	* This method is used to read a file (either a meta file or conference/channel list file) from disk.
	* @param path: the path of the file
	* @param delimiter: the delimiter in file to separate columns
	* @param size: the maximal line numbers to read
	* @return List: the data of string in arraylist
	*/
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
			
			/* read file line by line */
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
	
	/**
	* This method is used to write the conference/channel list file in disk.
	* @param type: which list file to write (conference or channel)
	* @param content: the contents to write
	* @return boolean: write succeeds or not
	*/
	@Override
	public boolean writeFile(String type, String[] content) {
		
		String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
		logger.debug(new StringBuilder("Writing ").append(fileName).append("..."));
		
		try {			
			File file = new File(new StringBuilder(defaultPath).append(fileName).toString());
			boolean newlyCreated = false;
			
			/* create file IF not exists */
			if (!file.exists()) {
				logger.warn(new StringBuilder(fileName).append(" not exists. Creating a new one..."));
				file.createNewFile();
				newlyCreated = true;
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
			
			/* initialize file IF newly created */
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
			
			/* write file line by line */
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
	
	/**
	* This method is used to update the conference/channel list file (when a conference is deleted) in disk.
	* @param type: which list file to update (conference or channel)
	* @param confId: the ID of conference
	* @return boolean: update succeeds or not
	*/
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
			
			/* create temporary file */
			File tempFile = new File(origFile.getAbsolutePath().replace(fileName, new StringBuilder("Temp").append(fileName)));
			BufferedReader br = new BufferedReader(new FileReader(origFile.getAbsolutePath()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
			String line = null;
			
			/* copy old file to temporary file line by line without deleted information */
			while ((line = br.readLine()) != null)
				if (!line.trim().startsWith(confId)) {
					bw.write(line);
					bw.newLine();
				}
			bw.close();
			br.close();
			
			/* delete old file */
			if (!origFile.delete())
				logger.error(new StringBuilder("Cannot delete original ").append(fileName).append('.'));
			
			/* rename temporary file */
			if (!tempFile.renameTo(origFile))
				logger.error(new StringBuilder("Cannot rename new file to ").append(fileName).append('.'));
			
			return true;
			
		} catch (IOException e) {
			logger.error("Error in file I/O.");
		}
		
		
		return false;
	}
	
	/**
	* This method is used to get the statistics for a specific conference from disk.
	* @param folderPath: the path of conference
	* @return ConferenceStats: the statistics of conference
	*/
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
			ms = new MixerSum(mixSumData.get("quantum"), mixSumData.get("nConferenceId"), mixSumData.get("nSpeakers")
					, mixSumData.get("Speaker1"), mixSumData.get("Speaker2"), mixSumData.get("Speaker3"), mixSumData.get("Speaker4"));
		else
			logger.warn("Cannot find Mixer data.");
		if (ms != null)
			stats = new ConferenceStats(ms);
		
		return stats;
	}
	
	/**
	* This method is used to get the score for a specific conference from disk.
	* @param confId: the ID of conference
	* @return ConferenceScore: the score of conference
	*/
	private ConferenceScore getConferenceScore(String confId) {
		ConferenceScore score = null;
		List<List<String>> confs = readFile(new StringBuilder(defaultPath).append("ConfList.txt").toString(), ",", "all");
		for (List<String> conf : confs)
			if (conf.get(0).equals(confId))
				score = new ConferenceScore(Integer.parseInt(conf.get(3)), Integer.parseInt(conf.get(4)));
		
		return score;
	}
	
	/**
	* This method is used to get the statistics for a specific channel from disk.
	* @param folderPath: the conference path of channel
	* @param files: files of channel in list (i.e. stream processor, stream enhancer, mixer out)
	* @return ChannelStats: the statistics of channel
	*/
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
		
		/* read data from files */
		for (int i=0; i<files.size(); i++) {
			String fileName = files.get(i);
			if (fileName.contains("StreamProcessor"))
				strProData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
			if (fileName.contains("StreamEnhancer"))
				strEnhData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
			if (fileName.contains("MixerOutChannel"))
				mixOutData = convertDataStructure(readFile(new StringBuilder(folderPath).append("/").append(fileName).toString(), ",", maxStatsReadLine));
		}
		
		/* construct stream processor object with read data */
		if (strProData != null && !strProData.isEmpty()) {
			sp = new StreamProcessor(strProData.get("SeqNr"), strProData.get("muted"), strProData.get("RTP_streamBegin"), 
					strProData.get("RTP_isDelayed"), strProData.get("RTP_isReordered"), strProData.get("TimeStamp"), 
					strProData.get("Media_BufSize"), strProData.get("IATJitter"), /*strProData.get("CompJitter"), strProData.get("CalcJitter"),
					strProData.get("PacketsInPacketQueue"), strProData.get("PacketDropCounter"), */strProData.get("NS_speechPowerIn"),
					strProData.get("NS_speechPowerOut"), strProData.get("NS_noisePowerIn"), strProData.get("NS_noisePowerOut"), 
					strProData.get("AGC_speechLevelOut"), strProData.get("AGC_noiseLevelOut"), strProData.get("AGC_vadState"));
		} else {
			logger.warn("Cannot find StreamProcessor data.");
			sp = new StreamProcessor(new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999},
//					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999},
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999});
		}
		
		/* construct stream enhancer object with read data */
		if (strEnhData != null && !strEnhData.isEmpty()) {
			se = new StreamEnhancer(strEnhData.get("QNr"), strEnhData.get("QuantumsInJitterBuffer[Q]"), strEnhData.get("PacketScaleFast"), 
					strEnhData.get("PacketScaleSlow"), strEnhData.get("PacketScaleFactor"), strEnhData.get("QuantumUnderrunCounter [Q]"), 
					strEnhData.get("QuantumType"), strEnhData.get("PopTimeDelta"));
		} else {
			logger.warn("Cannot find StreamEnhancer data.");
			se = new StreamEnhancer(new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, 
					new double[] {-999999}, new double[] {-999999}, new double[] {-999999}, new double[] {-999999});
		}
		
		/* construct mixer out object with read data */
		if (mixOutData != null && !mixOutData.isEmpty()) {
			mo = new MixerOut(mixOutData.get("seqNo"), mixOutData.get("timestamp"));
		} else {
			logger.warn("Cannot find MixerOut data.");
			mo = new MixerOut(new double[] {-999999}, new double[] {-999999});
		}
		
		stats = new ChannelStats(sp, se, mo);
		
		return stats;
	}
	
	/**
	* This method is used to get the score for a specific channel from disk.
	* @param chanId: the ID of channel
	* @return ChannelScore: the score of channel
	*/
	private ChannelScore getChannelScore(String chanId) {
		ChannelScore score = null;
		List<List<String>> chans = readFile(new StringBuilder(defaultPath).append("ChanList.txt").toString(), ",", "all");
		for (List<String> chan : chans)
			if (chan.get(1).equals(chanId))
				score = new ChannelScore(Integer.parseInt(chan.get(2)), Integer.parseInt(chan.get(3)), Double.parseDouble(chan.get(4)));
		
		return score;
	}
	
	/**
	* This method is used to get the ID of conference for a specific channel.
	* @param chanId: the ID of channel
	* @return String: the ID of conference for the channel
	*/
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
	
	/**
	* This method is used to get the IDs of all conferences in the directory.
	* @param path: the path of directory
	* @return List: the IDs of all conferences in arraylist
	*/
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
	
	/**
	* This method is used to get the IDs of conferences that have been processed from ConfList.txt.
	* @param path: the path of directory
	* @return List: the IDs of processed conferences in arraylist
	*/
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
	
	/**
	* This method is used to get all folder names in the directory.
	* @param path: the path of directory
	* @return List: all folder names in the directory in arraylist
	*/
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
	
	/**
	* This method is used to get the file name for a conference/channel based on its ID.
	* @param path: the path of conference/channel
	* @param uuid: the ID of conference/channel
	* @param type: type of file
	* @return List: the file names for the ID in arraylist
	*/
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
	
	/**
	* This method is used to convert data (row -> column, column -> row).
	* @param data: the data with x rows and y columns
	* @return Map: the data with y rows and x columns in hashmap
	*/
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
}
