package org.openscience.ccb.transition;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.process.Process;

/**
 * This class represents a potential transition. It does not actually execute anything.
 *
 */
public class SimpleTransition extends Transition {

	Process subprocesstransitioning;
	Action actionperformed;
	int keybroken;
	Transition triggeredTransition;
	
	
	/**
	 * This builds a transition undoing an action
	 * 
	 * @param process         The (sub-)process on which this actions works.
	 * @param actionundone    The action to undo.
	 */
	public SimpleTransition(Process process, Action actionundone, boolean undo){
		this.subprocesstransitioning=process;
		this.actionperformed=actionundone;
		if(undo)
			this.keybroken=actionundone.getKey();
	}
	
	public SimpleTransition(Process prefix, WeakAction weakAction, boolean b, Transition transition) {
		this(prefix, weakAction, b);
		triggeredTransition=transition;
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
		return "P"+this.getClone().counter+" "+subprocesstransitioning.toString(actionperformed,null, triggeredTransition!=null ? triggeredTransition.getActionperformed() : null)+";"+actionperformed+";"+keybroken;
	}

	@Override
	public Transition getTriggeredTransition() {
		return triggeredTransition;
	}
	
	@Override
	public void setTriggeredTransition(Transition triggeredTransition) {
		this.triggeredTransition = triggeredTransition;
	}

	@Override
	public void setSubprocesstransitioning(Process process) {
		this.subprocesstransitioning=process;		
	}
}
