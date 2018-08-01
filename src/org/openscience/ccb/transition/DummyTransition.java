package org.openscience.ccb.transition;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.process.Process;

/**
 * This class is only used to put comments into a list of transitions. It does nothing real.
 *
 */
public class DummyTransition extends Transition{

	private String text="";
	
	public DummyTransition(String text){
		this.text=text;
	}
	
	@Override
	public Process getSubprocesstransitioning() {
		return null;
	}

	@Override
	public void setSubprocesstransitioning(Process process) {
	}

	@Override
	public Action getActionperformed() {
		return null;
	}

	@Override
	public int getKeybroken() {
		return 0;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public Transition getTriggeredTransition() {
		return null;
	}

	@Override
	public void setTriggeredTransition(Transition triggeredTransition) {
	}
}
