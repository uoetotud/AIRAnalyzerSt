package com.citrix.analyzerservice.wshandler;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtcollector.DtCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("/QueryAPI")
public class WsHandler {
	
	private static final Logger logger = Logger.getLogger(WsHandler.class);
	DtCollector dc = new DtCollector();

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
		
		LocalDbConference conference = dc.getConferenceSummary(confId);				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceDetails(@PathParam("confId") String confId) {
		
		logger.info("Received getting conference details request.");
		
		LocalDbConference conference = dc.getConferenceDetails(confId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Channels")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfChannels(@PathParam("confId") String confId) {
		
		logger.info("Received getting conference channels request.");
		
		List<LocalDbChannel> channels = dc.getConfChannels(confId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channels);
	}
	
	@GET
	@Path("/Channels/{chanId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelSummary(@PathParam("chanId") String chanId) {
		
		logger.info("Received getting channel summary request.");
		
		LocalDbChannel channel = dc.getChannelSummary(chanId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
	
	@GET
	@Path("/Channels/{chanId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelDetails(@PathParam("chanId") String chanId) {
		
		logger.info("Received getting channel details request.");
		
		LocalDbChannel channel = dc.getChannelDetails(chanId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
}
