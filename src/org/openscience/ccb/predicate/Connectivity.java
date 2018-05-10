package org.openscience.ccb.predicate;

import org.openscience.ccb.process.Process;

public class Connectivity {
	
	int maxConnectivity;
	
	public Connectivity(int maxConnectivity){
		this.maxConnectivity=maxConnectivity;
	}

	public boolean result(Process p, Process inProcess) {
		return connectivity(p, inProcess)<=maxConnectivity;
	}

	private int connectivity(Process p, Process inProcess) {
		ConnectivityVisitor cv=new ConnectivityVisitor(p);
		inProcess.accept(cv);
		return cv.connectedProcesses.size();
	}
}
