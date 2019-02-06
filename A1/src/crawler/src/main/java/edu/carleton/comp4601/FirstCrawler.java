package edu.carleton.comp4601;

import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class FirstCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png|tif|jpeg|tiff)$");

    // Connection to Mongo
    private static MongoStore mongoStore = new MongoStore();
    private static CrawlerGraph g;
    
    public void onStart() {
    	// Create graph if one doesn't exist in DB, otherwise load existing graph
    	if (mongoStore.getGraph() == null) {
        	g = new CrawlerGraph("firstGraph");
        } else {
        	g = mongoStore.getGraph();
        }
    }
    
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
    	// Assignment requirement 11.1: "prevent off site page visits"
        String href = url.getURL().toLowerCase();
        return href.startsWith("https://sikaman.dyndns.org:8443/");
    }

    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);

        if (page.getParseData() instanceof HtmlParseData) {
        	CrawlerVertex thisV = new CrawlerVertex(page);
        	g.addVertex(thisV);
        	CrawlerVertex parentV = g.getV().get((long) page.getWebURL().getParentDocid());
        	if (parentV != null) {
        		g.addEdge(thisV, parentV);      		
        	}
        	
        	// JSoup Document used here
            String pageHtml = ((HtmlParseData) page.getParseData()).getHtml();
            String baseURL = "http://" + page.getWebURL().getSubDomain() + page.getWebURL().getDomain();
            Document doc = Jsoup.parse(pageHtml, baseURL);	
            System.out.println("Jsoup says page title is: " + doc.title());
            
            // Get page text
            String pageText = doc.title() + doc.text() + doc.getElementsByTag("meta").attr("description");
            
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
            }
            
            // Add to MongoStore
            mongoStore.add(page, pageText, linkText, imagesText);
        }
    }
    
    public void onBeforeExit() {
    	// Save the serialized graph to Mongo
    	mongoStore.add(g);
    }
    
}
