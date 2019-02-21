package edu.carleton.comp4601.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {
	Integer id;
	String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User() {
	}

	public User(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
