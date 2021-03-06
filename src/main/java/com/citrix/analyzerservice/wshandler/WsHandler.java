package com.citrix.analyzerservice.wshandler;

import java.util.List;

import javax.ws.rs.DefaultValue;
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

/**
 * @author Xi Luo
 *
 */
@Path("/QueryAPI")
public class WsHandler {
	
	private static final Logger logger = Logger.getLogger(WsHandler.class);
	DtCollector dc = new DtCollector();
	
	@GET
	@Path("/Conferences")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceList(
			@DefaultValue("all") @QueryParam("size") String size,
			@DefaultValue("any") @QueryParam("from") String from,
			@DefaultValue("any") @QueryParam("to") String to) {
		
		logger.info("Request received: GET conference list.");
		
		List<LocalDbConference> conferenceList = dc.getConferenceList(size, from, to);				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conferenceList);
	}
	
	@GET
	@Path("/Conferences/{confId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceSummary(@PathParam("confId") String confId) {
		
		logger.info("Request received: GET conference summary.");
		
		LocalDbConference conference = dc.getConferenceSummary(confId);				
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConferenceDetails(@PathParam("confId") String confId) {
		
		logger.info("Request received: GET conference details.");
		
		LocalDbConference conference = dc.getConferenceDetails(confId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(conference);
	}
	
	@GET
	@Path("/Conferences/{confId}/Channels")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfChannels(@PathParam("confId") String confId) {
		
		logger.info("Request received: GET conference channels.");
		
		List<LocalDbChannel> channels = dc.getConfChannels(confId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channels);
	}
	
	@GET
	@Path("/Channels/{chanId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelSummary(@PathParam("chanId") String chanId) {
		
		logger.info("Request received: GET channel summary.");
		
		LocalDbChannel channel = dc.getChannelSummary(chanId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
	
	@GET
	@Path("/Channels/{chanId}/Details")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChannelDetails(@PathParam("chanId") String chanId) {
		
		logger.info("Request received: GET channel details.");
		
		LocalDbChannel channel = dc.getChannelDetails(chanId);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(channel);
	}
}
