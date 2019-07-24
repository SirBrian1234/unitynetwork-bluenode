package org.kostiskag.unitynetwork.bluenode.gui;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.PublicKey;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.kostiskag.unitynetwork.bluenode.ModeOfOperation;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;


final class CollectTrackerKeyView {

	private JFrame frame;
	private JTextField txtNotSet;
	private JTextArea txtpnWii;
	private JButton btnCollectTrackersPublic;

	private final ModeOfOperation mode;
	private final Optional<PublicKey> trackerPublicKey;
	private final Runnable collectTrackerPublicKey;

	/**
	 * Create the application.
	 */
	public CollectTrackerKeyView(ModeOfOperation mode, Optional<PublicKey> trackerPublicKey, Runnable collectTrackerPublicKey) {
		if (mode != ModeOfOperation.NETWORK) {
			throw new IllegalArgumentException("This window may only be called on network");
		} else {
			this.mode = mode;
			this.trackerPublicKey = trackerPublicKey;
			this.collectTrackerPublicKey = collectTrackerPublicKey;
			initialize();
			if (this.trackerPublicKey.isPresent()) {
				txtNotSet.setText("Key is set");
				txtpnWii.setText(CryptoUtilities.bytesToBase64String(this.trackerPublicKey.get().getEncoded()));
				btnCollectTrackersPublic.setEnabled(false);
			}
			frame.setVisible(true);
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
		this.collectTrackerPublicKey.run();
		if (this.trackerPublicKey.isPresent()) {
			txtpnWii.setText(CryptoUtilities.bytesToBase64String(this.trackerPublicKey.get().getEncoded()));
			txtNotSet.setText("Key is set");
			btnCollectTrackersPublic.setEnabled(false);
			MainWindow.getInstance().enableUploadPublicKey();
		} else {
			txtNotSet.setText("Key not set");
		}
	}
}
