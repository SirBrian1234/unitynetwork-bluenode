package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.CommonServiceFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
final class TrackingFunctions {

    public static void check(TrackerTimeBuilder timeBuilder, DataOutputStream outputWriter, SecretKey sessionKey) {
        try {
			SocketUtilities.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
			timeBuilder.resetClock();

        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    public static void getRns(LocalRedNodeTable redNodeTable, DataOutputStream outputWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException {
    	CommonServiceFunctions.sendLocalRedNodes(redNodeTable, outputWriter, sessionKey);
    }
    
    public static void killSig(Runnable terminate) {
        AppLogger.getInstance().consolePrint("Received KillSig from tracker. Bluenode terminates.");
        terminate.run();
    }
}