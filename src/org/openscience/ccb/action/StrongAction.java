package org.openscience.ccb.action;

public class StrongAction extends Action {

	public StrongAction(String actionName) {
		super(actionName);
	}

	public StrongAction(String actionName, int key) {
		super(actionName, key);
	}

    @Override
    public StrongAction clone() {
        return new StrongAction(new String(this.getActionName()), this.getKey());
    }
}
