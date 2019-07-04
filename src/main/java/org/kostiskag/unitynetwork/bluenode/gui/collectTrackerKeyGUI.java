package org.kostiskag.unitynetwork.bluenode.gui;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class collectTrackerKeyGUI {

	private JFrame frame;
	private JTextField txtNotSet;
	private JTextArea txtpnWii;
	private JButton btnCollectTrackersPublic;
	
	/**
	 * Create the application.
	 */
	public collectTrackerKeyGUI() {
		initialize();
		if (App.bn.network && App.bn.trackerPublicKey != null) {
			txtNotSet.setText("Key is set");
			txtpnWii.setText(CryptoUtilities.bytesToBase64String(App.bn.trackerPublicKey.getEncoded()));
			btnCollectTrackersPublic.setEnabled(false);
		} 
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 364);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblFromThis = new JLabel("<html>From this window you may collect the tracker's public key in order to authenticate the bluenode inside the network. The tracker's address and port can be set if not allready in the config file.</html>");
		lblFromThis.setBounds(12, 13, 408, 66);
		frame.getContentPane().add(lblFromThis);
		
		JLabel lblKeyStatus = new JLabel("Key status");
		lblKeyStatus.setBounds(12, 92, 100, 16);
		frame.getContentPane().add(lblKeyStatus);
		
		txtNotSet = new JTextField();
		txtNotSet.setFont(new Font("Tahoma", Font.ITALIC, 13));
		txtNotSet.setText("Not Set");
		txtNotSet.setEditable(false);
		txtNotSet.setBounds(124, 89, 116, 22);
		frame.getContentPane().add(txtNotSet);
		txtNotSet.setColumns(10);
		
		btnCollectTrackersPublic = new JButton("Collect tracker's public key");
		btnCollectTrackersPublic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				collect();
			}
		});
		btnCollectTrackersPublic.setBounds(237, 215, 183, 25);
		frame.getContentPane().add(btnCollectTrackersPublic);
		
		txtpnWii = new JTextArea();
		txtpnWii.setLineWrap(true);
		txtpnWii.setFont(new Font("Monospaced", Font.PLAIN, 12));
		txtpnWii.setBounds(12, 121, 408, 81);
		frame.getContentPane().add(txtpnWii);
		
		JLabel lblifYouWish = new JLabel("<html>If you wish to delete the tracker's public key, you may delete its file from the bluenode's directory and reset the bluenode.</html>");
		lblifYouWish.setBounds(12, 253, 408, 40);
		frame.getContentPane().add(lblifYouWish);
	}

	protected void collect() {
		TrackerClient.getPubKey();
		if (App.bn.trackerPublicKey != null) {
			txtpnWii.setText(CryptoUtilities.bytesToBase64String(App.bn.trackerPublicKey.getEncoded()));
			txtNotSet.setText("Key is set");
			btnCollectTrackersPublic.setEnabled(false);
			App.bn.window.enableUploadKey();
		}
	}

	public void setVisible() {
		frame.setVisible(true);
	}
}
