package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.SimpleTransition;
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
public class Prefix extends Process {
	
	List<Action> pastactions;
	List<Action> freshactions;
	WeakAction weakAction;
	Process process;
	GraphChecks gc;

	public Prefix(List<Action> actions, Process process, WeakAction weakAction) throws CCBException{
		this.setActions(actions);
		this.weakAction=weakAction;
		this.process=process;
		gc=new GraphChecks(this);
	}
	
	public Prefix(List<Action> actions, Process process) throws CCBException{
		this(actions,process,null);
	}
	
	public Prefix(Action action, Process process) throws CCBException{
		List<Action> actions=new ArrayList<Action>();
		actions.add(action);
		this.weakAction=null;
		this.process=process;
		this.setActions(actions);
		gc=new GraphChecks(this);
	}

	public Prefix(StrongAction strongAction, WeakAction weakAction, Process process) throws CCBException {
		List<Action> actions=new ArrayList<Action>();
		actions.add(strongAction);
		this.weakAction=weakAction;
		this.process=process;
		this.setActions(actions);
		gc=new GraphChecks(this);
	}

	public Prefix(Element process, Map<Set<String>, String> gamma,  List<Action> weakactions) throws CCBException {
		pastactions=new ArrayList<Action>();
		freshactions=new ArrayList<Action>();
		for(int i=0;i<process.getChildElements("action").size();i++){
			Element actionel=process.getChildElements("action").get(i);
			Action action;
			if(weakactions.contains(new StrongAction(actionel.getValue())))
				action=new WeakAction(actionel.getValue());
			else
				action=new StrongAction(actionel.getValue());
			if(actionel.getAttribute("key")!=null){
				action.setKey(Integer.parseInt(actionel.getAttribute("key").getValue()));
				pastactions.add(action);
			}else{
				freshactions.add(action);
			}
		}
		if(process.getChildElements("weakaction").size()>0){
			weakAction=new WeakAction(process.getChildElements("weakaction").get(0).getValue());
			if(process.getChildElements("weakaction").get(0).getAttribute("key")!=null){
				weakAction.setKey(Integer.parseInt(process.getChildElements("weakaction").get(0).getAttribute("key").getValue()));
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

	public WeakAction getWeakAction() {
		return weakAction;
	}

	public List<Action> getPastactions() {
		return pastactions;
	}

	public List<Action> getFreshactions() {
		return freshactions;
	}

	private void setActions(List<Action> actions) {
		pastactions=new ArrayList<Action>();
		freshactions=new ArrayList<Action>();
		for(Action action : actions){
			if(action.getKey()==0)
				freshactions.add(action);
			else
				pastactions.add(action);
		}		
	}

	@Override
	public List<Action> getActionsReady() {
		List<Action> actionsReady=new ArrayList<Action>();
		for(Action action : freshactions){
			if(action.getKey()!=0)
				actionsReady.add(action);
		}
		if(actionsReady.size()>0)
			return actionsReady;
		if(weakAction!=null)
			actionsReady.add(weakAction);
		actionsReady.addAll(process.getActionsReady());
		return actionsReady;
	}
	
	public Set<Action> getAllActionsInOne(){
		Set<Action> allactions=new TreeSet<Action>(new SimpleActionComparator());
		allactions.addAll(freshactions);
		allactions.addAll(pastactions);
		return allactions;
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
		StringBuffer prefix=new StringBuffer();
		boolean first=true;
		if(allActionsInOne){
			Set<Action> allactions=getAllActionsInOne();
			for(Action action : allactions){
				if(!first){
					prefix.append(",");
				}else{
					first=false;
				}
				prefix.append(action.getActionName());
			}
		}else{
			for(Action action : freshactions){
				if(!first){
					prefix.append(",");
				}else{
					first=false;
				}
				if(action==actionperformed1 || action==actionperformed2){
					prefix.append("_");
				}
				if(action==triggeredAction){
					prefix.append("#");
				}
				prefix.append(action.toString());
			}
			for(Action action : pastactions){
				if(!first){
					prefix.append(",");
				}else{
					first=false;
				}
				if(action==actionperformed1 || action==actionperformed2){
					prefix.append("_");
				}
				if(action==triggeredAction){
					prefix.append("#");
				}
				prefix.append(action.toString());
			}
		}
		if(weakAction!=null){
			prefix.append(";");
			if(weakAction==actionperformed1 || weakAction==actionperformed2){
				prefix.append("_");
			}
			if(weakAction==triggeredAction){
				prefix.append("#");
			}
			prefix.append(weakAction.toString());
		}
		String value="";
		if(counter>-1)
			value="P"+counter+"=";
		return value+"("+prefix+")."+process.toString(actionperformed1, actionperformed2, triggeredAction);
	}
	
	@Override
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess) throws CCBException{
		List<Transition> resultingTransitions=new ArrayList<Transition>();
		for(Action action : freshactions){
			SimpleTransition transition = new SimpleTransition(this, action, false);
			resultingTransitions.add(transition);
		}
		if(configuration.allowSpontaneousBreaks){
    		for(Action action : pastactions){
    			SimpleTransition transition = new SimpleTransition(this, action, true);
    			resultingTransitions.add(transition);
    		}
		}
		if(freshactions.size()==0 && weakAction!=null){
			if(!(process instanceof Prefix) || ((Prefix)process).getPastactions().size()==0)
			for(Action action : pastactions){
				SimpleTransition triggeredTransition=new SimpleTransition(this, action, true);
				resultingTransitions.add(new SimpleTransition(this, weakAction, false, triggeredTransition));
			}
		}
		if(freshactions.size()==0){
			resultingTransitions.addAll(process.inferTransitions(synchronize, configuration, rootProcess));
		}
		return resultingTransitions;
	}

	@Override
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
		if(transition.getSubprocesstransitioning()==process){
			process.executeTransition(transition, newkey, rootProcess);
			return;
		}
		if(transition instanceof SimpleTransition){
			if(transition.getKeybroken()==0){
				Iterator<Action> itfresh=freshactions.iterator();
				while(itfresh.hasNext()){
					Action action=itfresh.next();
					if(action.equals(transition.getActionperformed())){
						action.setKey(newkey);
						itfresh.remove();
						pastactions.add(action);
						return;
					}
				}
				if(weakAction!=null && weakAction.equals(transition.getActionperformed())){
					weakAction.setKey(newkey);
				}
			}else{
				Iterator<Action> itpast=pastactions.iterator();
				while(itpast.hasNext()){
					Action action=itpast.next();
					if(action.equals(transition.getActionperformed())){
						action.setKey(0);
						itpast.remove();
						freshactions.add(action);
						return;
					}
				}
				if(weakAction!=null && weakAction.equals(transition.getActionperformed())){
					weakAction.setKey(0);
				}
			}
			if(transition.getTriggeredTransition()!=null){
				Transition secondTriggered=rootProcess.findSecondTriggeredTransition(transition.getTriggeredTransition());
				if(secondTriggered!=null){
					rootProcess.executeTransition(secondTriggered,0,rootProcess);
					this.executeTransition(transition.getTriggeredTransition(),0,rootProcess);
					transition.setTriggeredTransition(null);
				}
			}
		}else{
			throw new CCBException("Only SimpleTransitions can be execute on Prefix");
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
		if(transition.getKeybroken()==0){
			throw new CCBException("triggered transitions must always be past transitions");
		}else{
			for(Action action : pastactions){
				if(action.getKey()==transition.getKeybroken() && this!=transition.getSubprocesstransitioning()){
					return new SimpleTransition(this,action,true);
				}
			}
		}
		return null;
	}


	@Override
	public void accept(CCBVisitor visitor) {
		visitor.visit(this);
	}

    @Override
    public Process clone(){
        List<Action> actions = new ArrayList<Action>();
        for(Action action : pastactions){
            actions.add(action.clone());
        }
        for(Action action : freshactions){
            actions.add(action.clone());
        }
        try {
			return new Prefix(actions, process.clone(), weakAction==null ? null : weakAction.clone());
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
        for(Action action : pastactions){
            if(action.getKey()==bondnumber){
                action.setKey(0);
                pastactions.remove(action);
                freshactions.add(action);
                this.process.removeKey(bondnumber);
                return true;
            }
        }        
        return false;
    }

	@Override
	public int[] getKeys() {
		int[] result=new int[pastactions.size()];
		for(int i=0;i<pastactions.size();i++){
			result[i]=pastactions.get(i).getKey();
		}
		return result;
	}

	@Override
	public Element getXML() {
		Element prefix=new Element("prefix");
		for(Action a : this.freshactions){
			Element action=new Element("action");
			action.appendChild(a.getActionName());
			prefix.appendChild(action);
		}
		for(Action a : this.pastactions){
			Element action=new Element("action");
			action.appendChild(a.getActionName());
			action.addAttribute(new Attribute("key",Integer.toString(a.getKey())));
			prefix.appendChild(action);
		}
		if(weakAction!=null){
			Element action=new Element("weakaction");
			action.appendChild(weakAction.getActionName());
			if(weakAction.getKey()!=0)
				action.addAttribute(new Attribute("key",Integer.toString(weakAction.getKey())));
			prefix.appendChild(action);
		}
		prefix.appendChild(process.getXML());
		prefix.addAttribute(new Attribute("counter", Integer.toString(counter)));
		return prefix;
	}
}
