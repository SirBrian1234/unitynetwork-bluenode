package org.kostiskag.unitynetwork.bluenode.utilities;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class GetTime {
    public static String getFullTimestamp(){
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
        return date;
    }
    
    public static String getSmallTimestamp(){
        String date = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        return date;
    }
}
