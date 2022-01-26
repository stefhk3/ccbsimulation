package org.openscience.ccb.predicate;

import java.util.ArrayList;
import java.util.List;

import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.reduction.CCBVisitor;

public class ConnectivityVisitor implements CCBVisitor {
	
	private int[] keys;
	private Process processToCheck;
	public List<Process> connectedProcesses=new ArrayList<Process>();
	
	public ConnectivityVisitor(Process process){
		this.keys=process.getKeys();
		this.processToCheck=process;
	}

	@Override
	public void visit(Process process) {
		if(process!=processToCheck && process instanceof PrefixProcess){
			int[] keysInProc = ((PrefixProcess)process).getKeys();
			for(int i=0;i<keys.length;i++){
				for(int k=0;k<keysInProc.length;k++){
					if(keys[i]==keysInProc[k]){
						connectedProcesses.add(process);
						return;
					}
				}
			}
		}
	}
}
