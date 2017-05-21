package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.DataOutputStream;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.SocketFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
class TrackingFunctions {        

    public static void check(DataOutputStream outputWriter, SecretKey sessionKey) {
        try {
			SocketFunctions.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
			App.bn.trackerRespond.set(0);
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void getrns(DataOutputStream outputWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter, sessionKey);
    }
    
    public static void killsig(DataOutputStream outputWriter, SecretKey sessionKey) {
    	App.bn.localRedNodesTable.exitAll();
        App.bn.die();
    }
}