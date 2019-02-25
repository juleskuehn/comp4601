package edu.carleton.comp4601.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.*;
import javax.websocket.EncodeException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import edu.carleton.comp4601.resources.MongoStore;
import edu.carleton.comp4601.utility.HTMLTableFormatter;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchException;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.utility.SearchServiceManager;


@Path("/sda")
public class SearchableDocumentArchive {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private static String BASE_URL = "http://localhost:8080/COMP4601-SDA/rest/sda/";
	public static MongoStore store;
	public static LuceneFacade lucene;
	private String name;

	public SearchableDocumentArchive() {
		
		name = "COMP4601 Searchable Document Archive: Jules Kuehn and Brian Ferch";
		store = MongoStore.getInstance();
		lucene = new LuceneFacade();
		try {
			SearchServiceManager.getInstance().start();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@GET
	public String printName() {
		return name;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createDocument(@FormParam("id") String id,
							   @FormParam("name") String name,
							   @FormParam("content") String content,
							   @FormParam("tags") String tags,
							   @FormParam("links") String links) throws IOException {
		
		Document doc = new Document();
		doc.setId(new Integer(id).intValue());
		doc.setName(name);
		doc.setContent(content);
		doc.setUrl(BASE_URL + id);
		if (tags.length() > 0) doc.setTags(commaStringToList(tags));
		if (links.length() > 0) doc.setLinks(commaStringToList(links));
		doc.setScore(0f);
		store.add(doc);
		lucene.index(true, true);
		return Response.status(200).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("{docId}")
	public String getDocument(@PathParam("docId") String _id) {
		int id = new Integer(_id).intValue();
		Document doc = store.getDocument(id);
		return documentToHTML(doc) + 
		"<br/><a href=\"" + ("edit/" + doc.getId()) + "\"><button>Edit</button></a>";
	}

	public String documentToHTML(Document doc) {
		String HTMLLinks = "";
		for (String link : doc.getLinks()) {
			HTMLLinks += "<a href=\"" + link + "\">" + link + "</a><br/>";
		}
		return "<html><head><title>" + doc.getName() + "</title><meta charset=\"UTF-8\">" + 
		"<meta name=\"description\" content=\"COMP 4601 Assignment 1\">" +
		"<meta name=\"tags\" content=\"" + String.join(", ", doc.getTags()) + "\">" +
		"<meta name=\"author\" content=\"Brian Ferch\"></head><body>" + 
		"<p>" + doc.getContent() + "</p>" +
		HTMLLinks + "</body></html>";
	}
	
	@DELETE
	@Path("{docId}")
	public Response deleteDocument(@PathParam("docId") String _id) {
		int id = new Integer(_id).intValue();
		int status = store.deleteOne(id) ? 200 : 204;
		lucene.index(true, true);
		return Response.status(status).build();
	}
	
	@GET
	@Path("/delete/{tags}")
	public Response deleteTags(@PathParam("tags") String tags) {
		List<String> tagsList = Arrays.asList(tags.split("\\+"));
		int status = store.deleteAllWithTags(tagsList) ? 200 : 204;
		lucene.index(true, true);
		return Response.status(status).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/documents")
	public String getDocuments() {
		HTMLTableFormatter tableFormatter = new HTMLTableFormatter();
		tableFormatter.singleColumn();
		return tableFormatter.html(store.getAll());
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/edit/{docId}")
	public String getDocumentEditForm(@PathParam("docId") String _id) {
		int id = new Integer(_id).intValue();
		Document doc = store.getDocument(id);
		return documentToHTML(doc) + 
		"<form action=\"../" + doc.getId() + "\" method=\"POST\">" + 
		"<br/><label for=\"tags\">Edit Tags</label><br/>" + 
		"<input name=\"tags\" type=\"text\" value=\"" + String.join(", ", doc.getTags()) + "\"/>" +
		"<br/><br/><label for=\"links\">Edit Links</label><br/>" +
		"<input name=\"links\" type=\"text\" value=\"" + String.join(", ", doc.getLinks())  + "\"/>" +
		"<br/><br/><input type=\"submit\" value=\"Submit\"/>" + 
		"</form>";
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("{docId}")
	public Response updateDocument(@PathParam("docId") String _id,
								   @FormParam("tags") String tags,
								   @FormParam("links") String links) throws IOException {
		int id = new Integer(_id).intValue();
		Document doc = store.getDocument(id);
		doc.setTags(commaStringToList(tags));
		doc.setLinks(commaStringToList(links));
		store.update(doc);
		lucene.index(true, true);
		return Response.status(200).build();
	}
	
	public ArrayList<String> commaStringToList(String string) {
		List<String> list = Arrays.asList(string.split(","));
		list.replaceAll(tag -> tag.trim());
		return new ArrayList<String>(list);
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/reset")
	public String resetIndex() {
		try {
	        lucene.index(true, true);
			return HTMLMessage("Reset success");
		} catch(Exception e) {
			return HTMLMessage("Error occured while indexing: " + e.getMessage());
		}
	}
	
	public String HTMLMessage(String message) {
		return "<html><head></head><body>" + message + "</body></html>";
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/pagerank")
	public String getPageRanks() {
		HTMLTableFormatter tableFormatter = new HTMLTableFormatter();
		tableFormatter.setColl2Title("PAGERANK");
		return tableFormatter.html(store.getAll());
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/boost")
	public String boost() {
		try {
	        lucene.index(true, true);
			return HTMLMessage("Boost success");
		} catch(Exception e) {
			return HTMLMessage("Error occured while boosting: " + e.getMessage());
		}	
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/noboost")
	public String noboost() {
		try {
	        lucene.index(true, false);
			return HTMLMessage("No Boost Success");
		} catch(Exception e) {
			return HTMLMessage("Error occured while no-boosting: " + e.getMessage());
		}
	}
	
	@GET
	@Path("query/{terms}")
	@Produces(MediaType.APPLICATION_XML)
	public DocumentCollection queryAsXML(@PathParam("terms") String terms) {
		DocumentCollection results = query(terms);
		//return results.getDocuments().size() > 0 ? results : "No documents found.";
		return results;
	}
	
	@GET
	@Path("query/{terms}")
	@Produces(MediaType.TEXT_HTML)
	public String queryAsHTML(@PathParam("terms") String terms) {
		HTMLTableFormatter tableFormatter = new HTMLTableFormatter();
		DocumentCollection results = query(terms);
		return results.getDocuments().size() > 0 ? tableFormatter.html(results) : "No documents found.";
	}
	
	public DocumentCollection query(String terms) {
  		String[] termsList = terms.split("\\+");
		for (int i = 0; i < termsList.length; i++) {
			if (termsList[i].contains(":")) {
				termsList[i] = termsList[i].replace(":", ":(");
				termsList[i] += ")";
			}
		}
		String query = String.join("+", termsList);
		
		DocumentCollection dc = new DocumentCollection();
		List<Document> docs = lucene.query(query);
		dc.setDocuments(docs != null ? docs : new ArrayList<Document>());
		return dc;
	}
	
	@GET
	@Path("search/{terms}")
	@Produces(MediaType.APPLICATION_XML)
	public DocumentCollection searchAsXML(@PathParam("terms") String terms) throws IOException, EncodeException, SearchException, URISyntaxException {
		DocumentCollection results = search(terms);
		return results;
	}
	
	@GET
	@Path("search/{terms}")
	@Produces(MediaType.TEXT_HTML)
	public String searchAsHTML(@PathParam("terms") String terms) throws IOException, EncodeException, SearchException, URISyntaxException {
		DocumentCollection results = search(terms);
		HTMLTableFormatter tableFormatter = new HTMLTableFormatter();
		return results.getDocuments().size() > 0 ? tableFormatter.html(results) : "No documents found.";
	}
	
	public DocumentCollection search(String terms) throws IOException, EncodeException, SearchException, URISyntaxException {
		SearchResult sr;
		DocumentCollection results;
		SearchServiceManager.getInstance().start();
		sr = SearchServiceManager.getInstance().search(terms);
		results = query(terms);
		try {
			sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		} finally {
			SearchServiceManager.getInstance().reset();
		}
		
		// Join local and distributed results
		List<Document> temp = results.getDocuments();
		temp.addAll(sr.getDocs());
		results.setDocuments(temp);
		
		return results;	
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/list")
	public String listServices() {
		ArrayList<String> services = SearchServiceManager.getInstance().list();
		String output = "<h1>List of connected services:</h1><ul>";
		for (String service : services) {
			output += "<li>" + service + "</li>";
		}
		output += "</ul>";
		return HTMLMessage(output);
	}
	
}
