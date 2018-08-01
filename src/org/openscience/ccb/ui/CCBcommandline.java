package org.openscience.ccb.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.reduction.Move;
import org.openscience.ccb.reduction.Prom;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

public class CCBcommandline {
    
	public static void main(String[] args) throws CCBException, IOException, SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		CCBParser ccbparser=new CCBParser();
	    InputStream inputStream = ccbparser.getClass().getClassLoader().getResourceAsStream("ccbconfiguration_default.properties");
	    Properties ccbprops = new Properties();
	    ccbprops.load(inputStream);
	    CCBConfiguration ccbconfiguration=new CCBConfiguration(ccbprops);
		System.out.println("Enter your process: ");
		Scanner sc = new Scanner(System.in);
	    String input = sc.nextLine();
	    System.out.println("Enter your synchronisation function [format: a,a,b...): ");
	    String syncro = sc.nextLine();
	    StringTokenizer st=new StringTokenizer(syncro,",");
	    List<String> synchronizeList=new  ArrayList<String>();
	    while(st.hasMoreTokens()){
	        synchronizeList.add(st.nextToken());
	    }
	    Synchronize synchronize = new Synchronize(synchronizeList.toArray(new String[]{}));
	    System.out.println("Enter the weak actions [format: a,b,c...): ");
	    String weakactions = sc.nextLine();
	    st=new StringTokenizer(weakactions,",");
	    List<Action> weakActionsList=new  ArrayList<Action>();
	    while(st.hasMoreTokens()){
	    	weakActionsList.add(new WeakAction(st.nextToken()));
	    }
	    Map<Process,List<Transition>> newProcesses=new HashMap<Process,List<Transition>>();
	    newProcesses.put(ccbparser.parseProcess(input, weakActionsList, ccbconfiguration.connectivityMax>-1 ? new Connectivity(ccbconfiguration.connectivityMax) : null, ccbconfiguration.distanceMin>-1 ? new Distance(ccbconfiguration.distanceMax, ccbconfiguration.distanceMin, ccbconfiguration.distanceAnd) : null), null);
		CCBVisitor move = new Move();
		CCBVisitor prom = new Prom();
	    //To save memory and speed up lookup we remember done processes as strings
	    List<Process> doneProcesses=new ArrayList<Process>();
	    int newkey=100;
	    while(!newProcesses.isEmpty()){
	    	List<Transition> transitions=new ArrayList<Transition>();
		    int newProcessCount=0;
	    	for(Process newprocess : newProcesses.keySet()){
	    		List<Process> checkedProcesses=new ArrayList<Process>();
		        if(newProcesses.get(newprocess)==null){
		        	if(newprocess.counter>-1)
		        		System.out.println("Transitions from "+newprocess.counter);
    	    	    List<Transition> localTransitions = newprocess.inferTransitions(synchronize, ccbconfiguration, newprocess);
    	    	    List<Transition> newTransitions = new ArrayList<Transition>();
                    for(int i=0;i<localTransitions.size();i++){
                        Process clone=newprocess.clone();
                        Transition transition = clone.inferTransitions(synchronize, ccbconfiguration, clone).get(i);
                        //we check if the process reached is new in this round using the isomorphism
                        boolean existing=false;
                        Process clonedProcess=clone.clone();
                        clonedProcess.executeTransition(clonedProcess.inferTransitions(synchronize, ccbconfiguration, clonedProcess).get(i), newkey, clonedProcess);
                        if(ccbconfiguration.forceMove)
                            clonedProcess.accept(move);
                        if(ccbconfiguration.forcePromotion)
                            clonedProcess.accept(prom);
                        GraphChecks gc=new GraphChecks(clonedProcess);
                        for(int k=0;k<checkedProcesses.size();k++){
                            if(gc.isIsomorph(checkedProcesses.get(k)))
                                existing=true;
                        }
                        if(!existing){
                            checkedProcesses.add(clonedProcess);
                            transition.setClone(clone);
                            int counter=newProcessCount++;
                            clone.counter=counter;
                            newTransitions.add(transition);
                            System.out.println(counter+": Ready for execution "+transition);
                        }
                    }
    	    	    newProcesses.put(newprocess, newTransitions);
    	    	    if(newTransitions.size()==0){
    	    	    	System.out.println("Process "+newprocess+" can not transition.");
    	    	    }
	    	    }
	    		transitions.addAll(newProcesses.get(newprocess));
	    	}
            System.out.println(transitions.size()+" transitions ready, execute [a]ll or give [e]xclude or [i]nclude list or say [b]reakX");
            String proceed=sc.next();
            if(proceed.startsWith("b")){
                int bondnumber=Integer.parseInt(proceed.substring(1));
                Map<Process,List<Transition>> newNewProcesses=new HashMap<Process,List<Transition>>();
                for(Process process : newProcesses.keySet()){
                    process.removeKey(bondnumber);
                    newNewProcesses.put(process, null);
                }
                newProcesses=newNewProcesses;
            }else{
                List<Integer> exclusionList=null;
                List<Integer> inclusionList=null;
                if(proceed.startsWith("i")){
                	inclusionList=new ArrayList<Integer>();
                    st=new StringTokenizer(proceed.substring(1),",");
                    while(st.hasMoreTokens()){
                    	inclusionList.add(Integer.parseInt(st.nextToken()));
                    }
                }
                if(proceed.startsWith("e")){
                	exclusionList=new ArrayList<Integer>();
                    st=new StringTokenizer(proceed.substring(1),",");
                    while(st.hasMoreTokens()){
                    	exclusionList.add(Integer.parseInt(st.nextToken()));
                    }
                }
                newProcessCount=0;
                Map<Process,List<Transition>> newNewProcesses=new HashMap<Process,List<Transition>>();
                for(Process newprocess : newProcesses.keySet()){
                    for(Transition transition : newProcesses.get(newprocess)){
                        if((exclusionList!=null && !exclusionList.contains(newProcessCount)) || (inclusionList!=null && inclusionList.contains(newProcessCount)) || (inclusionList==null && exclusionList==null)){
                        	//we check if the process is new, ie. not reached anywhere before.
                            Process clonedProcess=transition.getClone();
                            clonedProcess.executeTransition(transition, newkey++, clonedProcess);
                            if(ccbconfiguration.forceMove)
                                clonedProcess.accept(move);
                            if(ccbconfiguration.forcePromotion)
                                clonedProcess.accept(prom);
                            GraphChecks gc=new GraphChecks(clonedProcess);
                            boolean alreadydone=false;
                            for(Process p : doneProcesses){
                            	if(gc.isIsomorph(p))
                            		alreadydone=true;
                            }
                            for(Process p : newNewProcesses.keySet()){
                            	if(gc.isIsomorph(p))
                            		alreadydone=true;
                            }
                            if(!alreadydone){
                                newNewProcesses.put(clonedProcess, null);
                            }
                        }
                        newProcessCount++;
                    }
                    doneProcesses.add(newprocess);
                }
                newProcesses=newNewProcesses;
            }
	    }
        sc.close();
	}
}
