/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.Functions;

/**
 *
 * @author kostis
 */

public class getTime {
    public static String getFullTimestamp(){
        String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
        return date;
    }
    
    public static String getSmallTimestamp(){
        String date = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        return date;
    }
}
