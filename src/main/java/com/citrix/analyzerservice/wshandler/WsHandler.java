package com.citrix.analyzerservice.wshandler;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtcollector.DtCollector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

@Path("/QueryAPI")
public class WsHandler {
	
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
		LocalDbConference container = dc.getConferenceSummary(confId);
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(container);
	}
	
	@GET
	@Path("/Conferences/{confId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceDetails(@PathParam("confId") String confId) {
		LocalDbConference container = dc.getConferenceDetails(confId);
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(container);
	}
	
	@GET
	@Path("/Conferences/{confId}/Channels")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfChannels(@PathParam("confId") String confId) {
		List<LocalDbChannel> channels = dc.getConfChannels(confId);
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channels);
	}
	
	@GET
	@Path("/Channels/{chanId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelSummary(@PathParam("chanId") String chanId) {
		LocalDbChannel channel = dc.getChannelSummary(chanId);
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
	
	@GET
	@Path("/Channels/{chanId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelDetails(@PathParam("chanId") String chanId) {
		LocalDbChannel channel = dc.getChannelDetails(chanId);
				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
}
