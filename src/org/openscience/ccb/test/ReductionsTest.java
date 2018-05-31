package org.openscience.ccb.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.PastSemicolonAction;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.process.Nil;
import org.openscience.ccb.process.Parallel;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.process.Restriction;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.reduction.Move;
import org.openscience.ccb.reduction.Prom;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ReductionsTest extends TestCase{

    CCBConfiguration ccbconfiguration;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ccbconfiguration_default.properties");
        Properties ccbprops = new Properties();
        ccbprops.load(inputStream);
        ccbconfiguration=new CCBConfiguration(ccbprops);
        ccbconfiguration.allowSpontaneousBreaks=true;
    }

	public void testPromote() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		Process p1=new Restriction(restrictions, new Parallel(new Prefix(new StrongAction("a"), new PastSemicolonAction("b"),new Nil()), new Parallel(new Prefix(new StrongAction("a"),new Nil()), new Prefix(new StrongAction("b"),new Nil()),null,null),null,null));
		Assert.assertEquals("((a;b).0 | (a).0 | (b).0) \\ {a,b}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(1, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0), 1, p1);
		Assert.assertEquals("((a[1];b).0 | (a[1]).0 | (b).0) \\ {a,b}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(1,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("d",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(1), 2, p1);
		Assert.assertEquals("((a;b[2]).0 | (a).0 | (b[2]).0) \\ {a,b}",  p1.toString());
		CCBVisitor prom = new Prom();
		p1.accept(prom);
		Assert.assertEquals("((a[2];b).0 | (a).0 | (b[2]).0) \\ {a,b}",  p1.toString());
	}
	
	public void testMove() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		List<Action> actions = new ArrayList<Action>();
		actions.add(new StrongAction("a"));
		actions.add(new WeakAction("b"));
		Process p1=new Restriction(restrictions, new Parallel(new Prefix(actions,new Nil()), new Parallel(new Prefix(new StrongAction("a"),new Nil()), new Prefix(new StrongAction("b"),new Nil()),null,null),null,null));
		Assert.assertEquals("((a,b).0 | (a).0 | (b).0) \\ {a,b}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("d",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(1), 1, p1);
		Assert.assertEquals("((a,b[1]).0 | (a).0 | (b[1]).0) \\ {a,b}",  p1.toString());
		CCBVisitor prom = new Move();
		p1.accept(prom);
		Assert.assertEquals("((b,a[1]).0 | (a).0 | (b[1]).0) \\ {a,b}",  p1.toString());
	}
}
