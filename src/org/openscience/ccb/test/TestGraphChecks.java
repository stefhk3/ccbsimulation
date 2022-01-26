package org.openscience.ccb.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.PastSemicolonAction;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.process.Nil;
import org.openscience.ccb.process.Parallel;
import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.process.Restriction;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

public class TestGraphChecks extends TestCase {
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
        GraphChecks graphChecks = new GraphChecks(p1);
        Assert.assertEquals(3, graphChecks.getGraph().vertexSet().size());
        Assert.assertEquals(0, graphChecks.getGraph().edgeSet().size());
        Assert.assertFalse(graphChecks.areConnected(((Parallel)((Restriction)p1).getProcess()).getLeft(), ((Parallel)((Restriction)p1).getProcess()).getRight()));
        List<Transition> transitions=p1.inferTransitions(synchronize, ccbconfiguration, p1);
        p1.executeTransition(transitions.get(0), 1, p1);
        graphChecks = new GraphChecks(p1);
        Assert.assertEquals(3, graphChecks.getGraph().vertexSet().size());
        Assert.assertEquals(1, graphChecks.getGraph().edgeSet().size());
        Assert.assertTrue(graphChecks.areConnected(((Parallel)((Restriction)p1).getProcess()).getLeft(), ((Parallel)((Parallel)((Restriction)p1).getProcess()).getRight()).getLeft()));
        Assert.assertFalse(graphChecks.areConnected(((Parallel)((Restriction)p1).getProcess()).getLeft(), ((Parallel)((Parallel)((Restriction)p1).getProcess()).getRight()).getRight()));
    }
    
    public void testExample2() throws CCBException{
	    String input = "(((c1[1],c2[2],c3[3],c4[4];p).0 | (h1[1];p).0 | (h2[2];p).0 | (o1[3],o2[4],n).0) \\ {c1,c2,c3,c4,h1,h2,o1,o2,np,c1h1,c2h2} | ((h3[5];p).0 | (h4[6];p).0 | (o3[5],o4[6],n).0) \\ {h3,h4,o3,o4,np} | ((h5[7];p).0 | (h6[8];p).0 | (o5[7],o6[8],n).0) \\ {h5,h6,o5,o6,np} | ((h7[9];p).0 | (h8[10];p).0 | (o7[9],o8[10],n).0) \\ {h7,h8,o7,o8,np} ) \\ {n,p}";
	    String weakactions = "n,p";
	    StringTokenizer st=new StringTokenizer(weakactions,",");
	    List<Action> weakActionsList=new  ArrayList<Action>();
	    while(st.hasMoreTokens()){
	    	weakActionsList.add(new WeakAction(st.nextToken()));
	    }
	    CCBParser ccbparser=new CCBParser();
	    Process p1=ccbparser.parseProcess(input, weakActionsList,  null, null);
	    input = "(((c1[1],c2[2],c3[3],c4[4];p).0 | (h1[1];p).0 | (h2[2];p).0 | (o1[3],o2[4],n).0) \\ {c1,c2,c3,c4,h1,h2,o1,o2,np,c1h1,c2h2} | ((h3[124];p).0 | (h4[6];p).0 | (o3[143],o4[6],n).0) \\ {h3,h4,o3,o4,np} | ((h5[143];p).0 | (h6[8];p).0 | (o5[124],o6[8],n).0) \\ {h5,h6,o5,o6,np} | ((h7[9];p).0 | (h8[10];p).0 | (o7[9],o8[10],n).0) \\ {h7,h8,o7,o8,np} ) \\ {n,p}";
	    Process p2=ccbparser.parseProcess(input, weakActionsList,  null, null);
	    GraphChecks gc=new GraphChecks(p1);
	    Assert.assertTrue(gc.isIsomorph(p2));
    }

    public void testExample3() throws CCBException{
	    String input = "((c1[1],c2[2],c4[4],c3[100];p).0 | (h1[1];p).0 | (h2[2];p).0 | (n,o1[108],o2[4]).0) \\ {c1,c2,c3,c4,h1,h2,o1,o2,np,c1h1,c2h2} | ((h3[5];p).0 | (h4[6];p).0 | (o3[5],o4[6],n[100]).0) \\ {h3,h4,o3,o4,np} | ((h5[108];p).0 | (h6[8];p).0 | (n,o5,o6[8]).0) \\ {h5,h6,o5,o6,np} | ((h7[9];p).0 | (h8[10];p).0 | (o7[9],o8[10],n[105]).0) \\ {h7,h8,o7,o8,np}";
	    String weakactions = "n,p";
	    StringTokenizer st=new StringTokenizer(weakactions,",");
	    List<Action> weakActionsList=new  ArrayList<Action>();
	    while(st.hasMoreTokens()){
	    	weakActionsList.add(new WeakAction(st.nextToken()));
	    }
	    CCBParser ccbparser=new CCBParser();
	    Process p1=ccbparser.parseProcess(input, weakActionsList,  null, null);
	    input = "((c1[1],c2[2],c4[4],c3[100];p).0 | (h1[1];p).0 | (h2[2];p).0 | (n,o1[108],o2[4]).0) \\ {c1,c2,c3,c4,h1,h2,o1,o2,np,c1h1,c2h2} | ((h3[5];p).0 | (h4[6];p).0 | (o3[5],o4[6],n[100]).0) \\ {h3,h4,o3,o4,np} | ((h5[105];p).0 | (h6[8];p).0 | (n,o5,o6[8]).0) \\ {h5,h6,o5,o6,np} | ((h7[108];p).0 | (h8[10];p).0 | (o7[9],o8[10],n[105]).0) \\ {h7,h8,o7,o8,np}";
	    Process p2=ccbparser.parseProcess(input, weakActionsList,  null, null);
	    GraphChecks gc=new GraphChecks(p1);
	    Assert.assertTrue(gc.isIsomorph(p2));
    }
}
