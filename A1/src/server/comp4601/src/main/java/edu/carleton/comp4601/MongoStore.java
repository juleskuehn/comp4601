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

	static String ID = "ID";
	static String URL = "URL";
	static String TEXT = "Text";
	static String TAGS = "Tags";
	static String LINKS = "Links";
	static String IMAGES = "Images";

	static MongoStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> coll;
	private MongoCollection<Document> graphColl;

	public MongoStore() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("first_crawler");
		coll = db.getCollection("pages");
		graphColl = db.getCollection("graph");
	}
	
	public void add(Page page) {
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
		Document doc = new Document(ID, page.getWebURL().getDocid())
				.append(URL, page.getWebURL().toString())
				.append(TEXT, htmlParseData.getText())
				.append(TAGS, htmlParseData.getMetaTagValue("description"));
		coll.insertOne(doc);
	}
	
	public void add(CrawlerGraph g) {
		Document graph;
		try {
			graph = new Document("_id", 1).append("Bytestream", Marshaller.serializeObject(g));
			graphColl.replaceOne(Filters.eq("_id", 1), graph, new UpdateOptions().upsert(true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void add(Page page, String pageText, String linkText, String imagesText) {
		Document doc = new Document(ID, page.getWebURL().getDocid())
				.append(URL, page.getWebURL().toString())
				.append(TEXT, pageText)
				.append(LINKS, linkText)
				.append(IMAGES, imagesText);
		coll.insertOne(doc);
	}

//	private ConcurrentHashMap<Integer, Account> getAccounts() {
//		FindIterable<Document> cursor = coll.find();
//		ConcurrentHashMap<Integer, Account> map = new ConcurrentHashMap<Integer, Account>();
//		MongoCursor<Document> c = cursor.iterator();
//		while (c.hasNext()) {
//			Document object = c.next();
//			if (object.get(ID) != null)
//				map.put((Integer) object.get(ID), new Account((Integer) object.get(ID), (Integer) object.get(BALANCE),
//						(String) object.get(DESCRIPTION)));
//		}
////		System.out.println(map);
//		return map;
//	}
//
//	public Map<Integer, Account> getModel() {
//		return getAccounts();
//	}
//
//	public Account find(int id) {
//		FindIterable<Document> cursor = coll.find(new BasicDBObject(ID, id));
//		MongoCursor<Document> c = cursor.iterator();
//		if (c.hasNext()) {
//			Document object = c.next();
//			return new Account((Integer) object.get(ID), (Integer) object.get(BALANCE),
//					(String) object.get(DESCRIPTION));
//		} else
//			return null;
//	}
//

//
//	public long size() {
//		return coll.count();
//	}
//
//	public static AccountStore getInstance() {
//		if (instance == null)
//			instance = new AccountsMongoDB();
//		return instance;
//	}
//
//	@Override
//	public boolean close(int id) {
//		DeleteResult result = coll.deleteOne(new BasicDBObject(ID, id));
//		return result != null;
//	}
//
//	public void update(Account a) {
//		Document update = new Document(ID, a.getId()).append(BALANCE, a.getBalance()).append(DESCRIPTION,
//				a.getDescription());
//
//		coll.updateOne(Filters.eq(ID, a.getId().intValue()), new Document("$set", update));
//	}
}