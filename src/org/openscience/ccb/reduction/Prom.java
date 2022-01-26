package org.openscience.ccb.reduction;

import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;

public class Prom implements CCBVisitor {
	
	public void visit(Process process){
		if(process instanceof PrefixProcess){
			visitPrefix(((PrefixProcess) process).getPrefix());
		}
	}
	
	public void visitPrefix(Prefix prefix) {
		if(prefix.getWeakAction()!=null && prefix.getWeakAction().getKey()>0 && prefix.getFreshactions().size()>0){
			prefix.getFreshactions().iterator().next().setKey(prefix.getWeakAction().getKey());
			prefix.getWeakAction().setKey(0);
			prefix.getPastactions().add(prefix.getFreshactions().iterator().next());
			prefix.getFreshactions().remove(prefix.getFreshactions().iterator().next());
		}
	}
}
