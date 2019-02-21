package edu.carleton.comp4601.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.*;

import edu.carleton.comp4601.model.Account;

import org.bson.*;

@XmlRootElement
public class AccountsMongoDB implements AccountStore {

	static String ID = "Identity";
	static String BALANCE = "Balance";
	static String DESCRIPTION = "Description";

	static AccountStore instance;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private MongoCollection<Document> coll;

	public AccountsMongoDB() {
		mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDatabase("bank");
		coll = db.getCollection("accounts");
	}

	private ConcurrentHashMap<Integer, Account> getAccounts() {
		FindIterable<Document> cursor = coll.find();
		ConcurrentHashMap<Integer, Account> map = new ConcurrentHashMap<Integer, Account>();
		MongoCursor<Document> c = cursor.iterator();
		while (c.hasNext()) {
			Document object = c.next();
			if (object.get(ID) != null)
				map.put((Integer) object.get(ID), new Account((Integer) object.get(ID), (Integer) object.get(BALANCE),
						(String) object.get(DESCRIPTION)));
		}
		return map;
	}

	public Map<Integer, Account> getModel() {
		return getAccounts();
	}

	public Account find(int id) {
		FindIterable<Document> cursor = coll.find(new BasicDBObject(ID, id));
		MongoCursor<Document> c = cursor.iterator();
		if (c.hasNext()) {
			Document object = c.next();
			return new Account((Integer) object.get(ID), (Integer) object.get(BALANCE),
					(String) object.get(DESCRIPTION));
		} else
			return null;
	}

	public Account open(int id, int balance, String description) {
		Account a = new Account(id, balance, description);
		Document doc = new Document(ID, a.getId()).append(BALANCE, a.getBalance()).append(DESCRIPTION,
				a.getDescription());
		coll.insertOne(doc);
		return a;
	}

	public long size() {
		return coll.count();
	}

	public static AccountStore getInstance() {
		if (instance == null)
			instance = new AccountsMongoDB();
		return instance;
	}

	@Override
	public boolean close(int id) {
		DeleteResult result = coll.deleteOne(new BasicDBObject(ID, id));
		return result != null;
	}

	public void update(Account a) {
		Document update = new Document(ID, a.getId()).append(BALANCE, a.getBalance()).append(DESCRIPTION,
				a.getDescription());

		coll.updateOne(Filters.eq(ID, a.getId().intValue()), new Document("$set", update));
	}
}
