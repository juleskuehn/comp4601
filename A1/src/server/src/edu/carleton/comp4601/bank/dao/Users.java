package edu.carleton.comp4601.bank.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import edu.carleton.comp4601.bank.model.User;

@XmlRootElement
public class Users implements UserStore {
	
	private ConcurrentHashMap<Integer, User> users;
	static Users instance;

	public Users() {
		users = new ConcurrentHashMap<Integer, User>();
	}

	public User find(int id) {
		return users.get(new Integer(id));
	}
	
	public User create(int id, String name) {
		User user = new User(id, name);
		users.put(new Integer(id), user);
		return user;
	}
	
	public boolean delete(int id) {
		if (find(id) != null) {
			Integer no = new Integer(id);
			users.remove(no);
			return true;
		}
		else
			return false;
	}
	
	public Map<Integer, User> getModel() {
		return users;
	}
	
	public long size() {
		return users.size();
	}
	
	public static Users getInstance() {
		if (instance == null)
			instance = new Users();
		return instance;
	}
	
	public void update(User user) {
		users.put(user.getId(), user);
	}
}
