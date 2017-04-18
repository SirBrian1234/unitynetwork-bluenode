package kostiskag.unitynetwork.bluenode.GUI;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingBlueNodeFunctions;

import javax.swing.GroupLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

/**
 *
 * @author kostis
 */
public class AddRemoteRedNode extends javax.swing.JFrame {

    /**
     * Creates new form AddRemoteRedNode
     */
    public AddRemoteRedNode() {
        initComponents();
    }

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Adding a remote RedNode");

        jLabel2.setText("Virtual Address");

        jLabel3.setText("Hostname");

        jButton1.setText("SEARCH AND LEASE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        
        rdbtnNewRadioButton = new JRadioButton("Seach by hostname");
        rdbtnNewRadioButton.setSelected(true);
        buttonGroup.add(rdbtnNewRadioButton);
        
        rdbtnNewRadioButton_1 = new JRadioButton("Search by vaddress");
        buttonGroup.add(rdbtnNewRadioButton_1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(rdbtnNewRadioButton)
        				.addComponent(rdbtnNewRadioButton_1)
        				.addGroup(layout.createSequentialGroup()
        					.addGap(21)
        					.addComponent(jLabel2)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(21)
        					.addComponent(jLabel3)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE))
        				.addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jLabel1))
        			.addContainerGap(175, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(jLabel1)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(rdbtnNewRadioButton)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel3)
        				.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(5)
        			.addComponent(rdbtnNewRadioButton_1)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel2)
        				.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
        			.addComponent(jButton1))
        );
        getContentPane().setLayout(layout);

        pack();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (App.bn.joined) {
	    	if (rdbtnNewRadioButton.isSelected()) {
	        	String bnhostname = jTextField2.getText();
	        	//TODO
	        	//typical validate
	        } else {
	        	String vaddress = jTextField1.getText();
	        	//TODO
	        	//typical validate
	        }
        }
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JRadioButton rdbtnNewRadioButton;
    private javax.swing.JRadioButton rdbtnNewRadioButton_1;
    private final ButtonGroup buttonGroup = new ButtonGroup();
}
