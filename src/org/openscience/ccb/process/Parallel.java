package org.openscience.ccb.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.PastSemicolonAction;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Communication;
import org.openscience.ccb.transition.SimpleTransition;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

import nu.xom.Attribute;
import nu.xom.Element;

public class Parallel extends Process {
	
	Process left;
    Process right;
    Connectivity conn=null;
    Distance dist=null;
	GraphChecks gc;

	public Parallel(Process left, Process right, Connectivity conn, Distance dist) throws CCBException{
		this.left=left;
		this.right=right;
		this.conn=conn;
		this.dist=dist;
		gc=new GraphChecks(this);
	}
	
	
	public Parallel(Element process, Map<Set<String>, String> gamma,  List<Action> weakactions) throws CCBException {
		this.left=CCBParser.parseProcess(process.getChildElements().get(0), gamma, weakactions);
		this.right=CCBParser.parseProcess(process.getChildElements().get(1), gamma, weakactions);
		if(process.getAttribute("counter")!=null)
			counter=Integer.parseInt(process.getAttributeValue("counter"));
		gc=new GraphChecks(this);
	}


	@Override
	public List<Action> getActionsReady() {
		List<Action> actionsReady = left.getActionsReady();
		actionsReady.addAll(right.getActionsReady());
		return actionsReady;
	}
	
	@Override
	public String toString(){
		return toString(null, null, null);
	}
	
	@Override
	public List<Transition> inferTransitions(Synchronize synchronize, CCBConfiguration configuration, Process rootProcess) throws CCBException{
		List<Transition> leftTransitions=left.inferTransitions(synchronize, configuration, rootProcess);
		List<Transition> rightTransitions=right.inferTransitions(synchronize, configuration, rootProcess);
		List<Transition> resultingTransitions=new ArrayList<Transition>();
		Iterator<Transition> itleft = leftTransitions.iterator();
		while (itleft.hasNext()) {
			Transition leftTransition=itleft.next();
			Iterator<Transition> itright = rightTransitions.iterator();
			while (itright.hasNext()) {
				//only simple transitions can communicate
				Transition rightTransition=itright.next();
				if(leftTransition instanceof SimpleTransition && rightTransition instanceof SimpleTransition){
					//both transitions must either be forward or reverse
					if((
							((((SimpleTransition)leftTransition).getKeybroken()!=0 && ((SimpleTransition)rightTransition).getKeybroken()!=0 &&
									((SimpleTransition)leftTransition).getKeybroken()==((SimpleTransition)rightTransition).getKeybroken()
							)
							||
							((SimpleTransition)leftTransition).getKeybroken()==0 && ((SimpleTransition)rightTransition).getKeybroken()==0)
							&&
							synchronize.isSychronized(((SimpleTransition)leftTransition).getActionperformed().getActionName(),((SimpleTransition)rightTransition).getActionperformed().getActionName())!=null
						) && (
								//both processes must fulfill the connectivity condition if there is one
								conn==null || (conn.result(leftTransition.getSubprocesstransitioning(),this) && conn.result(leftTransition.getSubprocesstransitioning(),rootProcess))
							) && (
									//distance condition must be fulllfilled if there is one
									dist==null || (dist.result(leftTransition.getSubprocesstransitioning(), rightTransition.getSubprocesstransitioning(), rootProcess))))
							{
						
						if(((SimpleTransition)leftTransition).getActionperformed() instanceof PastSemicolonAction && ((SimpleTransition)rightTransition).getActionperformed() instanceof PastSemicolonAction){
							//For forward transitions we need to find matching transitions to break
							boolean foundmatching=false;
							if(((SimpleTransition)leftTransition).getActionperformed().getKey()==0){
								for(Action leftaction : ((PrefixProcess)((SimpleTransition)leftTransition).getSubprocesstransitioning()).getPastactions()){
									for(Action rightaction : ((PrefixProcess)((SimpleTransition)rightTransition).getSubprocesstransitioning()).getPastactions()){
										if(rightaction.getKey()==leftaction.getKey())
											foundmatching=true;
									}
								}
							}
							if(!foundmatching)
								break;
						}
						Action commAction=null;
						if(((SimpleTransition)rightTransition).getKeybroken()!=0)
							commAction = new StrongAction(synchronize.isSychronized(((SimpleTransition)leftTransition).getActionperformed().getActionName(),((SimpleTransition)rightTransition).getActionperformed().getActionName()),((SimpleTransition)rightTransition).getKeybroken());
						else
							commAction = new StrongAction(synchronize.isSychronized(((SimpleTransition)leftTransition).getActionperformed().getActionName(),((SimpleTransition)rightTransition).getActionperformed().getActionName()));
						Communication newTransition=new Communication((SimpleTransition)leftTransition,  (SimpleTransition)rightTransition, this, commAction, ((SimpleTransition)rightTransition).getKeybroken()!=0);
						if(rightTransition.getTriggeredTransition()==null)
							newTransition.setTriggeredTransition(rightTransition.getTriggeredTransition());
						else if(leftTransition.getTriggeredTransition()!=null)
							newTransition.setTriggeredTransition(leftTransition.getTriggeredTransition());
						resultingTransitions.add(newTransition);
					}
				}
			}
		}
		for(Transition leftTransition : leftTransitions){
			if(!resultingTransitions.contains(leftTransition)){
				resultingTransitions.add(leftTransition);
			}
		}
		for(Transition rightTransition : rightTransitions){
			if(!resultingTransitions.contains(rightTransition)){
				resultingTransitions.add(rightTransition);
			}
		}
		return resultingTransitions;
	}
	
