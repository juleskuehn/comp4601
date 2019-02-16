package edu.carleton.comp4601;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

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

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class FirstCrawler extends WebCrawler {

	// TODO: Update to allow only the formats specified in Assignment
//    private static final Pattern ALLOW_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png|tif|jpeg|tiff)$");

    // Connection to Mongo
    private static MongoStore mongoStore = new MongoStore();
    private static CrawlerGraph g;
    int lastID;
    
    public void onStart() {
    	g = new CrawlerGraph("firstGraph");
    	// Create graph if one doesn't exist in DB, otherwise load existing graph
//    	if (mongoStore.getGraph() == null) {
//        	g = new CrawlerGraph("firstGraph");
//        } else {
//        	g = mongoStore.getGraph();
//        }
    }
    
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
    	// For testing my own page, which only has internal links
    	return true;
    	// Assignment requirement 11.1: "prevent off site page visits"
//        String href = url.getURL().toLowerCase();
//        return href.startsWith("https://sikaman.dyndns.org");
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
    	
    	// Add to graph
    	CrawlerVertex thisV = new CrawlerVertex(page);
    	g.addVertex(thisV);
    	CrawlerVertex parentV = g.getV().get((long) page.getWebURL().getParentDocid());
    	if (parentV != null) {
    		g.addEdge(thisV, parentV);      		
    	}
    	
    	// Get page properties via crawler4j methods
        String url = page.getWebURL().getURL();

        if (page.getParseData() instanceof HtmlParseData) {
        	// JSoup Document used here
            String pageHtml = ((HtmlParseData) page.getParseData()).getHtml();
            String baseURL = "http://" + page.getWebURL().getSubDomain() + page.getWebURL().getDomain();
            Document doc = Jsoup.parse(pageHtml, baseURL);	
            System.out.println("Jsoup says page title is: " + doc.title());
            
            // Get page text
            String pageText = doc.title() + doc.text() + doc.getElementsByTag("meta").attr("description");
            System.out.println("Page text:"+pageText);
            
            // Get link hrefs and text
            Elements links = doc.select("a[href]");
            String linkText = "";
            for (Element link : links) {
            	System.out.println(link.text() + ": " + link.attr("href"));
            	linkText += link.attr("href") + " " + link.text() + " ";
            }
            
            // Get images
            String selector = "img[src~=(?i)\\.(png|jpe?g|gif)]";
            Elements images = doc.select(selector);
            String imagesText = "";
            for (Element image : images) {
            	imagesText += image.attr("src") + " " + image.attr("alt") + " ";
            	System.out.println("Image in HTML:"+imagesText);
            }
            
            // Add to MongoStore
            mongoStore.add(page, pageText, linkText, imagesText, new Date());
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
    	    	
    	        String[] metadataNames = metadata.names();

    	        for(String name : metadataNames) {		        
    	           System.out.println(name + ": " + metadata.get(name));
    	        }
    	        System.out.println("!!!!!!"+handler.toString());
    	        
    	        mongoStore.addNonHTML(page, handler.toString(), metadata.toString(), new Date());
    	    	
    	    	stream.close();
    	    	lastID = page.getWebURL().getDocid();
    	    } catch (Exception e) { }
    	    	
        }
    }
    
    public void onBeforeExit() {
    	System.out.println(g);
//    	 Compute page rank (test)
    	PageRank.computePageRank(g.toAdjMatrix());
    	// Save the serialized graph to Mongo
    	mongoStore.add(g);
    }
    
}
