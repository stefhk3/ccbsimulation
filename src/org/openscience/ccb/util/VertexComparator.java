package org.openscience.ccb.util;

import java.util.Comparator;

import org.openscience.ccb.process.Process;

public class VertexComparator implements Comparator<Process> {

    @Override
    public int compare(Process o1, Process o2) {
        String p1=o1.toString();
        String simplep1=makeSimple(p1);
        String p2=o2.toString();
        String simplep2=makeSimple(p2);
        if(simplep1.equals(simplep2))
            return 0;
        else
            return 1;
    }

    private String makeSimple(String p1) {
        StringBuffer buffer=new StringBuffer();
        for(int i=0;i<p1.length();i++){
            if(!Character.isDigit(p1.charAt(i)) && p1.charAt(i)!='[' && p1.charAt(i)!=']')
                buffer.append(p1.charAt(i));
        }
        return buffer.toString();
    }

}
