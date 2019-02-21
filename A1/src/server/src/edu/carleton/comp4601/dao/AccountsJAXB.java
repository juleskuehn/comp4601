package edu.carleton.comp4601.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import edu.carleton.comp4601.model.Account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

@XmlRootElement
public class AccountsJAXB implements AccountStore {

	static AccountStore instance;
	JAXBContext context;
	Marshaller m;
	Unmarshaller um;
	String accountsPath = "/Users/brianferch/Code/COMP4601 - Web Systems/SecondBankWorkspace/Accounts";

	public AccountsJAXB() {		
		try {
			context = JAXBContext.newInstance(Account.class);
	        m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	        um = context.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private ConcurrentHashMap<Integer, Account> getAccounts() {
		ConcurrentHashMap<Integer, Account> map = new ConcurrentHashMap<Integer, Account>();
		File accountsFolder = new File(accountsPath);
		try {	
			for (File file : accountsFolder.listFiles()) {
				Account acct = (Account) um.unmarshal(new FileReader(accountsPath + file.getName()));
				map.put(acct.getId(), new Account(acct.getId(), acct.getBalance(), acct.getDescription()));				
			}
		} catch (NullPointerException | FileNotFoundException | JAXBException e) {
			// No Accounts
		}
		return map;
	}

	public Map<Integer, Account> getModel() {
		return getAccounts();
	}

	public Account find(int id) {
        try {
			Account acct = (Account) um.unmarshal(new FileReader(accountsPath + id + ".xml"));
			return new Account(acct.getId(), acct.getBalance(), acct.getDescription());
		} catch (FileNotFoundException | JAXBException e) {
			return null;
		}
	}

	public Account open(int id, int balance, String description) {
		Account acct = new Account(id, balance, description);
		try {
	        m.marshal(acct, new File(accountsPath + id + ".xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return acct;
	}

	public long size() {
		return new File(accountsPath).list().length;
	}

	public static AccountStore getInstance() {
		if (instance == null)
			instance = new AccountsJAXB();
		return instance;
	}

	@Override
	public boolean close(int id) {
		return new File(accountsPath + id + ".xml").delete();
	}

	public void update(Account a) {
        try {
			m.marshal(a, new File(accountsPath + a.getId() + ".xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
