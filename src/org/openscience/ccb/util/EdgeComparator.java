package org.openscience.ccb.util;

import java.util.Comparator;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.openscience.ccb.process.Process;

public class EdgeComparator implements Comparator<DefaultEdge> {
    
	DefaultDirectedGraph<Process, DefaultEdge> graph1;
	DefaultDirectedGraph<Process, DefaultEdge> graph2;
    
    public EdgeComparator(DefaultDirectedGraph<Process, DefaultEdge> graph1, DefaultDirectedGraph<Process, DefaultEdge> graph2){
        this.graph1=graph1;
        this.graph2=graph2;
    }

    @Override
    public int compare(DefaultEdge o1, DefaultEdge o2) {
        if(graph1.containsEdge(o1)){
            if(graph1.getEdgeWeight(o1)==graph2.getEdgeWeight(o2))
                return 0;
            else if (graph1.getEdgeWeight(o1)<graph2.getEdgeWeight(o2))
                return -1;
            else
                return 1;
        }else{
            if(graph2.getEdgeWeight(o1)==graph1.getEdgeWeight(o2))
                return 0;
            else if (graph2.getEdgeWeight(o1)<graph1.getEdgeWeight(o2))
                return -1;
            else
                return 1;
        }
    }

}
