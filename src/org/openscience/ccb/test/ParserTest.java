package org.openscience.ccb.test;

import java.util.ArrayList;
import java.util.List;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.process.PrefixProcess;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.util.CCBException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ParserTest  extends TestCase{
	public void testSimple() throws CCBException{
		String input="((a,b[1];c).0 | 0) \\ {a,b}";
		Process process=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals(input, process.toString());
	}
	
	public void testPaperExamples() throws CCBException{
		String input="((a[1];b).0 | (a[1]).0 | (b).0) \\ {a,b}";
		Process process=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals(input, process.toString());
		input="((a;b[2]).0 | (a;b[2]).0 | (e).0) \\ {a,b,e}";
		process=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals(input, process.toString());
		input="((a[1];b).0 | (e[2];b).0 | (a[1],e[2]).0) \\ {a,b,e}";
		process=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals(input, process.toString());
		input="((a[1]).0 | (b).0 | (b[1]).0) \\ {a,b}";
		process=new CCBParser().parseProcess(input,null,null);
		Assert.assertEquals(input, process.toString());		
	}
}
