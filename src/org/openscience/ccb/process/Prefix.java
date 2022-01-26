package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.SimpleTransition;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import nu.xom.Attribute;
import nu.xom.Element;

public class Prefix {
	List<Action> pastactions;
	List<Action> freshactions;
	WeakAction weakAction;
	//for parallel prefixes, this is the right prefix(es)
	Prefix nextPrefix=null;

	public Prefix(List<Action> actions, WeakAction weakAction2) {
		this(actions);
		this.weakAction=weakAction2;
	}
	public Prefix(List<Action> actions) {
		setActions(actions);
	}
	public Prefix(Prefix parsePrefix, Prefix parsePrefix2) {
		this.nextPrefix=parsePrefix2;
		this.pastactions=parsePrefix.pastactions;
		this.freshactions=parsePrefix.freshactions;
		this.weakAction=parsePrefix.getWeakAction();
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
	public List<Action> getPastactions() {
		List<Action> actionspast=new ArrayList<Action>();
		actionspast.addAll(pastactions); 
		if(nextPrefix!=null)
			actionspast.addAll(nextPrefix.getPastactions());
		return actionspast;
	}
	public List<Action> getFreshactions() {
		List<Action> actionsfreshy=new ArrayList<Action>();
		actionsfreshy.addAll(freshactions);
		if(nextPrefix!=null)
			actionsfreshy.addAll(nextPrefix.getFreshactions());
		return actionsfreshy;
	}
	public WeakAction getWeakAction() {
		return weakAction;
	}
	public Prefix getNextPrefix() {
		return nextPrefix;
	}
	public List<Action> getActionsReady() {
		List<Action> actionsReady=new ArrayList<Action>();
		for(Action action : freshactions){
			if(action.getKey()!=0)
				actionsReady.add(action);
		}
		if(actionsReady.size()>0)
			return actionsReady;
		if(weakAction!=null && weakAction.getKey()==0)
			actionsReady.add(weakAction);
		return actionsReady;
	}
	
	public Set<Action> getAllActionsInOne(){
		Set<Action> allactions=new TreeSet<Action>(new SimpleActionComparator());
		allactions.addAll(freshactions);
		allactions.addAll(pastactions);
		return allactions;
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
		if(nextPrefix!=null) {
			prefix.append(nextPrefix.toString(actionperformed1, actionperformed2, triggeredAction, allActionsInOne));
		}
		return prefix.toString();
	}
	public boolean removeKey(int bondnumber) {
        for(Action action : pastactions){
            if(action.getKey()==bondnumber){
                action.setKey(0);
                pastactions.remove(action);
                freshactions.add(action);
                return true;
            }
        }
        if(nextPrefix!=null) {
        	nextPrefix.removeKey(bondnumber);
        }
        return false;
	}
	public Transition findSecondTriggeredTransition(Transition transition, Process process) throws CCBException {
		if(transition.getKeybroken()==0){
			throw new CCBException("triggered transitions must always be past transitions");
		}else{
			for(Action action : pastactions){
				if(action.getKey()==transition.getKeybroken() && process!=transition.getSubprocesstransitioning()){
					return new SimpleTransition(process,action,true);
				}
			}
		}
		return null;
	}
    public Prefix clone(){
        List<Action> actions = new ArrayList<Action>();
        for(Action action : pastactions){
            actions.add(action.clone());
        }
        for(Action action : freshactions){
            actions.add(action.clone());
        }
        Prefix firstPrefix=new Prefix(actions, weakAction==null ? null : weakAction.clone());
        if(nextPrefix!=null)
        	return new Prefix(firstPrefix, nextPrefix.clone());
        else
        	return firstPrefix; 
    }
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess, PrefixProcess motherProcess) throws CCBException{
		List<Transition> resultingTransitions=new ArrayList<Transition>();
		for(Action action : freshactions){
			SimpleTransition transition = new SimpleTransition(motherProcess, action, false);
			resultingTransitions.add(transition);
		}
		if(configuration.allowSpontaneousBreaks){
    		for(Action action : pastactions){
    			SimpleTransition transition = new SimpleTransition(motherProcess, action, true);
    			resultingTransitions.add(transition);
    		}
		}
		if(freshactions.size()==0 && weakAction!=null && weakAction.getKey()==0){
			if(!(motherProcess.getProcess() instanceof PrefixProcess) || ((PrefixProcess)motherProcess.getProcess()).getPastactions().size()==0)
			for(Action action : pastactions){
				SimpleTransition triggeredTransition=new SimpleTransition(motherProcess, action, true);
				resultingTransitions.add(new SimpleTransition(motherProcess, weakAction, false, triggeredTransition));
			}
		}
		if(freshactions.size()==0){
			resultingTransitions.addAll(motherProcess.getProcess().inferTransitions(synchronize, configuration, rootProcess));
		}
		if(nextPrefix!=null) {
			resultingTransitions.addAll(nextPrefix.inferTransitions(synchronize, configuration, rootProcess, motherProcess));
		}
		return resultingTransitions;
	}
	public List<Integer> getKeys() {
		List<Integer> result=new ArrayList<Integer>();
		for(int i=0;i<pastactions.size();i++){
			result.add(pastactions.get(i).getKey());
		}
		if(nextPrefix!=null)
			result.addAll(nextPrefix.getKeys());
		return result;
	}
	public void getXML(int counter, List<Element> prefixes) {
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
		prefixes.add(prefix);
		if(nextPrefix!=null)
			nextPrefix.getXML(counter, prefixes);
	}
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
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
			if(nextPrefix!=null)
				nextPrefix.executeTransition(transition, newkey, rootProcess);
		}else{
			throw new CCBException("Only SimpleTransitions can be execute on Prefix");
		}
	}
}
