package org.openscience.ccb.process;

import java.util.List;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import nu.xom.Element;

public abstract class Process {
	
	public abstract List<Action> getActionsReady();
	public abstract String toString();
	public abstract List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess) throws CCBException;
	public abstract void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException ;
	public abstract boolean contains(Process subprocesstransitioning);
	public abstract Transition findSecondTriggeredTransition(Transition transition) throws CCBException;
	public abstract void accept(CCBVisitor visitor);
    public abstract Process clone();
	public abstract String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction);
    public abstract void removeKey(int bondnumber);
    public abstract int[] getKeys();
    public int counter=-1;
    
    public abstract Element getXML();
}
