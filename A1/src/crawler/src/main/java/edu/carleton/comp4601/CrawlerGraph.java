package edu.carleton.comp4601;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import Jama.Matrix;

public class CrawlerGraph implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private Multigraph<CrawlerVertex, DefaultEdge> g;
	private ConcurrentHashMap<Long, CrawlerVertex> vertices;
	// When creating an adjacency matrix, need to map indices of rows/cols (same)
	// to the vertex ID
	private ConcurrentHashMap<Long, Integer> vIDtoAdjIdx;
	private ConcurrentHashMap<Integer, Long> adjIdxToVID;
	
	// Keep track of first vertex added as seed for DFS
	// Note that this only works when there is a single seed
	private CrawlerVertex firstV = null;
	
	public Matrix toAdjMatrix() {
		double[][] array = new double[vertices.size()][vertices.size()];
		System.out.printf("There are %d vertices.", vertices.size());
		int i = 0;
		// Map vertex ID to index in adjacency matrix 
		
		Iterator<ConcurrentHashMap.Entry<Long, CrawlerVertex>> it = vertices.entrySet().iterator();
		Iterator<CrawlerVertex> iterator = new DepthFirstIterator<>(g, firstV);
        while (iterator.hasNext()) {
        	i++;
            CrawlerVertex v = iterator.next();
//          vIDtoAdjIdx.put(v.getID(), i);
//	        adjIdxToVID.put(i, v.getID());
        }
	    // Fill in 1's in adjacency matrix where edge exists
    	for (DefaultEdge e : g.edgeSet()) {
//    	    int targetAdjIdx = vIDtoAdjIdx.get(g.getEdgeTarget(e).getID());
//    	    int sourceAdjIdx = vIDtoAdjIdx.get(g.getEdgeSource(e).getID());
    		int sourceIdx = (int) (g.getEdgeSource(e).getID() - 1);
    		int targetIdx = (int) (g.getEdgeTarget(e).getID() - 1);
    	    array[targetIdx][sourceIdx] = 1;
    	}
		return new Matrix(array);
	}

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
            s += "ID " + v.getID() + " " + v.toString() + "\n";
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
