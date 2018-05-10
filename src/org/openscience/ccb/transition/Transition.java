package org.openscience.ccb.transition;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.process.Process;

public abstract class Transition {
    
    Process clone;
	
	public abstract Process getSubprocesstransitioning();
	public abstract void setSubprocesstransitioning(Process process);
	public abstract Action getActionperformed();
	public abstract int getKeybroken();
	public abstract String toString();
	public abstract Transition getTriggeredTransition();
	public abstract void setTriggeredTransition(Transition triggeredTransition);
	public void setClone(Process clone){
	    this.clone=clone;
	}
	public Process getClone(){
	    return clone;
	}
}
