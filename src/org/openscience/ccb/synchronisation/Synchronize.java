package org.openscience.ccb.synchronisation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nu.xom.Element;

public class Synchronize {
	
	Map<Set<String>,String> gamma;
	
	public Synchronize(Map<Set<String>,String> gamma){
		this.gamma=gamma;
	}
	
	public Synchronize(String[] gammainput){
		gamma=new HashMap<Set<String>,String>();
		for(int i=0;i<gammainput.length;i+=3){
			Set<String> set=new HashSet<String>();
			set.add(gammainput[i]);
			set.add(gammainput[i+1]);
			gamma.put(set, gammainput[i+2]);
		}
	}
	
	public String isSychronized(String a, String b){
		Set<String> set = new HashSet<String>();
		set.add(a);
		set.add(b);
		if(gamma.containsKey(set))
			return gamma.get(set);
		else
			return null;
	}

	public Element getXML() {
		Element synchros=new Element("synchronizes");
		for(Set<String>  pair : gamma.keySet()){
			Element synchro=new Element("synchronize");
			synchros.appendChild(synchro);
			Element first=new Element("first");
			Iterator<String> it=pair.iterator();
			first.appendChild(it.next());
			synchro.appendChild(first);
			Element second=new Element("second");
			if(it.hasNext())
				second.appendChild(it.next());
			else
				second.appendChild(first.getChild(0).getValue());
			synchro.appendChild(second);
			Element result=new Element("result");
			result.appendChild(gamma.get(pair));
			synchro.appendChild(result);
		}
		return synchros;
	}
}
