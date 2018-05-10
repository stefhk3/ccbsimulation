package org.openscience.ccb.util;

import java.lang.reflect.Field;
import java.util.Properties;

public class CCBConfiguration {
    
    public CCBConfiguration(Properties ccbprops) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        for(Object prop : ccbprops.keySet()){
            Field chap = this.getClass().getDeclaredField((String)prop);
            if(chap.getType().equals(boolean.class)){
                if(((String)ccbprops.get(prop)).equals("false"))
                    chap.setBoolean(this, false);
                else
                    chap.setBoolean(this, true);
            }else if(chap.getType().equals(int.class)){
            	chap.setInt(this, Integer.parseInt((String)ccbprops.get(prop)));
            }
        }
    }

    public boolean allowSpontaneousBreaks=false;
    public boolean forceMove=true;
    public boolean forcePromotion=true;
    public int connectivityMax=3;
    public int distanceMax=1;
    public int distanceMin=5;
    public boolean distanceAnd=false;

}
