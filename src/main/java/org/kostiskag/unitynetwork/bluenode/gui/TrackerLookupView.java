package org.kostiskag.unitynetwork.bluenode.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.PublicKey;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import java.awt.Font;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class TrackerLookupView extends javax.swing.JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2134450119636073406L;
	/**
     * Creates new form AddRemoteRedNode
     */
    public TrackerLookupView() {
    	setTitle("Remote Red Node Lookup (Debug)");
        initComponents();
    }

    private void initComponents() {
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel2.setText("input:");

        jButton1.setText("checkRnOnlineByHostname(rnhostname)");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        
        btnNewButton = new JButton("checkRnOnlineByVaddr(String vaddress)");
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		jButton2ActionPerformed(evt);
        	}
        });
        
        label = new JLabel("Please fill in the respective fields for each option and press the button");
        
        textField = new JTextArea();
        textField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textField.setLineWrap(true);
        textField.setEditable(false);
        textField.setColumns(10);
        
        lblResponce = new JLabel("Responce");
        
        JButton btnNewButton_1 = new JButton("GETBNPUB");
        btnNewButton_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (App.bn.joined) {
                	String bnName = jTextField2.getText();
                	if (!bnName.isEmpty() && bnName.length() < App.max_str_len_small_size) {
                		TrackerClient tr = new TrackerClient();
                		PublicKey key = tr.getBlueNodesPubKey(bnName);
                		if (key != null) {
							try {
								textField.setText(CryptoUtilities.objectToBase64StringRepresentation(key));
							} catch (IOException e) {
								textField.setText("null");
							}
						} else {
                			textField.setText("null");
                		}
                	}
        	    }
        	}
        });
        
        JButton btnNewButton_2 = new JButton("GETRNPUB");
        btnNewButton_2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (App.bn.joined) {
                	String hostname = jTextField2.getText();
                	if (!hostname.isEmpty() && hostname.length() < App.max_str_len_small_size) {
                		TrackerClient tr = new TrackerClient();
                		PublicKey key = tr.getRedNodesPubKey(hostname);
                		if (key != null) {
							try {
								textField.setText(CryptoUtilities.objectToBase64StringRepresentation(key));
							} catch (IOException ex) {
								textField.setText("null");
							}
						} else {
                			textField.setText("null");
                		}
                	}
        	    }
        	}
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(jButton1)
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(jLabel2)
        							.addGap(18)
        							.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
        						.addComponent(btnNewButton))
        					.addGap(848))
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        						.addComponent(btnNewButton_2, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        						.addComponent(btnNewButton_1, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
        					.addGap(854))
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(lblResponce)
        					.addContainerGap(1060, Short.MAX_VALUE))
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(label, GroupLayout.PREFERRED_SIZE, 437, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 762, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        			.addGap(15)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel2)
        				.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jButton1)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnNewButton)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnNewButton_1)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnNewButton_2)
        			.addGap(18)
        			.addComponent(lblResponce)
        			.addGap(18)
        			.addComponent(textField, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(label)
        			.addContainerGap(44, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
    }

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (App.bn.joined) {
        	String rnhostname = jTextField2.getText();
        	if (!rnhostname.isEmpty() && rnhostname.length() < App.max_str_len_small_size) {
        		TrackerClient tr = new TrackerClient();
        		String bluenode = tr.checkRnOnlineByHostname(rnhostname);
        		if (bluenode != null) {
        			textField.setText("Remote Red Node "+rnhostname+" is leased at Bluenode named "+bluenode);
        		} else {
        			textField.setText("Remote Red Node "+rnhostname+" is offline");
        		}
        	}
	    }
    }
	
	private void jButton2ActionPerformed(ActionEvent evt) {
		String vaddress = jTextField2.getText();
    	if (!vaddress.isEmpty() && vaddress.length() < App.max_str_len_small_size) {
    		TrackerClient tr = new TrackerClient();
    		String bluenode = tr.checkRnOnlineByVaddr(vaddress);	 
    		if (bluenode != null) {
    			textField.setText("Remote Red Node "+vaddress+" is leased at Bluenode named "+bluenode);
    		} else {
    			textField.setText("Remote Red Node "+vaddress+" is offline");
    		}
    	}		
	}

    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jTextField2;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JButton btnNewButton;
    private JLabel label;
    private JTextArea textField;
    private JLabel lblResponce;
}
