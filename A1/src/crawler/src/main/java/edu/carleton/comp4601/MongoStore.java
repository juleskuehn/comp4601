package edu.carleton.comp4601;

import java.io.IOException;
import java.util.ArrayList;
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
		
	public void add(int thisDocId, String name, String url, String content,
			ArrayList<String> tags, ArrayList<String> links) {
		Document doc = new Document("_id", thisDocId)
							.append("name", name)
							.append("url", url)
							.append("content", content)
							.append("tags", tags)
							.append("links", links)
							.append("crawltime", new Date());
		docColl.replaceOne(Filters.eq("_id", thisDocId), doc, new UpdateOptions().upsert(true));
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