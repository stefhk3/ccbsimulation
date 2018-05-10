package org.openscience.ccb.test;

import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.process.Parallel;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class PredicateTest  extends TestCase{
	
	public void testConnectivity() throws CCBException{
		String input="(a[1],b).0 | (a[1]).0 | (b).0";
		Process process=new CCBParser().parseProcess(input,null,null);
		Connectivity conn=new Connectivity(1);
		Assert.assertTrue(conn.result(((Parallel)process).getLeft(),process));
		input="(a[1],b[2]).0 | (a[1]).0 | (b[2]).0";
		process=new CCBParser().parseProcess(input,null,null);
		Assert.assertFalse(conn.result(((Parallel)process).getLeft(),process));
	}
	
	public void testDistance() throws CCBException{
		String input="(a[1]).0 | (a[1],b[2]).0 | (b[2]).0";
		Process process=new CCBParser().parseProcess(input,null,null);
		Distance conn=new Distance(1,5,false);
		Assert.assertTrue(conn.result(((Parallel)process).getLeft(),((Parallel)((Parallel)process).getRight()).getLeft(),process));
		Assert.assertFalse(conn.result(((Parallel)process).getLeft(),((Parallel)((Parallel)process).getRight()).getRight(),process));
	}
}
