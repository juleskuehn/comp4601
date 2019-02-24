package edu.carleton.comp4601.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;

public class MongoStore {

	public static String COMP_4601_BASE = System.getProperty("user.home") + File.separator;
	
	static MongoStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> docColl;

	public MongoStore() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("sda");
		docColl = db.getCollection("pages");
	}
	
	public MongoCollection<Document> getDocColl() {
		return docColl;
	}
	
	public void add(Document doc) {
		docColl.replaceOne(Filters.eq("_id", doc.getInteger("_id")), doc, new UpdateOptions().upsert(true));
	}
	
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
		add(doc);
	}
	
	// Convenience functions for interaction with front-end
	// TODO ensure return types match with assignment requirements
	public void add(edu.carleton.comp4601.dao.Document sdaDocument) {
		add(toMongoDocument(sdaDocument));
	}
	
	public Document find(int id) {
		Document document = docColl.find(Filters.eq("_id", id)).first();
		return document;
	}
	
	public edu.carleton.comp4601.dao.Document getDocument(int id) {
		Document mongoDocument = find(id);
		return toSdaDocument(mongoDocument);
	}
	
	public void update(edu.carleton.comp4601.dao.Document sdaDocument) {
		Document update = toMongoDocument(sdaDocument);
		docColl.updateOne(Filters.eq("_id", sdaDocument.getId().intValue()), new Document("$set", update));
	}
	
	// This nastiness is because the SDA Document class takes a Float (not a Double) for score
	// Otherwise, we could use the Document(mongoDocument) map constructor
	public edu.carleton.comp4601.dao.Document toSdaDocument(Document mongoDocument) {
		edu.carleton.comp4601.dao.Document sdaDocument = new edu.carleton.comp4601.dao.Document();
		sdaDocument.setId(mongoDocument.getInteger("_id"));
		sdaDocument.setName(mongoDocument.getString("name"));
		sdaDocument.setUrl(mongoDocument.getString("url"));
		sdaDocument.setContent(mongoDocument.getString("content"));
		sdaDocument.setTags((ArrayList<String>) mongoDocument.get("tags"));
		sdaDocument.setLinks((ArrayList<String>) mongoDocument.get("links"));
		sdaDocument.setScore(((Double) mongoDocument.get("score")).floatValue());
		return sdaDocument;
	}
	
	public Document toMongoDocument(edu.carleton.comp4601.dao.Document sdaDocument) {
		return new Document("_id", (int) sdaDocument.getId())
				.append("name", sdaDocument.getName())
				.append("url", sdaDocument.getUrl())
				.append("content", sdaDocument.getContent())
				.append("tags", sdaDocument.getTags())
				.append("links", sdaDocument.getLinks())
				.append("crawltime", new Date())
				.append("type", "MIME-type is undefined in sdaDocument")
				.append("score", (double) sdaDocument.getScore());
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
	
	public boolean deleteAllWithTags(List<String> tags) {
		return docColl.deleteMany(Filters.all("tags", tags)).getDeletedCount() > 0;
	}
	
	public edu.carleton.comp4601.dao.DocumentCollection getByTag(String tag) {
		edu.carleton.comp4601.dao.DocumentCollection coll = new edu.carleton.comp4601.dao.DocumentCollection();
		ArrayList<edu.carleton.comp4601.dao.Document> tmpList = new ArrayList<edu.carleton.comp4601.dao.Document>();
		FindIterable<Document> docs = tag == null ? docColl.find() : docColl.find(Filters.eq("tags", tag));
		MongoCursor<Document> cursor = docs.iterator();
        try {
            while(cursor.hasNext()) {               
                tmpList.add(toSdaDocument(cursor.next()));
            }
        } finally {
            cursor.close();
        }
        coll.setDocuments(tmpList);
		return coll;
	}
	
	public edu.carleton.comp4601.dao.DocumentCollection getAll() {
		return getByTag(null);
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
	
	public static MongoStore getInstance() {
		if (instance == null) {
			instance = new MongoStore();
			System.out.println("NEW");
		}
			
		return instance;
	}
}