package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.DataOutputStream;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.service.GlobalSocketFunctions;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerTimeBuilder;

/**
 *
 * @author Konstantinos Kagiampakis
 */
class TrackingFunctions {        

    public static void check(DataOutputStream outputWriter, SecretKey sessionKey) {
        try {
			SocketUtilities.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
			TrackerTimeBuilder.getInstance().resetClock();

        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void getrns(DataOutputStream outputWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter, sessionKey);
    }
    
    public static void killsig(DataOutputStream outputWriter, SecretKey sessionKey) {
        Bluenode.getInstance().terminate();
    }
}