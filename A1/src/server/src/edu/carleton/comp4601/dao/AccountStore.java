package edu.carleton.comp4601.dao;

import java.util.Map;

import edu.carleton.comp4601.model.Account;

public interface AccountStore {
	Account find(int id);
	Map<Integer, Account> getModel();
	long size();
	Account open(int id, int balance, String description);
	void update(Account acct);
	boolean close(int id);
}
