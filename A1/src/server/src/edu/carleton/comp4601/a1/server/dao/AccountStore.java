package edu.carleton.comp4601.a1.server.dao;

import java.util.Map;

import edu.carleton.comp4601.a1.server.model.Account;

public interface AccountStore {
	Account find(int id);
	Map<Integer, Account> getModel();
	long size();
	Account open(int id, int balance, String description);
	void update(Account acct);
	boolean close(int id);
}
