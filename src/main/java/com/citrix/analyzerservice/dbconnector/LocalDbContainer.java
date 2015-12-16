package com.citrix.analyzerservice.dbconnector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;
import com.citrix.analyzerservice.model.ConferenceStats;
import com.citrix.analyzerservice.model.Mixer;
import com.citrix.analyzerservice.model.StreamEnhancer;
import com.citrix.analyzerservice.model.StreamProcessor;
import com.citrix.analyzerservice.util.Config;

public class LocalDbContainer implements IDbConnector {

	private transient String defaultPath = new Config().getPropValues().get("File_Directory");
	private LocalDateTime defaultTime = LocalDateTime.MIN;
		
	public LocalDbContainer() {}
	
	@Override
	public List<String> findNewConfIds() {
		List<String> newConfIds = new ArrayList<String>();
		
		List<String> allConfIds = findAllConfIds(defaultPath);
		List<String> proConfIds = findProConfIds(defaultPath + "/ConfList.txt");
		
		if (allConfIds.size() == proConfIds.size())
			return newConfIds;
		
		for (String s : allConfIds)
			if (!proConfIds.contains(s))
				newConfIds.add(s);
		
		return newConfIds;
	}
	
	@Override
	public List<LocalDbConference> findConferenceList() {
		List<String> confIds = findAllConfIds(defaultPath);		
		if (confIds == null || confIds.isEmpty())
			return null;
		
		List<LocalDbConference> conferences = new ArrayList<LocalDbConference>();
				
		for (int i=0; i<confIds.size(); i++) {
			LocalDbConference conference = findConference(confIds.get(i), false);
			conferences.add(conference);
		}
		
		return conferences;
	}
	
	@Override
	public LocalDbConference findConference(String confId, boolean showAll) {
		
		if (confId == null || confId.length() <= 0)
			return null;
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty())
			return null;
		
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
		if (score == null)
			score = new ConferenceScore(0, 0);
				
		// Get conference channel IDs
		List<LocalDbChannel> channels = findConfChannels(folderPath);
		
		LocalDbConference conference = new LocalDbConference(confId, timestamp, startTime, endTime, channels.size(), stats, score, channels);
		
