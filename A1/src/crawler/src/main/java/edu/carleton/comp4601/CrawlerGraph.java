package edu.carleton.comp4601;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

public class CrawlerGraph implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private Multigraph<CrawlerVertex, DefaultEdge> g;
	private ConcurrentHashMap<Long, CrawlerVertex> vertices;
	
	// Keep track of first vertex added as seed for DFS
	// Note that this only works when there is a single seed
	private CrawlerVertex firstV = null;

	public CrawlerGraph(String name) {
		this.name = name;
		this.vertices = new ConcurrentHashMap<Long, CrawlerVertex>();
		this.g = new Multigraph<CrawlerVertex, DefaultEdge>(DefaultEdge.class);
	}
	
	public synchronized boolean addVertex(CrawlerVertex v) {
		if (firstV == null) {
			firstV = v;
		}
		this.vertices.put(v.getID(), v);
		return g.addVertex(v);
	}
	
	public synchronized boolean removeVertex(CrawlerVertex v) {
		this.vertices.remove(v.getID());
		return g.removeVertex(v);
	}
	
	public synchronized DefaultEdge addEdge(CrawlerVertex v1, CrawlerVertex v2) {
		return g.addEdge(v1, v2);
	}
	
	public synchronized boolean removeEdge(DefaultEdge e) {
		return g.removeEdge(e);
	}
	
	@Override
	public String toString()
    {
		String s = "CrawlerGraph:\n";
        Iterator<CrawlerVertex> iterator = new DepthFirstIterator<>(g, firstV);
        while (iterator.hasNext()) {
            CrawlerVertex v = iterator.next();
            s += v.toString() + "\n";
        }
        return s;
    }
	
	// Simple getters and setters
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Multigraph<CrawlerVertex, DefaultEdge> getG() {
		return g;
	}

	public void setG(Multigraph<CrawlerVertex, DefaultEdge> g) {
		this.g = g;
	}

	public ConcurrentHashMap<Long, CrawlerVertex> getV() {
		return vertices;
	}

	public void setV(ConcurrentHashMap<Long, CrawlerVertex> v) {
		this.vertices = v;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
}
