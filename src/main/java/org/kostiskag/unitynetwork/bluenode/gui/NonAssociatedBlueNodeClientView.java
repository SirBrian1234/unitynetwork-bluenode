package org.kostiskag.unitynetwork.bluenode.gui;

import java.io.IOException;
import java.security.PublicKey;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class NonAssociatedBlueNodeClientView extends javax.swing.JFrame {

	BlueNode bn;
	
    /**
     * Creates new form AddBlueNode
     */
    public NonAssociatedBlueNodeClientView() {
    	setTitle("Public Blue Node Client Functions (Debug)");
    	initComponents();    	
    }
    
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("getPhysicalBn(String BNHostname) - checkBlueNode() ");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Blue Node Name");
        
        JButton btnNewButton = new JButton("getPhysicalBn(String BNHostname) - associateClient() ");
        btnNewButton.setToolTipText("");
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		jButton2ActionPerformed(arg0);
        	}
        });
        
        textField = new JTextField();
        textField.setEditable(false);
        textField.setColumns(10);
        
        lblResponce = new JLabel("Responce");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jLabel4)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(jTextField1, 111, 111, 111)
        					.addContainerGap(158, Short.MAX_VALUE))
        				.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        						.addGroup(Alignment.LEADING, layout.createSequentialGroup()
        							.addComponent(lblResponce)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
        						.addComponent(btnNewButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        						.addComponent(jButton1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        					.addGap(34))))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(53)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel4)
        				.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(18)
        			.addComponent(jButton1)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(btnNewButton)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblResponce))
        			.addContainerGap(77, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
    }

    
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (Bluenode.getInstance().isJoinedNetwork()) {
	    	if (!jTextField1.getText().isEmpty() && jTextField1.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size()) {
	    		String bnName = jTextField1.getText();
	    		TrackerClient tr = new TrackerClient();	
	        	String[] args = tr.getPhysicalBn(bnName);	 
	        	if (args[0].equals("OFFLINE")) {
	        		textField.setText("BLUE NODE "+bnName+" IS OFFLINE");
	        		return;
	        	}
	        	String addr = args[0];
	        	int port = Integer.parseInt(args[1]);
	        	System.out.println("collected address "+addr+" "+port);
	        	
	        	tr = new TrackerClient();	
	        	PublicKey pub = tr.getBlueNodesPubKey(bnName);
	            if (pub == null) {
	            	textField.setText("BLUE NODE "+bnName+" NULL PUBKEY");
	            	return;
	            }
				try {
					System.out.println("collected key "+ CryptoUtilities.objectToBase64StringRepresentation(pub));
				} catch (IOException e) {
					return;
				}

				BlueNodeClient cl = new BlueNodeClient(bnName, pub, addr, port);
	            try {
	            	boolean check = cl.checkBlueNode();
	            	if (check) textField.setText("BLUE NODE "+bnName+" ONLINE");
					else textField.setText("BLUE NODE "+bnName+" OFFLINE");
				} catch (Exception e) {
					e.printStackTrace();
				}                  
	           
	        }
        }
    }

	protected void jButton2ActionPerformed(ActionEvent arg0) {
		if (Bluenode.getInstance().isJoinedNetwork()) {
	    	if (!jTextField1.getText().isEmpty() && jTextField1.getText().length() <= NumericConstraints.MAX_STR_LEN_SMALL.size()) {
	    		TrackerClient tr = new TrackerClient();	
	        	String[] args = tr.getPhysicalBn(jTextField1.getText());	 
	        	if (args[0].equals("OFFLINE")) {
	        		textField.setText("BLUE NODE "+jTextField1.getText()+" IS OFFLINE");
	        		return;
	        	}
	        	
	        	tr = new TrackerClient();	
	        	PublicKey pub = tr.getBlueNodesPubKey(jTextField1.getText());
	            if (pub == null) {
	            	textField.setText("BLUE NODE "+jTextField1.getText()+" NULL PUBKEY");
	            	return;
	            }
	        	
            	textField.setText("ADDING BLUE NODE "+jTextField1.getText()+" on address "+args[0]);
	            BlueNodeClient cl = new BlueNodeClient(jTextField1.getText(), pub, args[0], Integer.parseInt(args[1]));
	            try {
					cl.associateClient();						
				} catch (Exception e) {
					e.printStackTrace();
					textField.setText("Could not establish association.");
				}                  	            
	        }
        }
	}

    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
    private JTextField textField;
    private JLabel lblResponce;
}
