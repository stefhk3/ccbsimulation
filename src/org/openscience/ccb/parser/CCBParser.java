package org.openscience.ccb.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.PastSemicolonAction;
import org.openscience.ccb.action.StrongAction;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.process.Nil;
import org.openscience.ccb.process.Parallel;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.process.Restriction;
import org.openscience.ccb.util.CCBException;

import nu.xom.Element;

public class CCBParser {
	
	public static Process parseProcess(Element process, Map<Set<String>, String> gamma, List<Action> weakactions) throws CCBException{
		if(process.getLocalName().equals("prefix")){
			return new Prefix(process, gamma, weakactions);
		}else if(process.getLocalName().equals("nil")){
			return new Nil(process);
		}else if(process.getLocalName().equals("parallel")){
			return new Parallel(process, gamma, weakactions);
		}else if(process.getLocalName().equals("restriction")){
			return new Restriction(process, gamma, weakactions);
		}else{
			throw new CCBException(process.getLocalName()+" is not a valid process type in CCB xml");
		}
	}
	
	public Process parseProcess(String input, List<Action> weakActionsList, Connectivity conn, Distance dist) throws CCBException{
		//We look at the different operators, starting from the ouside
		String patternRestriction="\\s\\\\\\s\\{[A-Za-z_0-9,]*\\}$";
		Pattern word = Pattern.compile(patternRestriction);
		Matcher match = word.matcher(input);
		if(stringContainsPipeOnFirstLevel(input)>-1){
		    //Outer operator is a pipe
			int position = stringContainsPipeOnFirstLevel(input);
			return new Parallel(parseProcess(input.substring(0,position).trim(), weakActionsList, conn, dist), parseProcess(input.substring(position+1).trim(), weakActionsList, conn, dist), conn, dist);
		}else if(match.find()){
		    //Outer operator is a restriction
		    if(input.indexOf("(")!=0 || input.lastIndexOf(")")!=match.start()-1)
		    	throw new CCBException("Restriction format wrong");
		    Process subprocess=this.parseProcess(input.substring(1,match.start()-1).trim(), weakActionsList, conn, dist);
		    String restrictionsstring=input.substring(input.lastIndexOf(")")+5,input.length()-1);
		    StringTokenizer st=new StringTokenizer(restrictionsstring,",");
		    List<Action> restrictions=new ArrayList<Action>();
		    while(st.hasMoreTokens()){
		    	StrongAction restriction=new StrongAction(st.nextToken());
		    	restrictions.add(restriction);
		    }
		    return new Restriction(restrictions, subprocess);
		}else if(input.startsWith("(")){
		    //Outer operator is prefix
			int end=input.indexOf(")");
			PastSemicolonAction weakAction = null;
			Process process = parseProcess(input.substring(end+2), weakActionsList, conn, dist);
			if(input.substring(1,end-1).indexOf(";")>-1){
				weakAction=new PastSemicolonAction(input.substring(input.indexOf(";")+1,end));
				end=input.substring(0,end-1).indexOf(";");
			}
			List<Action> actions=new ArrayList<Action>();
			String actionstring=input.substring(1,end);
			StringTokenizer st=new StringTokenizer(actionstring,",");
			while(st.hasMoreTokens()){
				Action action=null;
				String actionStringLocal=st.nextToken();
				Integer key=null;
				if(actionStringLocal.indexOf("[")>-1){
					key = Integer.parseInt(actionStringLocal.substring(actionStringLocal.indexOf("[")+1,actionStringLocal.indexOf("]")));
					actionStringLocal = actionStringLocal.substring(0,actionStringLocal.indexOf("["));
				}
				if(weakActionsList!=null && weakActionsList.contains(new WeakAction(actionStringLocal))){
					action=new WeakAction(actionStringLocal);
				}else{
					action=new StrongAction(actionStringLocal);
				}
				if(key!=null)
					action.setKey(key);
				actions.add(action);
			}
			if(weakAction!=null){
				return new Prefix(actions, process, weakAction);
			}else{
				return new Prefix(actions, process);
			}
		}else if(input.equals("0")){
		    //Outer process is a nil process
			return new Nil();
		}else{
			throw new CCBException(input+" doesn't look like a valid process description");
		}
	}

	private int stringContainsPipeOnFirstLevel(String input) {
		int level=0;
		for(int i=0;i<input.length();i++){
			if(input.charAt(i)=='(')
				level++;
			else if(input.charAt(i)==')')
				level--;
			else if(input.charAt(i)=='|' && level==0)
				return i;
		}
		return -1;
	}

	public Process parseProcess(String input, Connectivity conn, Distance dist) throws CCBException {
		return parseProcess(input, null, conn, dist);
	}
}
