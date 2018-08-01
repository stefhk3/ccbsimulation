package org.openscience.ccb.process;

import java.util.Comparator;

import org.openscience.ccb.action.Action;

/**
 * Sorts actions by actions names. Used to ensure canonical representation of prefixes.
 *
 */
public class SimpleActionComparator implements Comparator<Action> {

	@Override
	public int compare(Action o1, Action o2) {
		return o1.getActionName().compareTo(o2.getActionName());
	}

}
