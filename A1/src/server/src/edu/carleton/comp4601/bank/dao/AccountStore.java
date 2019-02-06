package edu.carleton.comp4601.bank.dao;

import java.util.Map;

import edu.carleton.comp4601.bank.model.Account;

public interface AccountStore {
	Account find(int id);
	Map<Integer, Account> getModel();
	long size();
	Account open(int id, int balance, String description);
	void update(Account acct);
	boolean close(int id);
}
