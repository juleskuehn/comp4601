package edu.carleton.comp4601.utility;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.EncodeException;

import java.util.logging.Level;

import edu.carleton.cas.logging.Logger;
import edu.carleton.cas.messaging.SearchMessage;
import edu.carleton.cas.messaging.SearchResultMessage;
import edu.carleton.cas.messaging.handlers.CloseMessageHandler;
import edu.carleton.cas.messaging.handlers.OpenMessageHandler;
import edu.carleton.cas.messaging.handlers.SearchMessageHandler;
import edu.carleton.cas.messaging.handlers.SearchResultMessageHandler;
import edu.carleton.cas.resources.Resource;
import edu.carleton.cas.resources.ResourceListener;
import edu.carleton.cas.security.UniqueDigits;
import edu.carleton.cas.websocket.WebsocketClientEndpoint;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;

/**
 * This class is the primary interface to the distributed search service. It
 * uses a websocket interface in order to communicate with a distributed search
 * service that maintains connections to all active search services.
 * 
 * Assumptions: That this works as a singleton. Name uniqueness is expected
 * here.
 * 
 * We connect to the websocket service when we create it.
 * 
 * @author tonywhite
 *
 */

public class SearchServiceManager implements ResourceListener {

	private static SearchServiceManager instance; // Using singleton pattern
	private static final String sUri = "wss://sikaman.dyndns.org:8443/DistributedSearch/router/";
	public static final Logger logger = new Logger(); // Used for logging messages
	private static final UniqueDigits ud = new UniqueDigits(8);		// Used for auto-naming

	private WebsocketClientEndpoint endpoint; // client web socket
	private String name; // Just a unique name for identification
	private ArrayList<String> services; // Known services
	private SearchResult sr; // Current search query (only allow 1 at a time)

	private SearchServiceManager(String name) {
		this.name = name;
		this.endpoint = null;
		this.services = new ArrayList<String>();
		this.sr = null;
	}

	/**
	 * Lazy creation of the singleton instance of the SearchServiceManager. A name
	 * composed of 8 random digits is used for the manager name.
	 * 
	 * @return Singleton instance of the SearchServiceManager.
	 */
	public static SearchServiceManager getInstance() {
		if (instance == null) {
			instance = new SearchServiceManager(ud.unique());
			try {
				Logger.setup("ssm");
			} catch (IOException e) {
				System.err.println("Could not set up ssm logging");
			}
		}
		return instance;
	}

	/**
	 * Lazy creation of the singleton instance of the SearchServiceManager. A
	 * user-provided name is used for the SearchServiceManager.
	 * 
	 * @return Singleton instance of the SearchServiceManager.
	 */
	public static SearchServiceManager getInstance(String name) {
		if (instance == null) {
			instance = new SearchServiceManager(name);
			try {
				Logger.setup("ssm");
			} catch (IOException e) {
				System.err.println("Could not set up ssm logging");
			}
		}
		return instance;
	}

	/**
	 * In order to use the SearchServiceManager we have to start it. This creates
	 * the web socket interface to the directory.
	 * 
	 * @throws URISyntaxException
	 */
	public synchronized void start() throws URISyntaxException {
		if (endpoint == null) {
			log(Level.INFO, "Starting distributed search services...");
			endpoint = new WebsocketClientEndpoint(new URI(sUri + name));
			endpoint.addListener(this);
			endpoint.addMessageHandler("close", new CloseMessageHandler());
			endpoint.addMessageHandler("open", new OpenMessageHandler());
			endpoint.addMessageHandler("search", new SearchMessageHandler());
			endpoint.addMessageHandler("result", new SearchResultMessageHandler());
			endpoint.open();
		}
	}

	/**
	 * We should stop the SearchServiceManager when our web service stops.
	 */
	public synchronized void stop() {
		log(Level.INFO, "Stopping distributed search services...");
		endpoint.close();
		endpoint = null;
	}

	/**
	 * Enables the use of the search capability. This call is required if a
	 * distributed search times out. Only one search at a time is supported.
	 */
	public synchronized void reset() {
		sr = null;
	}

	/**
	 * Add a known remote service
	 * 
	 * @param name
	 */
	public synchronized void add(String name) {
		services.add(name);
	}

	/**
	 * Remove a known remote service
	 * 
	 * @param name
	 */
	public synchronized void remove(String name) {
		services.remove(name);
	}

	/**
	 * Return a copy of all known service names. This can be used to support the
	 * Assignment 1 "list" RESTful web service
	 * 
	 * @return ArrayList of all known remote services
	 */
	public synchronized ArrayList<String> list() {
		return new ArrayList<String>(services);
	}

	/**
	 * This is the distributed interface: send query to all
	 * 
	 * @param query The query (e.g., eclipse+i:tony)
	 * @return The SearchResult which holds results of deferred synchronous
	 *         interaction
	 * @throws EncodeException
	 * @throws IOException
	 * @throws SearchException
	 */
	public synchronized SearchResult search(String query) throws IOException, EncodeException, SearchException {
		if (sr != null)
			throw new SearchException("Only allowed one search at a time");
		sr = new SearchResult(services.size());
		endpoint.sendMessage(SearchMessage.create(name, query));
		return sr;
	}

	/**
	 * Used when we need to respond to a SearchResultMessage. See
	 * SearchResultMessageHandler We may be too late so we have to deal with
	 * throwing the results away (potentially).
	 * 
	 * @param list The list of Documents which were received from a remote search
	 *             engine
	 */
	public synchronized void addResults(List<Document> list) {
		if (sr != null) {
			sr.addAll(list);
			sr.countDown();
			if (sr.latch.getCount() == 0) {
				sr = null;
			}
		}
	}

	/**
	 * Used when we need to respond to a SearchMessage. See SearchMessageHandler
	 * 
	 * @param to The name of a remote search engine. The destination for the results
	 * @param d  The DocumentCollection to be sent to the remote search engine
	 * @throws IOException
	 * @throws EncodeException
	 */
	public synchronized void sendResults(String to, DocumentCollection d) throws IOException, EncodeException {
		endpoint.sendMessage(SearchResultMessage.create(to, name, d));
	}
	
	/**
	 * Public interface to SSM logger
	 * @param level		Level at which log is created
	 * @param msg		Actual log
	 */
	public synchronized void log(Level level, String msg) {
		logger.log(level, msg);
	}

	/**
	 * Invoked when a WebsocketClientEndpoint either connects to or disconnects from
	 * the DistributedSearch web service. We log the lifecycle event and then, on
	 * close, reset the endpoint. The application can then reconnect to the
	 * DistributedSearch service with a call to the start method.
	 */
	@Override
	public synchronized void resourceEvent(Resource resource, String type, String description) {
		if (resource instanceof WebsocketClientEndpoint) {
			if (description.equals("open")) {
				log(Level.INFO, "Connected to search server");
			} else if (description.equals("close") || description.equals("error")) {
				endpoint = null;
				log(Level.INFO, "Disconnected from search server");
			}
		}
	}

}
