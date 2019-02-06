package edu.carleton.comp4601.bank.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Account {
	Integer id;
	Integer balance;
	String description;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Account() {
	}
	
	public Account(int id) {
		this(id, 0,"");
	}

	public Account(int id, int balance, String description) {
		this.id = id;
		this.balance = balance;
		this.description = description;
	}
	
	public Account(int id, int balance) {
		this.id = id;
		this.balance = balance;
		this.description = "";
	}

	public int balance() {
		return balance;
	}
	
	public void deposit(int amount) {
		if (amount > 0)
			balance += amount;
	}
	
	public void withdraw(int amount) {
		if (amount > 0)
			balance -= amount;
	}
}
