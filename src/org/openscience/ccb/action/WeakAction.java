package org.openscience.ccb.action;

public class WeakAction extends Action {

	public WeakAction(String actionName) {
		super(actionName);
	}

    public WeakAction(String actionName, int key) {
        this(actionName);
        this.setKey(key);
    }

    @Override
    public WeakAction clone() {
        return new WeakAction(new String(this.getActionName()), this.getKey());
    }
}
