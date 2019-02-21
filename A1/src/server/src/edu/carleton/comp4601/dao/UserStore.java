package edu.carleton.comp4601.dao;

import java.util.Map;

import edu.carleton.comp4601.model.User;

public interface UserStore {
	User find(int id);
	Map<Integer, User> getModel();
	long size();
	User create(int id, String name);
	void update(User acct);
	boolean delete(int id);
}
