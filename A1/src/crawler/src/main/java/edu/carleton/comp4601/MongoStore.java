package edu.carleton.comp4601;

import java.io.IOException;
import java.util.Date;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import edu.uci.ics.crawler4j.crawler.Page;

import org.bson.*;
import org.bson.types.Binary;

public class MongoStore {

	static MongoStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> docColl;
	private MongoCollection<Document> graphColl;

	public MongoStore() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("first_crawler");
		docColl = db.getCollection("pages");
		graphColl = db.getCollection("graph");
	}
	
	// TODO update to store required information specified in Assignment
	public void add(Page page, String pageText, String linkText, String imagesText, Date crawlTime) {
		Document doc = new Document("_id", page.getWebURL().getDocid())
				.append("URL", page.getWebURL().toString())
				.append("TEXT", pageText)
				.append("LINKS", linkText)
				.append("IMAGES", imagesText)
				.append("CRAWLTIME", crawlTime);
		docColl.replaceOne(Filters.eq("_id", page.getWebURL().getDocid()), doc, new UpdateOptions().upsert(true));
	}
	
	// TODO update to store required information specified in Assignment
	public void addNonHTML(Page page, String text, String metadata, Date crawlTime) {
		Document doc = new Document("_id", page.getWebURL().getDocid())
				.append("URL", page.getWebURL().toString())
				.append("TEXT", text)
				.append("METADATA", metadata)
				.append("CRAWLTIME", crawlTime);
		docColl.replaceOne(Filters.eq("_id", page.getWebURL().getDocid()), doc, new UpdateOptions().upsert(true));
	}
	
	public Document getDocument(int id) {
		// TODO not working when last tested
		Document document = docColl.find(Filters.eq("_id", id)).first();
		return document;
	}
	
	public int getIdByURL(String url) {
		// TODO not working when last tested
		System.out.println(url);
		Document document = docColl.find(Filters.eq("URL", url)).first();
		return Integer.parseInt(document.get("_id").toString());
	}
	
	public void add(CrawlerGraph g) {
		Document graph;
		try {
			graph = new Document("_id", 1).append("Bytestream", Marshaller.serializeObject(g));
			graphColl.replaceOne(Filters.eq("_id", 1), graph, new UpdateOptions().upsert(true));
		} catch (IOException e) {
		}
	}
	
	public CrawlerGraph getGraph() {
		Document g = graphColl.find(Filters.eq("_id", 1)).first();
		if (g != null) {
			System.out.println("Found the graph");
			try {
				return (CrawlerGraph) Marshaller.deserializeObject(((Binary) g.get("Bytestream")).getData());
			} catch (IOException e) {
			}
		} else {
			System.out.println("No graph found in MongoDB");
		}
		return null;
	}

}