	@Override
	public void executeTransition(Transition transition, int newkey, Process rootProcess) throws CCBException {
		if(transition instanceof Communication){
			Communication comm=(Communication)transition;
			for(int i=0;i<2;i++){
				Transition parttransition;
				if(i==0)
					parttransition=comm.getTransition1();
				else
					parttransition=comm.getTransition2();
				if(left.contains(parttransition.getSubprocesstransitioning()))
					left.executeTransition(parttransition, newkey, rootProcess);
				else
					right.executeTransition(parttransition, newkey, rootProcess);
			}			
		}else{
			if(left.contains(transition.getSubprocesstransitioning()))
				left.executeTransition(transition, newkey, rootProcess);
			else
				right.executeTransition(transition, newkey, rootProcess);
		}
		if(transition.getTriggeredTransition()!=null){
			Transition secondTriggered=rootProcess.findSecondTriggeredTransition(transition.getTriggeredTransition());
			if(secondTriggered!=null){
				this.executeTransition(secondTriggered,0, rootProcess);
				this.executeTransition(transition.getTriggeredTransition(),0, rootProcess);
				transition.setTriggeredTransition(null);
			}
		}
	}
	
	@Override
	public boolean contains(Process subprocesstransitioning) {
		if(left.contains(subprocesstransitioning) || right.contains(subprocesstransitioning))
			return true;
		else
			return false;
	}
	
	@Override
	public Transition findSecondTriggeredTransition(Transition transition)  throws CCBException{
		if(left.findSecondTriggeredTransition(transition)!=null)
			return left.findSecondTriggeredTransition(transition);
		else
			return right.findSecondTriggeredTransition(transition);
	}


	@Override
	public void accept(CCBVisitor visitor) {
		visitor.visit(this);
		left.accept(visitor);
		right.accept(visitor);
	}
	
	@Override
    public Process clone(){
		try{
			return new Parallel(left.clone(),right.clone(),conn,dist);
		} catch (CCBException e) {
			e.printStackTrace();
			return null;
		}
    }


	@Override
	public String toString(Action actionperformed1, Action actionperformed2, Action triggeredAction) {
		String value="";
		if(counter>-1)
			value="P"+counter+"=";
		return value+left.toString(actionperformed1, actionperformed2, triggeredAction)+" | "+right.toString(actionperformed1, actionperformed2, triggeredAction);
	}

    public Process getLeft() {
        return left;
    }


    public Process getRight() {
        return right;
    }


    @Override
    public boolean removeKey(int bondnumber) {
        boolean leftremoved=this.left.removeKey(bondnumber);
        boolean rightremoved=this.right.removeKey(bondnumber);
        return leftremoved || rightremoved;
    }


	@Override
	public int[] getKeys() {
		int[] leftkeys=left.getKeys();
		int[] rightkeys=right.getKeys();
		int[] result = new int[leftkeys.length+rightkeys.length];
		System.arraycopy(leftkeys, 0, result, 0, leftkeys.length);
		System.arraycopy(rightkeys, 0, result, leftkeys.length, rightkeys.length);
		return result;
	}


	@Override
	public Element getXML() {
		Element parallel=new Element("parallel");
		parallel.appendChild(left.getXML());
		parallel.appendChild(right.getXML());
		parallel.addAttribute(new Attribute("counter", Integer.toString(counter)));
		return parallel;
	}
}
