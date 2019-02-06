package edu.carleton.comp4601;

import java.io.Serializable;

import edu.uci.ics.crawler4j.crawler.Page;

public class CrawlerVertex implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Mirrors structure of document storage in Mongo
	private long ID;
	private String URL, text, tags;
	
	public CrawlerVertex (long ID, String URL) {
		this.ID = ID;
		this.URL = URL;
	}
	
	public CrawlerVertex (Page page) {
		this(
			(long) page.getWebURL().getDocid(),
			page.getWebURL().toString()
		);
	}

	@Override
	public String toString() {
//		return "CrawlerVertex [ID=" + ID + ", URL=" + URL + ", text=" + text + ", tags=" + tags + "]";
		return URL;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
}
