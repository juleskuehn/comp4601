package edu.carleton.comp4601;


import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.AbstractGraph;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;

/*
 * A public utility class to visualize the graphs that
 * are generated as a result of a crawl. Thanks to the efforts
 * of COMP 4601 2017 class students.
 * 
 * Works for all of the JGrapht abstract graph classes; e.g.,
 * DefaultDirectedGraph 
 */

public class GraphLayoutVisualizer {
	
	public static String TITLE = "COMP 4601 Graph Visualization";
	
	public static <V, K> void visualizeGraph(AbstractGraph<K, V> g) {
		JGraph jgraph = new JGraph(new JGraphModelAdapter<K, V>(g));
		JFrame frame = new JFrame();
		/*
		 * Just change the reference to JGraphFastOrganicLayout
		 * to modify the type of layout requested. This algorithm
		 * is fast (only a few seconds on my 2015 iMac) whereas
		 * others take a very long time (> 1 minute).
		 */
	    JGraphLayout layout = new JGraphFastOrganicLayout();
	    JGraphFacade facade = new JGraphFacade(jgraph);
	    layout.run(facade);
	    Map<?, ?> nested = facade.createNestedMap(false, false);
	    jgraph.getGraphLayoutCache().edit(nested);

	    JScrollPane sp = new JScrollPane(jgraph);
	    frame.getContentPane().add(sp);
	    frame.setTitle(TITLE);
		JFrame.setDefaultLookAndFeelDecorated(true);		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 1000);
		frame.setVisible(true);
	}

}
