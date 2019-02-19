package edu.carleton.comp4601;

public class CrawlerSharedConfig {
	public static CrawlerSharedConfig instance = new CrawlerSharedConfig();
	public static boolean firstToFinish = true;
	private CrawlerSharedConfig() {	}
	//synchronized method to control simultaneous access 
	synchronized public static CrawlerSharedConfig getInstance() {
	    if (instance == null) { 
	      instance = new CrawlerSharedConfig(); 
	    } 
	    return instance; 
	}
}
