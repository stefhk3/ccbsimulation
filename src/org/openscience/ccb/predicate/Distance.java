package org.openscience.ccb.predicate;

import org.jgrapht.alg.DijkstraShortestPath;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

public class Distance{
	
	int maxDistance;
	int minDistance;
	boolean and;
	
	public Distance(int maxDistance, int minDistance, boolean and){
		this.maxDistance=maxDistance;
		this.minDistance=minDistance;
		this.and=and;
	}

	public boolean result(Process p1, Process p2, Process inProcess) throws CCBException {
		double distance = new GraphChecks(inProcess).distance(p1, p2);
		if(and)
			return distance<=maxDistance && distance>=minDistance;
		else
			return distance<=maxDistance || distance>=minDistance;
	}
}
