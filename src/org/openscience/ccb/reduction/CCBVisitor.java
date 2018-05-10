package org.openscience.ccb.reduction;

import org.openscience.ccb.process.Process;

public interface CCBVisitor {
	void visit(Process process);
}
