package org.kostiskag.unitynetwork.bluenode.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class GetTime {
    public static String getFullTimestamp(){
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        return date;
    }
    
    public static String getSmallTimestamp(){
        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
        return date;
    }
}
