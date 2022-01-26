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
import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.process.Restriction;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PaperExamples extends TestCase{
    
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

	
	public void testExample1() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		Process p1=new Restriction(restrictions, new Parallel(new PrefixProcess(new StrongAction("a"), new PastSemicolonAction("b"),new Nil()), new Parallel(new PrefixProcess(new StrongAction("a"),new Nil()), new PrefixProcess(new StrongAction("b"),new Nil()),null,null),null,null));
		Assert.assertEquals("((a;b).0 | (a).0 | (b).0) \\ {a,b}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(1, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0), 1, p1);
		Assert.assertEquals("((a[1];b).0 | (a[1]).0 | (b).0) \\ {a,b}",  p1.toString());
        transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
        Assert.assertEquals(2, transitions.size());
        Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
        Assert.assertEquals("d",transitions.get(1).getActionperformed().getActionName());
        p1.executeTransition(transitions.get(1), 2, p1);
        Assert.assertEquals("((a;b[2]).0 | (a).0 | (b[2]).0) \\ {a,b}",  p1.toString());
	}
	
	public void testExample2() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		restrictions.add(new StrongAction("e"));
		Process p1=new Restriction(restrictions, new Parallel(new Parallel(new PrefixProcess(new StrongAction("a",1), new PastSemicolonAction("b"),new Nil()), new PrefixProcess(new StrongAction("a",1), new WeakAction("b"),new Nil()),null,null), new PrefixProcess(new StrongAction("e"),new Nil()),null,null));
		Assert.assertEquals("((a[1];b).0 | (a[1];b).0 | (e).0) \\ {a,b,e}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(1,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("d",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0),0, p1);
		Assert.assertEquals("((a;b).0 | (a;b).0 | (e).0) \\ {a,b,e}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(1, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0),1, p1);
		Assert.assertEquals("((a[1];b).0 | (a[1];b).0 | (e).0) \\ {a,b,e}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(1,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("d",transitions.get(1).getActionperformed().getActionName());
		Assert.assertEquals("a",transitions.get(1).getTriggeredTransition().getActionperformed().getActionName());
		p1.executeTransition(transitions.get(1),2, p1);
		Assert.assertEquals("((a;b[2]).0 | (a;b[2]).0 | (e).0) \\ {a,b,e}",  p1.toString());
	}
	
	public void testExample3() throws CCBException{
		List<Action> actionsp3 = new ArrayList<Action>();
		actionsp3.add(new StrongAction("a",1));
		actionsp3.add(new StrongAction("e",2));
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d","e","e","f"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		restrictions.add(new StrongAction("e"));
		Process p1=new Restriction(restrictions, new Parallel(new Parallel(new PrefixProcess(new StrongAction("a",1), new PastSemicolonAction("b"),new Nil()), new PrefixProcess(new StrongAction("e",2), new PastSemicolonAction("b"),new Nil()),null,null), new PrefixProcess(new Prefix(actionsp3),new Nil()),null,null));
		Assert.assertEquals("((a[1];b).0 | (e[2];b).0 | (a[1],e[2]).0) \\ {a,b,e}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(1,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(2,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("f",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0),0, p1);
		Assert.assertEquals("((a;b).0 | (e[2];b).0 | (a,e[2]).0) \\ {a,b,e}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(2,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("f",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(1),0, p1);
		Assert.assertEquals("((a;b).0 | (e;b).0 | (a,e).0) \\ {a,b,e}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("f",transitions.get(1).getActionperformed().getActionName());
		
	}

	public void testConflict() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","b","c"});
		List<Action> restrictions=new ArrayList<Action>();
		restrictions.add(new StrongAction("a"));
		restrictions.add(new StrongAction("b"));
		Process p1=new Restriction(restrictions, new Parallel(new PrefixProcess(new StrongAction("a"),new Nil()), new Parallel(new PrefixProcess(new StrongAction("b"),new Nil()), new PrefixProcess(new StrongAction("b"),new Nil()),null,null),null,null));
		Assert.assertEquals("((a).0 | (b).0 | (b).0) \\ {a,b}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(0), 1, p1);
		Assert.assertEquals("((a[1]).0 | (b[1]).0 | (b).0) \\ {a,b}",  p1.toString());
		p1=new Restriction(restrictions, new Parallel(new PrefixProcess(new StrongAction("a"),new Nil()), new Parallel(new PrefixProcess(new StrongAction("b"),new Nil()), new PrefixProcess(new StrongAction("b"),new Nil()),null,null),null,null));
		Assert.assertEquals("((a).0 | (b).0 | (b).0) \\ {a,b}",  p1.toString());
		transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(2, transitions.size());
		Assert.assertEquals(0,transitions.get(0).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(0).getActionperformed().getActionName());
		Assert.assertEquals(0,transitions.get(1).getActionperformed().getKey());
		Assert.assertEquals("c",transitions.get(1).getActionperformed().getActionName());
		p1.executeTransition(transitions.get(1), 1, p1);
		Assert.assertEquals("((a[1]).0 | (b).0 | (b[1]).0) \\ {a,b}",  p1.toString());
	}
}
