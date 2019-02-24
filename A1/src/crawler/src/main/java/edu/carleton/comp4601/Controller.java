package edu.carleton.comp4601;

import java.io.File;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	public static String COMP_4601_BASE = System.getProperty("user.home") + File.separator;
	
    public static void main(String[] args) throws Exception {

        String crawlStorageFolder = COMP_4601_BASE + "crawler_temp/";
        int numberOfCrawlers = 1;
        CrawlerSharedConfig.getInstance().numCrawlers = numberOfCrawlers;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setPolitenessDelay(1000); // Changes adaptively in FirstCrawler::visit()
        config.setMaxDepthOfCrawling(-1); // Default -1 is unlimited depth
        config.setMaxPagesToFetch(100); // Default -1 for unlimited pages
        config.setIncludeBinaryContentInCrawling(true);
        config.setResumableCrawling(false);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // Multiple format test
        controller.addSeed("http://lol.jules.lol/parsertest/");
        
        // Page rank example 1: 3 pages [link1, link2, link3]
        controller.addSeed("http://lol.jules.lol/parsertest/link1.html");
        
        // Page rank example 2: 6 pages [d0, d1, d2, d3, d4, d5, d6]
        controller.addSeed("http://lol.jules.lol/parsertest/d1.html");
        controller.addSeed("http://lol.jules.lol/parsertest/d5.html");
        controller.addSeed("http://lol.jules.lol/parsertest/eclipse.html");
        
        
        // Seeds specified in the assignment.
//        controller.addSeed("https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/handouts/");
        controller.addSeed("https://sikaman.dyndns.org:8443/WebSite/rest/site/courses/4601/resources/N-0.html");
        
        controller.start(Crawler.class, numberOfCrawlers);
    }
    
}