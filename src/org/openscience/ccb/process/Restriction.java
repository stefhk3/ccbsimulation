package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

import nu.xom.Attribute;
import nu.xom.Element;

public class Restriction extends Process {

	List<Action> restrictions;
	Process process;
	GraphChecks gc;
	
	public Restriction(List<Action> restrictions, Process process) throws CCBException{
		this.restrictions=restrictions;
		this.process=process;
		gc=new GraphChecks(this);
	}
	
	public Restriction(Element process, Map<Set<String>, String> gamma, List<Action> weakactions) throws CCBException {
		restrictions=new ArrayList<Action>();
		for(int i=0;i<process.getChildElements("action").size();i++){
			Element actionel=process.getChildElements().get(i);
			restrictions.add(new StrongAction(actionel.getValue()));
		}
		Element childprocess=null;
		if(process.getChildElements("prefix").size()>0)
			childprocess=process.getFirstChildElement("prefix");
		else if(process.getChildElements("nil").size()>0)
			childprocess=process.getFirstChildElement("nil");
		else if(process.getChildElements("parallel").size()>0)
			childprocess=process.getFirstChildElement("parallel");
		else if(process.getChildElements("restriction").size()>0)
			childprocess=process.getFirstChildElement("restriction");
		else
			throw new CCBException("Element "+process.toXML()+" has no valid child process");
		this.process=CCBParser.parseProcess(childprocess,gamma, weakactions);
		if(process.getAttribute("counter")!=null)
			counter=Integer.parseInt(process.getAttributeValue("counter"));
		gc=new GraphChecks(this);
	}
	
	
	@Override
	public List<Action> getActionsReady() {
		List<Action> actionsReadyChild = process.getActionsReady();
		Iterator<Action> itactions = actionsReadyChild.iterator();
		while (itactions.hasNext()) {
			Action childaction=itactions.next();
			for(Action restriction : restrictions){
				if(childaction.equals(restriction.getActionName()))
					itactions.remove();
			}
		}
		return actionsReadyChild;
	}

	@Override
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess) throws CCBException {
		List<Transition> unrestrictedTransitions = new ArrayList<Transition>();
		List<Transition> transitions = process.inferTransitions(synchronize, configuration, rootProcess);
		for(Transition transition : transitions){
			boolean triggeredrestricted=false;
			Transition secondTriggered=null;
			//If the triggered action is restricted, the whole transition becomes impossible
			if(transition.getTriggeredTransition()!=null){
				secondTriggered=this.findSecondTriggeredTransition(transition.getTriggeredTransition());
				if(secondTriggered!=null && synchronize.isSychronized(transition.getTriggeredTransition().getActionperformed().getActionName(),secondTriggered.getActionperformed().getActionName())!=null && restrictions.contains(new StrongAction(synchronize.isSychronized(transition.getTriggeredTransition().getActionperformed().getActionName(),secondTriggered.getActionperformed().getActionName()))))
					triggeredrestricted=true;
			}
			if(!restrictions.contains(new StrongAction(transition.getActionperformed().getActionName())) && !triggeredrestricted){// && secondTriggered!=null){
				unrestrictedTransitions.add(transition);
			}
		}
		return unrestrictedTransitions;
	}
	
	@Override
	public String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction){
		StringBuffer process=new StringBuffer();
		if(counter>-1)
			process.append("P"+counter+"=");
		process.append("(");
		process.append(this.process.toString(actionperformed1, actionperformed2, triggeredAction));
		process.append(") \\ {");
		boolean first=true;
		for(Action restriction : restrictions){
			if(!first){
				process.append(",");
			}else{
				first=false;
			}
			process.append(restriction.toString());
		}
		process.append("}");
		return process.toString();
	}

	@Override
	public String toString(){
		return toString(null, null, null);
	}
	
	@Override
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
		process.executeTransition(transition, newkey, rootProcess);
		if(transition.getTriggeredTransition()!=null){
			Transition secondTriggered=rootProcess.findSecondTriggeredTransition(transition.getTriggeredTransition());
			if(secondTriggered!=null){
				this.executeTransition(secondTriggered,0,rootProcess);
				this.executeTransition(transition.getTriggeredTransition(),0,rootProcess);
				transition.setTriggeredTransition(null);
			}
		}
	}
	
	@Override
	public Transition findSecondTriggeredTransition(Transition transition)  throws CCBException {
		return this.process.findSecondTriggeredTransition(transition);
	}

	@Override
	public boolean contains(Process subprocesstransitioning) {
		if(process.contains(subprocesstransitioning))
			return true;
		else
			return false;
	}


	@Override
	public void accept(CCBVisitor visitor) {
		visitor.visit(this);
		process.accept(visitor);
	}

    @SuppressWarnings("unchecked")
    @Override
    public Process clone(){
    	try{
    		return new Restriction((List<Action>)((ArrayList<Action>)restrictions).clone(), process.clone());
		} catch (CCBException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public Process getProcess() {
        return process;
    }

    @Override
    public boolean removeKey(int bondnumber) {
        return this.process.removeKey(bondnumber);        
    }

	@Override
	public int[] getKeys() {
		return process.getKeys();
	}

	@Override
	public Element getXML() {
		Element restriction=new Element("restriction");
		for(Action a : this.restrictions){
			Element action=new Element("action");
			action.appendChild(a.getActionName());
			restriction.appendChild(action);
		}
		restriction.appendChild(process.getXML());
		restriction.addAttribute(new Attribute("counter", Integer.toString(counter)));
		return restriction;
	}
}