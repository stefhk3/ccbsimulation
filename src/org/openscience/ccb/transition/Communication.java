package org.openscience.ccb.transition;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.process.Process;

public class Communication extends Transition {

	SimpleTransition transition1;
	SimpleTransition transition2;
	Process subprocesstransitioning;
	Action actionperformed;
	Transition triggeredTransition;
	
	@Override
	public void setTriggeredTransition(Transition triggeredTransition) {
		this.triggeredTransition = triggeredTransition;
	}

	public SimpleTransition getTransition1() {
		return transition1;
	}

	public SimpleTransition getTransition2() {
		return transition2;
	}

	int keybroken;
	
	/**
	 * Builds a communication from two SimpleTransitions. The caller must make sure the transitions actually form a communication. 
	 * There is not validation done.
	 * 
	 * @param transition1
	 * @param transition2
	 */
	public Communication(SimpleTransition transition1, SimpleTransition transition2, Process process, Action actiondone, boolean undo){
		this.subprocesstransitioning=process;
		this.actionperformed=actiondone;
		this.transition1=transition1;
		this.transition2=transition2;
		if(undo)
			keybroken=transition1.keybroken;
	}

	
	@Override
	public Process getSubprocesstransitioning() {
		return subprocesstransitioning;
	}

	@Override
	public Action getActionperformed() {
		return actionperformed;
	}

	@Override
	public int getKeybroken() {
		return keybroken;
	}
	
	@Override
	public String toString(){
		return "P"+this.getClone().counter+" "+subprocesstransitioning.toString(transition1.getActionperformed(),transition2.getActionperformed(), (transition1.getTriggeredTransition()!=null ? transition1.getTriggeredTransition().getActionperformed() : (transition2.getTriggeredTransition() !=null ? transition2.getTriggeredTransition().getActionperformed() : null )))+";"+actionperformed+";"+keybroken;
	}
	
	@Override
	public Transition getTriggeredTransition() {
		if(transition1.getTriggeredTransition()!=null)
			return transition1.getTriggeredTransition();
		else if(transition2.getTriggeredTransition()!=null)
			return transition2.getTriggeredTransition();
		else
			return triggeredTransition;
	}

	@Override
	public void setSubprocesstransitioning(Process process) {
		this.subprocesstransitioning=process;		
	}
}
