package kostiskag.unitynetwork.bluenode.Routing.packets;

/**
 *
 * @author kostis
 */
public class Envelope {
    //the first byte is type
    //the second byte is number of packet
    //after the 2 bytes either a full ip datagramm or a unity datagramm
    //upackets have no tail just header
    static int offset = 2;
    
     public static byte[] makeEnvelope(int type, int number, byte[] datagramm){
        byte Btype = (byte) type;
        byte Bnumber = (byte) number;
        
        byte[] envelope = new byte[ 2 + datagramm.length];
        System.arraycopy(Btype, 0, envelope, 0, 1);
        System.arraycopy(Bnumber, 0, envelope, 1, 1);
        System.arraycopy(datagramm, 0, envelope, 2, datagramm.length);
        
        return envelope;
    }
     
    public static int getType(byte[] envelope) {
        int type = (int) envelope[0];
        return type;
    } 
    
    public static int getNumber(byte[] envelope) {
        int num = (int) envelope[1];
        return num;
    } 
    
    public static byte[] getDatagramm(byte[] envelope) {
        byte[] datagramm = new byte[envelope.length - offset];
        System.arraycopy(envelope, 0, datagramm, offset, envelope.length - offset);
        return datagramm;
    }            
   
}
