package com.citrix.analyzerservice.wshandler;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtcollector.DtCollector;
import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.util.Cache;
import com.citrix.analyzerservice.util.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

@Path("/QueryAPI")
public class WsHandler {
	
	private static final Logger logger = Logger.getLogger(WsHandler.class);
	DtCollector dc = new DtCollector();
	
	// Get cache configurations
	private static Map<String, String> configs = new Config().getPropValues();
	private static String cacheEnabled = configs.get("Cache_Enable");
	private static String cacheType = configs.get("Cache_Type");
	private static String cacheTimeOut = configs.get("Cache_TimeOut");
	private static String cacheCleanInterval = configs.get("Cache_Clean_Interval");
	private static String cacheSize = configs.get("Cache_Size");
	// End get configurations
	
	private static Cache<String, CacheItem> cache = null;
	private static boolean cacheIsEnabled = false;
	
	// Create cache if enabled (put here to ensure ONLY ONCE execution)
	static {
		if (cacheEnabled.equalsIgnoreCase("true")) {
			logger.debug("Cache enabled.");
			cache = new Cache<String, CacheItem>(cacheType, Long.parseLong(cacheTimeOut), Long.parseLong(cacheCleanInterval), Integer.parseInt(cacheSize));
			cacheIsEnabled = true;
		} else {
			logger.debug("Cache NOT enabled.");
		}
	}
	// End create cache

	@GET
	@Path("/greeting")
	@Produces(MediaType.APPLICATION_JSON)
	public String greeting(@QueryParam("name") String name) {
		
		String resp = "Hello " + name;
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		
		return gson.toJson(resp);
	}
	
	@GET
	@Path("/Conferences")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceList() {
		
		logger.info("Received getting conference list request.");
		
		List<LocalDbConference> conferenceList = dc.getConferenceList();
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
//		    @Override
//		    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//		        Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
//		        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//		    }
//		}).create();
		
		return gson.toJson(conferenceList);
	}
	
	@GET
	@Path("/Conferences/{confId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceSummary(@PathParam("confId") String confId) {
		
		logger.info("Received getting conference summary request.");
		
		LocalDbConference conference = null;
		
		/* NEW */
		if (cacheIsEnabled) {
			String key = confId + "_summary";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " summary cached - return directly.");
				
				conference = (LocalDbConference) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " summary NOT cached.");
				
				conference = dc.getConferenceSummary(confId);				
				cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
			}
		} else {
			conference = dc.getConferenceSummary(confId);
		}
		/* NEW */
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceDetails(@PathParam("confId") String confId) {
		
		logger.info("Received getting conference details request.");
		
		LocalDbConference conference = null;
		
		/* NEW */
		if (cacheIsEnabled) {
			String key = confId + "_details";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " details cached - return directly.");
				
				conference = (LocalDbConference) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + confId + " details NOT cached.");
				
				conference = dc.getConferenceDetails(confId);				
				cache.put(key, new CacheItem(conference, System.currentTimeMillis()));
			}
		} else {
			conference = dc.getConferenceDetails(confId);
		}
		/* NEW */
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Channels")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfChannels(@PathParam("confId") String confId) {
		
		logger.info("Received getting conference channels request.");
		
		List<LocalDbChannel> channels = null;
		
		// TO TEST
		/* NEW */
		if (cacheIsEnabled) {
			String key = confId + "_channels";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + confId + " channels cached - return directly.");
				
				channels = (List<LocalDbChannel>) cache.fetch(key).getCacheObject();		
			} else {
				logger.info("Conference " + confId + " channels NOT cached.");
				
				channels = dc.getConfChannels(confId);				
				cache.put(key, new CacheItem(channels, System.currentTimeMillis()));
			}
		} else {
			channels = dc.getConfChannels(confId);
		}
		/* NEW */
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channels);
	}
	
	@GET
	@Path("/Channels/{chanId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelSummary(@PathParam("chanId") String chanId) {
		
		logger.info("Received getting channel summary request.");
		
		LocalDbChannel channel = null;
		
		/* NEW */
		if (cacheIsEnabled) {
			String key = chanId + "_summary";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + chanId + " summary cached - return directly.");
				
				channel = (LocalDbChannel) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " summary NOT cached.");
				
				channel = dc.getChannelSummary(chanId);				
				cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
			}
		} else {
			channel = dc.getChannelSummary(chanId);
		}
		/* NEW */
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
	
	@GET
	@Path("/Channels/{chanId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelDetails(@PathParam("chanId") String chanId) {
		
		logger.info("Received getting channel details request.");
		
		LocalDbChannel channel = null;
		
		/* NEW */
		if (cacheIsEnabled) {
			String key = chanId + "_details";
			if (cache != null && cache.contains(key)) {
				logger.info("Conference " + chanId + " details cached - return directly.");
				
				channel = (LocalDbChannel) cache.fetch(key).getCacheObject();			
			} else {
				logger.info("Conference " + chanId + " details NOT cached.");
				
				channel = dc.getChannelDetails(chanId);				
				cache.put(key, new CacheItem(channel, System.currentTimeMillis()));
			}
		} else {
			channel = dc.getChannelDetails(chanId);
		}
		/* NEW */
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
}
