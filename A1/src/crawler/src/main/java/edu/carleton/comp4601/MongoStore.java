package edu.carleton.comp4601;

import java.io.IOException;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

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
	
	// For use with JSoup
	public void add(Page page, String pageText, String linkText, String imagesText) {
		Document doc = new Document("ID", page.getWebURL().getDocid())
				.append("URL", page.getWebURL().toString())
				.append("Text", pageText)
				.append("LINKS", linkText)
				.append("IMAGES", imagesText);
		docColl.insertOne(doc);
	}
	
	// Used before JSoup introduced
	@Deprecated
	public void add(Page page) {
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
		Document doc = new Document("ID", page.getWebURL().getDocid())
				.append("URL", page.getWebURL().toString())
				.append("TEXT", htmlParseData.getText())
				.append("TAGS", htmlParseData.getMetaTagValue("description"));
		docColl.insertOne(doc);
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