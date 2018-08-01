package org.openscience.ccb.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SpecialCasesTest extends TestCase{
	
    CCBConfiguration ccbconfiguration;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ccbconfiguration_default.properties");
        Properties ccbprops = new Properties();
        ccbprops.load(inputStream);
        ccbconfiguration=new CCBConfiguration(ccbprops);
    }

	//There should be no transition possible in this case
	public void testSpecial1() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"a","a","c","b","b","d"});
		String input="((a[12];b).0 | (a).0 | (b[12]).0) \\ {a,b}";
		Process p1=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals("((a[12];b).0 | (a).0 | (b[12]).0) \\ {a,b}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(0, transitions.size());
	}
	
	public void testSpecial2() throws CCBException{
		Synchronize synchronize=new Synchronize(new String[]{"c1","h1","c1h1","c1","h2","c1h2","c1","h3","c1h3","c1","h4","c1h4","c1","h5","c1h5","c1","h6","c1h6","c2","h1","c2h1","c2","h2","c2h2","c2","h3","c2h3","c2","h4","c2h4","c2","h5","c2h5","c2","h6","c2h6","c3","h1","c3h1","c3","h2","c3h2","c3","h3","c3h3","c3","h4","c3h4","c3","h5","c3h5","c3","h6","c3h6","c4","h1","c4h1","c4","h2","c4h2","c4","h3","c4h3","c4","h4","c4h4","c4","h5","c4h5","c4","h6","c4h6","o1","h1","o1h1","o1","h2","o1h2","o1","h3","o1h3","o1","h4","o1h4","o1","h5","o1h5","o1","h6","o1h6","o2","h1","o2h1","o2","h2","o2h2","o2","h3","o2h3","o2","h4","o2h4","o2","h5","o2h5","o2","h6","o2h6","o3","h1","o3h1","o3","h2","o3h2","o3","h3","o3h3","o3","h4","o3h4","o3","h5","o3h5","o3","h6","o3h6","o4","h1","o4h1","o4","h2","o4h2","o4","h3","o4h3","o4","h4","o4h4","o4","h5","o4h5","o4","h6","o4h6","o5","h1","o5h1","o5","h2","o5h2","o5","h3","o5h3","o5","h4","o5h4","o5","h5","o5h5","o5","h6","o5h6","o6","h1","o6h1","o6","h2","o6h2","o6","h3","o6h3","o6","h4","o6h4","o6","h5","o6h5","o6","h6","o6h6","c1","n","c1n","c2","n","c2n","c3","n","c3n","c4","n","c4n","h1","n","h1n","h2","n","h2n","h3","n","h3n","h4","n","h4n","h5","n","h5n","h6","n","h6n","n","p","np","c1","o1","c1o1","c1","o2","c1o2","c1","o3","c1o3","c2","o1","c2o1","c2","o2","c2o2","c2","o3","c1o3","c3","o1","c3o1","c3","o2","c3o2","c3","o3","c3o3","c4","o1","c4o1","c4","o2","c4o2","c4","o3","c4o3"});
		String input="((c1[1],c2[2],_c3[3],c4[10];p).0 | (h1[1];p).0 | (h2[2];p).0 | (n,o2,_o1[3]).0 | (h3[11];p).0 | (h4[6];p).0 | (n,o4[6],o3[10]).0 | (h5[7];p).0 | (h6[8];p).0 | (o5[7],o6[8],n[11]).0) \\ {c1,c2,c3,c4,h1,h2,h3,h4,h5,h6,o1,o2,o3,o4,o5,o6,n,p,c1h1,c2h2}";
		List<Action> weakActions=new ArrayList<Action>();
		weakActions.add(new WeakAction("n"));
		weakActions.add(new WeakAction("p"));	
		Process p1=new CCBParser().parseProcess(input,weakActions,null,null);
		Assert.assertEquals("((_c3[3],c1[1],c2[2],c4[10];p).0 | (h1[1];p).0 | (h2[2];p).0 | (n,o2,_o1[3]).0 | (h3[11];p).0 | (h4[6];p).0 | (n,o3[10],o4[6]).0 | (h5[7];p).0 | (h6[8];p).0 | (n[11],o5[7],o6[8]).0) \\ {c1,c2,c3,c4,h1,h2,h3,h4,h5,h6,o1,o2,o3,o4,o5,o6,n,p,c1h1,c2h2}",  p1.toString());
		List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
		Assert.assertEquals(12, transitions.size());
		for(int i=0;i<12;i++)
			p1.executeTransition(transitions.get(i), 1, p1);
	}

	
	public void testWeakActions() throws CCBException{
		String input="(o1,o2,n).0";
		List<Action> weakActions = new ArrayList<Action>();
		weakActions.add(new WeakAction("n"));
		Process process=new CCBParser().parseProcess(input, weakActions,null,null);
		Assert.assertEquals("(n,o1,o2).0",process.toString());
		Assert.assertEquals(3,((Prefix)process).getFreshactions().size());
		Iterator<Action> it=((Prefix)process).getFreshactions().iterator();
		Assert.assertTrue(it.next() instanceof WeakAction);
	}
	
	public void testExecutionEmpty() throws CCBException{
        String input="((n,o1[3],o2[4]).0 | (h4[3];p).0) \\ {o1,o2,h4,n,p}";
        List<Action> weakActions = new ArrayList<Action>();
        weakActions.add(new WeakAction("n"));
        weakActions.add(new WeakAction("p"));
        Process process=new CCBParser().parseProcess(input, weakActions, null, null);
        Assert.assertEquals(input, process.toString());
        Synchronize synchronize=new Synchronize(new String[]{"n","p","np","o1","h4","o1h4"});
        List<Transition> transitions = process.inferTransitions(synchronize, ccbconfiguration, process);
        Assert.assertEquals(1,transitions.size());
        process.executeTransition(transitions.get(0), 1, process);
        transitions = process.inferTransitions(synchronize, ccbconfiguration, process);
        Assert.assertEquals(1,transitions.size());
        
    }

	public void testTwoGroups() throws CCBException{
		String input="(d[101];d).(e).0";
		Process process=new CCBParser().parseProcess(input,null,null);
		List<Transition> trans = process.inferTransitions(new Synchronize(new String[0]), ccbconfiguration, process);
		Assert.assertEquals(2, trans.size());		
	}
	
	public void testTransition1() throws CCBException {
		Synchronize synchronize=new Synchronize(new String[]{"c1","h1","c1h1","c1","h2","c1h2","c1","h3","c1h3","c1","h4","c1h4","c1","h5","c1h5","c1","h6","c1h6","c2","h1","c2h1","c2","h2","c2h2","c2","h3","c2h3","c2","h4","c2h4","c2","h5","c2h5","c2","h6","c2h6","c3","h1","c3h1","c3","h2","c3h2","c3","h3","c3h3","c3","h4","c3h4","c3","h5","c3h5","c3","h6","c3h6","c4","h1","c4h1","c4","h2","c4h2","c4","h3","c4h3","c4","h4","c4h4","c4","h5","c4h5","c4","h6","c4h6","o1","h1","o1h1","o1","h2","o1h2","o1","h3","o1h3","o1","h4","o1h4","o1","h5","o1h5","o1","h6","o1h6","o2","h1","o2h1","o2","h2","o2h2","o2","h3","o2h3","o2","h4","o2h4","o2","h5","o2h5","o2","h6","o2h6","o3","h1","o3h1","o3","h2","o3h2","o3","h3","o3h3","o3","h4","o3h4","o3","h5","o3h5","o3","h6","o3h6","o4","h1","o4h1","o4","h2","o4h2","o4","h3","o4h3","o4","h4","o4h4","o4","h5","o4h5","o4","h6","o4h6","o5","h1","o5h1","o5","h2","o5h2","o5","h3","o5h3","o5","h4","o5h4","o5","h5","o5h5","o5","h6","o5h6","o6","h1","o6h1","o6","h2","o6h2","o6","h3","o6h3","o6","h4","o6h4","o6","h5","o6h5","o6","h6","o6h6","c1","n","c1n","c2","n","c2n","c3","n","c3n","c4","n","c4n","h1","n","h1n","h2","n","h2n","h3","n","h3n","h4","n","h4n","h5","n","h5n","h6","n","h6n","n","p","np","c1","o1","c1o1","c1","o2","c1o2","c1","o3","c1o3","c2","o1","c2o1","c2","o2","c2o2","c2","o3","c1o3","c3","o1","c3o1","c3","o2","c3o2","c3","o3","c3o3","c4","o1","c4o1","c4","o2","c4o2","c4","o3","c4o3"});
		String input="(((c1[1],c2[2],c3[237],c4[10];p).0 | (h1[1];p).0 | (h2[2];p).0 | (n,o2[4],_o1[218]).0) \\ {c1,c2,c3,c4,h1,h2,o1,o2,np,c1h1,c2h2} | ((h3[218];p).0 | (h4[6];p).0 | (n,o4[6],o3).0) \\ {h3,h4,o3,o4,np} | ((h5[7];p).0 | (h6[8];p).0 | (o5[7],o6[8],n[237]).0) \\ {h5,h6,o5,o6,np} | ((h7[9];p).0 | (h8[10];p).0 | (o7[9],o8[10],n).0) \\ {75,h8,o7,o8,np}) \\ {n,p}";
		List<Action> weakActions=new ArrayList<Action>();
		weakActions.add(new WeakAction("n"));
		weakActions.add(new WeakAction("p"));	
		Process p1=new CCBParser().parseProcess(input,weakActions,null,null);
		List<Transition> transitions = p1.inferTransitions(synchronize, ccbconfiguration, p1);
		boolean containstransition=false;
		for(Transition t : transitions) {
			if(t.toString().contains("((#h5[7];_p).0") && t.toString().contains("(_n,o7[9],o8[10]).0")) {
				containstransition=true;
			}
		}
		Assert.assertTrue(containstransition);
	}
}
