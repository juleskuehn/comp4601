package edu.carleton.comp4601;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Jama.Matrix;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

    // Connection to Mongo
    private static MongoStore mongoStore = new MongoStore();
    private static CrawlerGraph g;
    
    public void onStart() {
    	g = new CrawlerGraph("crawlerGraph");
    	// Create graph if one doesn't exist in DB, otherwise load existing graph
//    	if (mongoStore.getGraph() == null) {
//        	g = new CrawlerGraph("crawlerGraph");
//        } else {
//        	g = mongoStore.getGraph();
//        }
    }
    
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
    	// Assignment requirement 11.1: "prevent off site page visits"
        String href = url.getURL().toLowerCase();
        return href.startsWith("https://sikaman.dyndns.org")
        	          || href.startsWith("http://lol.jules.lol");
    }

    @Override
    public void visit(Page page) {
    	// Adaptive delay. Note that this incurs an additional HTTP request and
    	// doesn't account for different hosts having different response times
    	int minDelay = 200;
    	long start = System.currentTimeMillis();
    	try {
			Jsoup.connect(page.getWebURL().getURL()).get();
		} catch (IOException e) {
		}
    	long end = System.currentTimeMillis();
    	int responseTime = (int) (end - start);
    	myController.getConfig().setPolitenessDelay(Math.max(responseTime * 10, minDelay));
    	System.out.println("\n\n\nAdaptive response time: " + responseTime * 10);
    	
    	int thisID = page.getWebURL().getDocid();
    	int parentID = page.getWebURL().getParentDocid();
    	
    	// Add to graph
    	CrawlerVertex thisV = g.getV().get(thisID);
    	if (thisV == null) {
    		thisV = new CrawlerVertex(page);
    		g.addVertex(thisV);
    	}
    	CrawlerVertex parentV = g.getV().get(parentID);
    	if (parentV != null) {
    		g.addEdge(parentV, thisV);
    	}
    	
    	// Get page properties via crawler4j methods
        String url = page.getWebURL().getURL();
        int thisDocId = page.getWebURL().getDocid();
        System.out.println(url);

        if (page.getParseData() instanceof HtmlParseData) {
        	// JSoup Document used here
            String pageHtml = ((HtmlParseData) page.getParseData()).getHtml();
            String baseURL = url.substring(0, url.lastIndexOf("/")) + "/";
            Document doc = Jsoup.parse(pageHtml, baseURL);	
            
            // Get page content for SDA Document
            String name = doc.title();
            String content = doc.text();
            
            ArrayList<String> tags = new ArrayList<String>();
//            for (Element metaTag : doc.getElementsByTag("meta")) {
//            	tags.add(metaTag.attr("content"));
//            }
//            tags.add(name);
            
            // Get each link's href and text
            Elements linkEls = doc.select("a[href]");
            ArrayList<String> links = new ArrayList<String>();
            for (Element link : linkEls) {
            	links.add(link.attr("abs:href"));
            }
            
            // Add alt text from images to content
            String selector = "img[src~=(?i)\\.(png|jpe?g|gif)]";
            Elements images = doc.select(selector);
            for (Element image : images) {
            	content += " " + image.attr("alt");
            }
            
            // Insert before adding outgoing links
            // Neccessary to insert here because there may be self-links
            mongoStore.add(thisDocId, name, url, content, tags, links, page.getContentType(), 0.);
        
            for (Element link : linkEls) {
            	// Add links to visited pages to graph
            	try {
            		int linkDocId = mongoStore.getIdByURL(link.attr("abs:href"));
            		if (linkDocId == thisDocId) {
            			System.out.println("Self link at url " + page.getWebURL().toString());
            			g.addEdge(thisV, thisV);
            		} else {
            			System.out.printf("%d linkDocId Found!!", linkDocId);
            			// Add to graph
            			CrawlerVertex linkedV = g.getV().get(linkDocId);
            			g.addEdge(thisV, linkedV);
            		}
            	} catch (Exception e) {
            		System.out.printf("%s not found :(", link.attr("abs:href"));
            	}
            }
            
        } else {
        	// Retrieved URL is not HTML
        	// Parse with Tika
    	    try (InputStream stream = new URL(url).openStream()) {
    	    	// Tika setup
    	        AutoDetectParser parser = new AutoDetectParser();
    	    	BodyContentHandler handler = new BodyContentHandler();
    	    	Metadata metadata = new Metadata();
    	    	ParseContext context = new ParseContext();
    	    	parser.parse(stream, handler, metadata, context);
    	    	
    	    	// Text content parsed here
    	        String content = handler.toString();
    	        
    	        // Use metadata as tags
    	        ArrayList<String> tags = new ArrayList<String>();
//    	        for(String name : metadata.names()) {		        
//    	           tags.add(metadata.get(name));
//    	        }
    	        
//    	        Not adding outgoing links from non-web documents to the graph
    	        ArrayList<String> links = new ArrayList<String>();
    	        mongoStore.add(thisDocId, url.substring(url.lastIndexOf("/")+1), url, content, tags, links, page.getContentType(), 0.);
    	    	
    	    	stream.close();
    	    } catch (Exception e) { }
    	    	
        }
    }
    
	public static void scorePages() {
		Matrix PR = PageRank.computePageRank(g.toAdjMatrix());
		double[] scores = PR.getArray()[0];
		for (int i=0;i<scores.length;i++) {
			int docId = g.adjIdxToVID.get(i);
			mongoStore.setScore(docId, scores[i]);
		}
	}
    
    // (Shady) singleton pattern to communicate between threads
    @SuppressWarnings("static-access")
	public void onBeforeExit() {
    	CrawlerSharedConfig.getInstance().finished++;
    	// Is this crawler thread the last to finish?
    	if (CrawlerSharedConfig.getInstance().lastToFinish()) {
    		// (Prevent printing multiple times)
    		System.out.println(g);
    		scorePages();
    		GraphLayoutVisualizer.visualizeGraph(g.getG());
    	}
    	// Save the serialized graph to Mongo
    	mongoStore.addGraph(g);
    }

}