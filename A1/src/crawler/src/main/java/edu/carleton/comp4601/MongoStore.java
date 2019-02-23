package edu.carleton.comp4601;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	public MongoCollection<Document> getDocColl() {
		return docColl;
	}

	public MongoStore() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("sda");
		docColl = db.getCollection("pages");
		graphColl = db.getCollection("graph");
	}
	
	// Core document functions used by Crawler
	public void add(int thisDocId, String name, String url, String content,
			ArrayList<String> tags, ArrayList<String> links, String type, Double score) {
		Document doc = new Document("_id", thisDocId)
							.append("name", name)
							.append("url", url)
							.append("content", content)
							.append("tags", tags)
							.append("links", links)
							.append("crawltime", new Date())
							.append("type", type)
							.append("score", score);
		docColl.replaceOne(Filters.eq("_id", thisDocId), doc, new UpdateOptions().upsert(true));
	}
	
	public Document find(int id) {
		Document document = docColl.find(Filters.eq("_id", id)).first();
		return document;
	}
	
	public int getIdByURL(String url) {
		Document document = docColl.find(Filters.eq("url", url)).first();
		return Integer.parseInt(document.get("_id").toString());
	}
	
	public void setScore(int id, double newScore) {
		Document doc = find(id);
		doc.put("score", newScore);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}
	
	public boolean deleteOne(int id) {
		return docColl.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
	}
	
	public boolean deleteAllWithTag(String tag) {
		return docColl.deleteMany(Filters.eq("tags", tag)).getDeletedCount() > 0;
	}
	
	public void addLink(int id, String linkUrl) {
		Document doc = find(id);
		ArrayList<String> links = (ArrayList<String>) doc.get("links");
		links.add(linkUrl);
		doc.put("links", links);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}
	
	public void addTag(int id, String tag) {
		Document doc = find(id);
		ArrayList<String> tags = (ArrayList<String>) doc.get("tags");
		tags.add(tag);
		doc.put("tags", tags);
		docColl.replaceOne(Filters.eq("_id", id), doc, new UpdateOptions().upsert(true));
	}

	
	/////////////////
	// Graph
	public void addGraph(CrawlerGraph g) {
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
	
	public static void main(String[] args) {
		// For testing class methods
		MongoStore store = new MongoStore();
		
		String tag = "cool to be number 1";
		store.addTag(1, tag);
		store.addTag(2, tag);
	}

}