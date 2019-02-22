package edu.carleton.comp4601.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.*;
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
import edu.carleton.comp4601.dao.Document;


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
	private String name;

	public SearchableDocumentArchive() {
		
		name = "COMP4601 Searchable Document Archive: Jules Kuehn and Brian Ferch";
		
		store = MongoStore.getInstance();
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
							   @FormParam("url") String url, 
							   @FormParam("tags") String tags,
							   @FormParam("links") String links) throws IOException {
		
		Document doc = new Document();
		doc.setId(new Integer(id).intValue());
		doc.setName(name);
		doc.setContent(content);
		doc.setUrl(BASE_URL + id);
		doc.setTags(formTagsToList(tags));
		doc.setLinks(formLinksToList(links));
		doc.setScore(0f);
		store.add(doc);
		
		return Response.status(200).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("{docId}")
	public String getDocument(@PathParam("docId") String _id) {
		int id = new Integer(_id).intValue();
		Document doc = store.getDocument(id);
		return documentToHTML(doc);
	}

	public String documentToHTML(Document doc) {
		String HTMLLinks = "";
		for (String link : doc.getLinks()) {
			HTMLLinks += "<a href=\"" + link + "\">" + link + "</a>";
		}
		return "<html><head><title>" + doc.getName() + "</title><meta charset=\"UTF-8\">" + 
		"<meta name=\"description\" content=\"COMP 4601 Assignment 1\">" +
		"<meta name=\"tags\" content=\"" + String.join(", ", doc.getTags()) + "\">" +
		"<meta name=\"author\" content=\"Brian Ferch\"></head><body>" + 
		"<p>" + doc.getContent() + "</p>" +
		HTMLLinks +
		"</body></html>";		
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
		"<br/><input type=\"submit\" value=\"Submit\"/>\n" + 
		"</form>";
		//"<label for=\"links\">Edit Links</label><br/>" +
		//"<input name=\"links\" type=\"text\" value=\"" +  + "\"/>" +
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("{docId}")
	public Response updateDocument(@PathParam("docId") String _id,
								   @FormParam("tags") String tags) throws IOException {
		int id = new Integer(_id).intValue();
		Document doc = store.getDocument(id);
		doc.setTags(formTagsToList(tags));
		//doc.setLinks(formLinksToList(links));
		store.update(doc);
		return Response.status(200).build();
	}
	
	public ArrayList<String> formTagsToList(String tags) {
		List<String> list = Arrays.asList(tags.split(","));
		list.replaceAll(tag -> tag.trim());
		return new ArrayList<String>(list);
	}
	
	public ArrayList<String> formLinksToList(String links) {
	    List<String> list = Arrays.asList(links.split(","));
	    list.replaceAll(linkText -> BASE_URL + linkText.trim());
		return new ArrayList<String>(list);
	}
}
