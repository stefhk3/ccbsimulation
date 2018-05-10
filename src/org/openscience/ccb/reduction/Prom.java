package org.openscience.ccb.reduction;

import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;

public class Prom implements CCBVisitor {
	
	public void visit(Process process){
		if(process instanceof Prefix){
			Prefix prefix=(Prefix)process;
			if(prefix.getWeakAction()!=null && prefix.getWeakAction().getKey()>0 && prefix.getFreshactions().size()>0){
				prefix.getFreshactions().get(0).setKey(prefix.getWeakAction().getKey());
				prefix.getWeakAction().setKey(0);
				prefix.getPastactions().add(prefix.getFreshactions().get(0));
				prefix.getFreshactions().remove(0);
			}
		}
	}
}
