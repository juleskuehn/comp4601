package edu.carleton.comp4601.a1.server.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.*;

import edu.carleton.comp4601.a1.server.model.User;

import org.bson.*;

@XmlRootElement
public class UsersMongoDB implements UserStore {

	static String ID = "Identity";
	static String BALANCE = "Balance";
	static String USERNAME = "Username";

	static UserStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> coll;

	public UsersMongoDB() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("bank");
		coll = db.getCollection("users");
	}
	
	private ConcurrentHashMap<Integer, User> getUsers() {
		FindIterable<Document> cursor = coll.find();
		ConcurrentHashMap<Integer, User> map = new ConcurrentHashMap<Integer, User>();
		MongoCursor<Document> c = cursor.iterator();
		while (c.hasNext()) {
			Document object = c.next();
			if (object.get(ID) != null)
				map.put((Integer) object.get(ID), new User((Integer) object.get(ID), (String) object.get(USERNAME)));
		}
		return map;
	}

	public Map<Integer, User> getModel() {
		return getUsers();
	}

	public User find(int id) {
		FindIterable<Document> cursor = coll.find(new BasicDBObject(ID, id));
		MongoCursor<Document> c = cursor.iterator();
		if (c.hasNext()) {
			Document object = c.next();
			return new User((Integer) object.get(ID), (String) object.get(USERNAME));
		} else
			return null;
	}

	public User create(int id, String username) {
		User user = new User(id, username);
		Document doc = new Document(ID, user.getId()).append(USERNAME, user.getName());
		coll.insertOne(doc);
		return user;
	}

	public long size() {
		return coll.count();
	}

	public static UserStore getInstance() {
		if (instance == null)
			instance = new UsersMongoDB();
		return instance;
	}

	@Override
	public boolean delete(int id) {
		DeleteResult result = coll.deleteOne(new BasicDBObject(ID, id));
		return result != null;
	}

	public void update(User user) {
		Document update = new Document(ID, user.getId()).append(USERNAME, user.getName());

		coll.updateOne(Filters.eq(ID, user.getId().intValue()), new Document("$set", update));
	}
}
