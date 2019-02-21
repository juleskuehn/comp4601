package edu.carleton.comp4601.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import edu.carleton.comp4601.model.Account;

@XmlRootElement
public class Accounts implements AccountStore {
	
	private ConcurrentHashMap<Integer, Account> accounts;
	static Accounts instance;

	public Accounts() {
		accounts = new ConcurrentHashMap<Integer, Account>();
		accounts.put(1, new Account(1, 100, "Account for John Smith"));
		accounts.put(2, new Account(2, 25, "Account for Mickey Mouse"));
	}

	public Account find(int id) {
		return accounts.get(new Integer(id));
	}
	
	public Account open(int id, int balance, String description) {
		Account a = new Account(id, balance, description);
		accounts.put(new Integer(id), a);
		return a;
	}
	
	public boolean close(int id) {
		if (find(id) != null) {
			Integer no = new Integer(id);
			accounts.remove(no);
			return true;
		}
		else
			return false;
	}
	
	public Map<Integer, Account> getModel() {
		return accounts;
	}
	
	public long size() {
		return accounts.size();
	}
	
	public static AccountStore getInstance() {
		if (instance == null)
			instance = new Accounts();
		return instance;
	}
	
	public void update(Account acct) {
		accounts.put(acct.getId(), acct);
	}
}
