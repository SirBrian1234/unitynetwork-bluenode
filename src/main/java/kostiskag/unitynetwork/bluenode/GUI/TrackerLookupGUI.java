package kostiskag.unitynetwork.bluenode.gui;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.GroupLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

/**
 *
 * @author kostis
 */
public class TrackerLookupGUI extends javax.swing.JFrame {

    /**
     * Creates new form AddRemoteRedNode
     */
    public TrackerLookupGUI() {
    	setTitle("Remote Red Node Lookup (Debug)");
        initComponents();
    }

    private void initComponents() {
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel2.setText("TrackerClient");

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
        					.addGap(16)
        					.addComponent(lblResponce)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 327, GroupLayout.PREFERRED_SIZE)
        					.addContainerGap())
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(jButton1)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(btnNewButton)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(jTextField1))
        						.addComponent(jLabel2)
        						.addComponent(label, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE))
        					.addGap(51))))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(15)
        			.addComponent(jLabel2)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jButton1)
        				.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(btnNewButton)
        				.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(lblResponce))
        			.addPreferredGap(ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
        			.addComponent(label)
        			.addContainerGap())
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
		String vaddress = jTextField1.getText();
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
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JButton btnNewButton;
    private JLabel label;
    private JTextField textField;
    private JLabel lblResponce;
}
