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

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    // Connection to Mongo
    private static MongoStore mongoStore = new MongoStore();
    private static CrawlerGraph g;
    
    public void onStart() {
    	if (mongoStore.getGraph() == null) {
        	g = new CrawlerGraph("firstGraph");
        } else {
        	g = mongoStore.getGraph();
        }
    }
    
    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            return false;
        }

        // Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
        return href.startsWith("https://www.ics.uci.edu/");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
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
//        	mongoStore.add(page);
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
            
//            String text = htmlParseData.getText();
//            String html = htmlParseData.getHtml();
//            Set<WebURL> links = htmlParseData.getOutgoingUrls();
//
//            logger.debug("Text length: {}", text.length());
//            logger.debug("Html length: {}", html.length());
//            logger.debug("Number of outgoing links: {}", links.size());
        }

//        Header[] responseHeaders = page.getFetchResponseHeaders();
//        if (responseHeaders != null) {
//            logger.debug("Response headers:");
//            for (Header header : responseHeaders) {
//                logger.debug("\t{}: {}", header.getName(), header.getValue());
//            }
//        }
//
//        logger.debug("=============");
    }
    
    public void onBeforeExit() {
        // Save the graph to Mongo
    	mongoStore.add(g);
    	System.out.print(g);
    	System.out.print(g.getV());
    }
}
