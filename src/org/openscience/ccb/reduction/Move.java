package org.openscience.ccb.reduction;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;

public class Move implements CCBVisitor {
	
	public void visit(Process process){
		if(process instanceof PrefixProcess){
			visitPrefix(((PrefixProcess) process).getPrefix());
		}
	}
	
	public void visitPrefix(Prefix prefix) {
		Action weakAction=null;
		for(Action action : prefix.getPastactions()){
			if(action instanceof WeakAction){
				weakAction=action;
				break;
			}
		}
		Action strongAction=null;
		for(Action action : prefix.getFreshactions()){
			if(action instanceof StrongAction){
				strongAction=action;
				break;
			}
		}			
		if(strongAction!=null && weakAction!=null){
			strongAction.setKey(weakAction.getKey());
			weakAction.setKey(0);
			prefix.getPastactions().add(strongAction);
			prefix.getPastactions().remove(weakAction);
			prefix.getFreshactions().remove(strongAction);
			prefix.getFreshactions().add(weakAction);
		}
		if(prefix.getNextPrefix()!=null)
			visitPrefix(prefix.getNextPrefix());
	}
}
