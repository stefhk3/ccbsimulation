package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.List;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import nu.xom.Attribute;
import nu.xom.Element;

public class Nil extends Process {

	public Nil(Element process) {
		if(process.getAttribute("counter")!=null)
			counter=Integer.parseInt(process.getAttributeValue("counter"));
	}
	
	public Nil(){
		super();
	}

	@Override
	public List<Action> getActionsReady() {
		return new ArrayList<Action>();
	}

	@Override
	public String toString(){
		if(counter>-1)
			return "P"+counter+"=0";
		else
			return "0";
	}
	
	@Override
	public String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction) {
		return toString();
	}
	
	@Override
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess){
		return new ArrayList<Transition>();
	}
	
	@Override
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
		throw new CCBException("Nil cannot do anything");
	}

	@Override
	public boolean contains(Process subprocesstransitioning) {
		return false;
	}

	@Override
	public Transition findSecondTriggeredTransition(Transition transition) {
		return null;
	}

	@Override
	public void accept(CCBVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public Process clone(){
	    return new Nil();
	}

    @Override
    public boolean removeKey(int bondnumber) {
        //Nothing to do
    	return false;
    }

	@Override
	public int[] getKeys() {
		return new int[0];
	}

	@Override
	public Element getXML() {
		Element nil=new Element("nil");
		nil.addAttribute(new Attribute("counter", Integer.toString(counter)));
		return nil;
	}
}
