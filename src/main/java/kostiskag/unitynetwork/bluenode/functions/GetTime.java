package kostiskag.unitynetwork.bluenode.functions;

/**
 *
 * @author kostis
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
