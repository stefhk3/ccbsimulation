package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * @author shk12
 *
 */
public class PrefixProcess extends Process {
	Prefix prefix;
	Process process;
	GraphChecks gc;

	public PrefixProcess(Prefix prefix, Process process) throws CCBException {
		this.prefix=prefix;
		this.process=process;
		gc=new GraphChecks(this);
	}
	
	public PrefixProcess(Action action, Process process) throws CCBException{
		List<Action> actions=new ArrayList<Action>();
		actions.add(action);
		Prefix prefix=new Prefix(actions);
		this.process=process;
		this.prefix=prefix;
		gc=new GraphChecks(this);
	}

	public PrefixProcess(StrongAction strongAction, WeakAction weakAction, Process process) throws CCBException {
		List<Action> actions=new ArrayList<Action>();
		actions.add(strongAction);
		Prefix prefix=new Prefix(actions, weakAction);
		this.process=process;
		this.prefix=prefix;
		gc=new GraphChecks(this);
	}

	public PrefixProcess(Element process, Map<Set<String>, String> gamma,  List<Action> weakactions) throws CCBException {
		for(int k=0;k<process.getChildElements("prefix").size();k++){
			process=process.getChildElements("prefix").get(k);
			ArrayList<Action> actions = new ArrayList<Action>();
			for(int i=0;i<process.getChildElements("action").size();i++){
				Element actionel=process.getChildElements("action").get(i);
				Action action;
				if(weakactions.contains(new StrongAction(actionel.getValue())))
					action=new WeakAction(actionel.getValue());
				else
					action=new StrongAction(actionel.getValue());
				if(actionel.getAttribute("key")!=null){
					action.setKey(Integer.parseInt(actionel.getAttribute("key").getValue()));
					actions.add(action);
				}else{
					actions.add(action);
				}
			}
			WeakAction weakAction=null;
			if(process.getChildElements("weakaction").size()>0){
				weakAction=new WeakAction(process.getChildElements("weakaction").get(0).getValue());
				if(process.getChildElements("weakaction").get(0).getAttribute("key")!=null){
					weakAction.setKey(Integer.parseInt(process.getChildElements("weakaction").get(0).getAttribute("key").getValue()));
				}
			}
			Prefix prefix;
			if(weakAction==null)
				prefix=new Prefix(actions);
			else
				prefix=new Prefix(actions, weakAction);
			if(this.prefix==null){
				this.prefix=prefix;
			}else {
				this.prefix = new Prefix(prefix,  this.prefix);
			}
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

	public List<Action> getPastactions() {
		return prefix.getPastactions();
	}

	public List<Action> getFreshactions() {
		return prefix.getFreshactions();
	}

	public Prefix getPrefix() {
		return prefix;
	}

	@Override
	public List<Action> getActionsReady() {
		List<Action> actionsReady=new ArrayList<Action>();
		actionsReady.addAll(prefix.getActionsReady());
		actionsReady.addAll(process.getActionsReady());
		return actionsReady;
	}
	
	public Set<Action> getAllActionsInOne(){
		return prefix.getAllActionsInOne();
	}
	
	@Override
	public String toString(){
		return toString(null, null, null);
	}

	@Override
	public String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction) {
		return toString(actionperformed1, actionperformed2, triggeredAction, false);
	}
	
	public String toStringAllInOne(){
		return toString(null, null, null, true);
	}

	public String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction, boolean allActionsInOne) {
		String prefix=this.prefix.toString( actionperformed1,  actionperformed2,  triggeredAction,  allActionsInOne);
		if(this.prefix.getNextPrefix()!=null)
			prefix="("+prefix+")";
		String value="";
		if(counter>-1)
			value="P"+counter+"=";
		return value+prefix+"."+process.toString(actionperformed1, actionperformed2, triggeredAction);
	}
	
	@Override
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess) throws CCBException{
		List<Transition> resultingTransitions=new ArrayList<Transition>();
		resultingTransitions.addAll(prefix.inferTransitions(synchronize, configuration, rootProcess, this));
		return resultingTransitions;
	}

	@Override
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
		if(transition.getSubprocesstransitioning()==process){
			process.executeTransition(transition, newkey, rootProcess);
			return;
		}else {
			prefix.executeTransition(transition, newkey, rootProcess);
		}
	}

	@Override
	public boolean contains(Process subprocesstransitioning) {
		if(subprocesstransitioning==this || process.contains(subprocesstransitioning))
			return true;
		else
			return false;
	}
	
	@Override
	public Transition findSecondTriggeredTransition(Transition transition) throws CCBException {
		return prefix.findSecondTriggeredTransition(transition, this);
	}


	@Override
	public void accept(CCBVisitor visitor) {
		visitor.visit(this);
	}

    @Override
    public Process clone(){
        try {
			return new PrefixProcess(prefix.clone(), process.clone());
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
        this.process.removeKey(bondnumber);
    	return prefix.removeKey(bondnumber);
    }

	@Override
	public int[] getKeys() {
		return prefix.getKeys().stream().mapToInt(i->i).toArray();
	}

	@Override
	public Element getXML() {
		List<Element> prefixes=new ArrayList<Element>();
		prefix.getXML(counter, prefixes);
		Element prefix=new Element("prefix");
		prefixes.forEach((Element x) ->prefix.appendChild(x));
		prefix.appendChild(process.getXML());
		prefix.addAttribute(new Attribute("counter", Integer.toString(counter)));
		return prefix;
	}
}