		return conference;
	}
	
	@Override
	public List<LocalDbChannel> findConfChannels(String folderPath) {
		if (folderPath == null || folderPath.length() <= 0)
			return null;
		
		// if confId instead of folderPath is passed in
		if (folderPath.length() < 50) {
			String confId = folderPath;
			List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
			if (folder.isEmpty())
				return null;
			
			folderPath = defaultPath + folder.get(0);
		}
		
		List<LocalDbChannel> channels = new ArrayList<LocalDbChannel>();
		List<String> channelIds = getConfChannelIds(folderPath);
		
		// Get startTime and endTime
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
		
		for (int i=0; i<channelIds.size(); i++) {
			String chanId = channelIds.get(i);
			
			if (new File(defaultPath+"ChanList.txt").exists())
				channels.add(new LocalDbChannel(chanId, startTime, endTime, null, getChannelScore(chanId)));
			else
				channels.add(new LocalDbChannel(chanId, startTime, endTime, null, null));
		}
		
		return channels;
	}
	
	@Override
	public LocalDbChannel findChannel(String confId, String chanId, boolean showAll) {
		
		if (chanId == null || chanId.length() <= 0)
			return null;
		
		if (confId == null || confId.length() <= 0)
			confId = getChannelConference(chanId);
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty())
			return null;
		
		String folderPath = defaultPath + folder.get(0);
		
		// Get startTime and endTime
		LocalDateTime startTime = defaultTime;
		LocalDateTime endTime = defaultTime;
		
		// Get channel statistics
		ChannelStats stats = null;
		
		if (showAll) {
			List<String> files = getFileNameFromId(folderPath, chanId, "file");
			if (files != null && !files.isEmpty())
				stats = findChannelStats(folderPath, files);
		}
		
		// Get channel score
		ChannelScore score = getChannelScore(chanId);
		if (score == null)
			score = new ChannelScore(0, 0, 0);
		
		LocalDbChannel channel = new LocalDbChannel(chanId, startTime, endTime, stats, score);	
		
		return channel;
	}
	
	@Override
	public ChannelStats findChannelStats(String confId, String chanId) {
		
		if (confId == null || confId.length() <= 0 || chanId == null || chanId.length() <= 0)
			return null;
		
		List<String> folder = getFileNameFromId(defaultPath, confId, "folder");
		if (folder.isEmpty())
			return null;
		
		String folderPath = defaultPath + folder.get(0);		
		List<String> files = getFileNameFromId(folderPath, chanId, "file");
		if (files == null || files.isEmpty())
			return null;
		
		ChannelStats stats = findChannelStats(folderPath, files);
		
		return stats;
	}
	
	private ChannelStats findChannelStats(String folderPath, List<String> files) {
		
		if (files.isEmpty())
			return null;
		
		ChannelStats stats = null;
		StreamProcessor sp = null;
		Map<String, double[]> strProData = new HashMap<String, double[]>();
//		Map<String, double[]> strEnhData = null;
		
		for (int i=0; i<files.size(); i++) {
			strProData = convertDataStructure(readFile(folderPath+"/"+files.get(i), ","));
//			strEnhData = convertDataStructure(readFile(path, ","));
		}
		
		if (strProData != null && !strProData.isEmpty())
			sp = new StreamProcessor(strProData.get("SeqNr"), strProData.get("NS_speechPowerOut"), strProData.get("NS_noisePowerOut"));
		StreamEnhancer se = null;
		if (sp != null)
			stats = new ChannelStats(sp, se);
		
		return stats;
	}

	private List<List<String>> readFile(String path, String delimiter) {
		BufferedReader br = null;
		String line = "";
		List<List<String>> data = new ArrayList<List<String>>();		
		
		try {
			br = new BufferedReader(new FileReader(path));
			
			while ((line = br.readLine()) != null) {
				ArrayList<String> dataLine = new ArrayList<String>();
				String[] splitData = line.split("\\s*"+delimiter+"\\s*");
				for (int i=0; i<splitData.length; i++)
					if (!(splitData[i] == null) || !(splitData[i].length() == 0))
						dataLine.add(splitData[i].trim());
				data.add(dataLine);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return data;
		
	}
	
	@Override
	public boolean writeFile(String type, String[] content) {
		
		try {
			String fileName = type.equalsIgnoreCase("conference") ? "ConfList.txt" : "ChanList.txt";
			File file = new File(defaultPath + fileName);
			boolean newlyCreated = false;
			
			if (!file.exists()) {
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
			e.printStackTrace();
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
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return confIds;
	}
	
	private List<String> getFolderNames(String path) {
		List<String> folders = new ArrayList<String>();
		File folder = new File(path);
		File[] fileLists = folder.listFiles();
		
		if (fileLists.length <= 0)
			return new ArrayList<String>();
		
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
	
	private List<String> getConfChannelIds(String path) {
		List<String> channels = new ArrayList<String>();
		
		File folder = new File(path);
		File[] fileLists = folder.listFiles();
		
		if (fileLists.length <= 0)
			return null;
		
		for (int i=0; i<fileLists.length; i++)
			if (fileLists[i].isFile() && fileLists[i].getName().startsWith("MixerInChannel"))
				channels.add(fileLists[i].getName().substring(37, 72));
		
		return channels;
	}
	
	@Override
	public LocalDateTime findConferenceTimestamp(String folder) {
		
		// if confId instead of folderPath is passed in
		if (folder.length() < 50) {
			String confId = folder;
			List<String> folders = getFileNameFromId(defaultPath, confId, "folder");
			if (folder.isEmpty())
				return null;
			
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
		
		ConferenceStats stats = null;
		Mixer m = null;
		Map<String, double[]> mixData = null;

		mixData = convertDataStructure(readFile(folderPath + "/" + getFileNameFromId(folderPath, "", "mixer").get(0), ","));
		
		if (mixData != null && !mixData.isEmpty())
			m = new Mixer(mixData.get("quantum"), mixData.get("nConferenceId"), mixData.get("nSpeakers"));
		if (m != null)
			stats = new ConferenceStats(m);
		
		return stats;
	}
	
	private ConferenceScore getConferenceScore(String confId) {
		ConferenceScore score = null;
		List<List<String>> confs = readFile(defaultPath+"/ConfList.txt", ",");
		for (List<String> conf : confs)
			if (conf.get(0).equals(confId))
				score = new ConferenceScore(Integer.parseInt(conf.get(3)), Integer.parseInt(conf.get(4)));
		
		return score;
	}
	
	private ChannelScore getChannelScore(String chanId) {
		ChannelScore score = null;
		List<List<String>> chans = readFile(defaultPath+"/ChanList.txt", ",");
		for (List<String> chan : chans)
			if (chan.get(1).equals(chanId))
				score = new ChannelScore(Integer.parseInt(chan.get(2)), Integer.parseInt(chan.get(3)), Double.parseDouble(chan.get(4)));
		
		return score;
	}
	
	private String getChannelConference(String chanId) {
		List<String> allConfIds = findAllConfIds(defaultPath);
		
		for (int i=0; i<allConfIds.size(); i++) {
			String confId = allConfIds.get(i);
			List<String> channelIds = getConfChannelIds(defaultPath + getFileNameFromId(defaultPath, confId, "folder").get(0));
			for (String channelId : channelIds)
				if (channelId.equals(chanId))
					return confId;
		}	
		
		return null;
	}
}
