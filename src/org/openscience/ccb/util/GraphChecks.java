package org.openscience.ccb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.openscience.ccb.action.Action;
import org.openscience.ccb.process.Nil;
import org.openscience.ccb.process.Parallel;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.process.Restriction;

public class GraphChecks {
	AbstractBaseGraph<Process, DefaultEdge> graph;
    Map<Integer,List<Process>> edgesprovisional;

    public GraphChecks(Process p) throws CCBException{
        graph=createGraph(p);
    }

    private void followProcess(Process p, AbstractBaseGraph<Process, DefaultEdge> graph2){
        if(p==null)
            return;
        else{
            if(p instanceof Nil)
                return;
            else if(p instanceof Parallel){
                followProcess(((Parallel)p).getLeft(), graph2);
                followProcess(((Parallel)p).getRight(), graph2);
            }else if(p instanceof Prefix){
                graph2.addVertex(p);
                followProcess(((Prefix)p).getProcess(), graph2);
                for(Action action : ((Prefix)p).getPastactions()){
                    if(!edgesprovisional.containsKey(action.getKey()))
                        edgesprovisional.put(action.getKey(), new ArrayList<Process>());
                    edgesprovisional.get(action.getKey()).add(p);
                }
                if(((Prefix)p).getWeakAction()!=null && ((Prefix)p).getWeakAction().getKey()!=0){
                    if(!edgesprovisional.containsKey(((Prefix)p).getWeakAction().getKey()))
                        edgesprovisional.put(((Prefix)p).getWeakAction().getKey(), new ArrayList<Process>());
                    edgesprovisional.get(((Prefix)p).getWeakAction().getKey()).add(p);                	
                }
            }else if(p instanceof Restriction){
                followProcess(((Restriction)p).getProcess(), graph2);
            }
        }
    }

    /**
     * Makes a jgrapht graph out of a process.
     *
     * @throws CCBException 
     */
    private AbstractBaseGraph<Process, DefaultEdge> createGraph(Process p) throws CCBException
    {
    	AbstractBaseGraph<Process, DefaultEdge> graph = new SimpleGraph<Process, DefaultEdge>(DefaultEdge.class);
        edgesprovisional = new HashMap<Integer, List<Process>>();
        followProcess(p, graph);
        
        for(int key : edgesprovisional.keySet()){
            if(edgesprovisional.get(key).size()==2){
                if(graph.getEdge(edgesprovisional.get(key).get(0), edgesprovisional.get(key).get(1))!=null){
                    graph.setEdgeWeight(graph.getEdge(edgesprovisional.get(key).get(0), edgesprovisional.get(key).get(1)), graph.getEdgeWeight(graph.getEdge(edgesprovisional.get(key).get(0), edgesprovisional.get(key).get(1)))+1);
                }else{
                    graph.addEdge(edgesprovisional.get(key).get(0), edgesprovisional.get(key).get(1), new DefaultWeightedEdge());
                    graph.setEdgeWeight(graph.getEdge(edgesprovisional.get(key).get(0), edgesprovisional.get(key).get(1)), 1);
                }
            }
        }
        return graph;
    }
    
    /**
     * This is mainly for test purposes. Normally there should be no need to use the graph directly.
     * 
     * @return
     */
    public AbstractBaseGraph<Process, DefaultEdge> getGraph() {
        return graph;
    }

    public boolean areConnected(Process p1, Process p2){
        return new ConnectivityInspector<Process, DefaultEdge>((UndirectedGraph<Process, DefaultEdge>) graph).pathExists(p1, p2);
    }
    
    public boolean isIsomorph(Process p2) throws CCBException{
    	AbstractBaseGraph<Process, DefaultEdge> g2=createGraph(p2);
        VF2GraphIsomorphismInspector<Process, DefaultEdge> iso=new VF2GraphIsomorphismInspector<Process, DefaultEdge>(graph, g2, new VertexComparator(), new EdgeComparator(graph, g2));
        return iso.isomorphismExists();
    }
    
    public double distance(Process p1, Process p2){
    	return new DijkstraShortestPath<Process, DefaultEdge>(graph,p1, p2).getPathLength();
    }
}