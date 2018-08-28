package org.openscience.ccb.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Action implements Comparable<Action>{
	
	private String actionName;
	private int key;
	
	public Action(String actionName){
		this.actionName=actionName;
	}
	
	public Action(String actionName, int key){
		this.actionName=actionName;
		this.key=key;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	@Override
	public String toString(){
		if(key!=0)
			return actionName+"["+key+"]";
		else
			return actionName;
	}
	
	@Override
	public boolean equals(Object action){
	    if(this.getKey()!=0 && ((Action)action).getKey()!=0){
	        return this.getActionName().equals(((Action)action).getActionName()) && this.getKey()==((Action)action).getKey();	        
	    }else if(this.getKey()==0 && ((Action)action).getKey()==0){
	        return this.getActionName().equals(((Action)action).getActionName());
	    }else{
	        return false;
	    }
	}
	
	@Override
	public int compareTo(Action other){
		return this.getActionName().compareTo(other.getActionName());
	}
	
	public abstract Action clone();
	
	public String getName(){
		String patternRestriction="[A-Za-z]*[0-9]*";
		Pattern word = Pattern.compile(patternRestriction);
		Matcher match = word.matcher(actionName);
		if(match.matches()){
			patternRestriction="(.*?)(\\d+)";
			word = Pattern.compile(patternRestriction);
			match = word.matcher(actionName);
			if(match.find())
				return match.group(1);
		}
		return null;
	}
	
	public int getSubscript(){
		String patternRestriction="[A-Za-z]*[0-9]*";
		Pattern word = Pattern.compile(patternRestriction);
		Matcher match = word.matcher(actionName);
		if(match.matches()){
			patternRestriction="(.*?)(\\d+)";
			word = Pattern.compile(patternRestriction);
			match = word.matcher(actionName);
			if(match.find())
				return Integer.parseInt(match.group(2));
		}
		return 0;
	}
	
	public void setSubscript(int newsubscript){
		actionName=getName()+newsubscript;
	}
}
