package kostiskag.unitynetwork.bluenode.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * @author kostis
 */
public class NonAssociatedBlueNodeClientGUI extends javax.swing.JFrame {

	BlueNodeInstance bn;
	
    /**
     * Creates new form AddBlueNode
     */
    public NonAssociatedBlueNodeClientGUI() {
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        						.addComponent(jButton1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        					.addGap(34))
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jLabel4)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(jTextField1, 111, 111, 111)
        					.addContainerGap(156, Short.MAX_VALUE))))
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
        			.addContainerGap(112, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
    }

    
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (App.bn.joined) {
	    	if (!jTextField1.getText().isEmpty() && jTextField1.getText().length() <= App.max_str_len_small_size) {      
	    		TrackerClient tr = new TrackerClient();	
	        	String[] args = tr.getPhysicalBn(jTextField1.getText());	        	
	            if (args != null) {
		        	App.bn.ConsolePrint("ADDING BLUE NODE "+jTextField1.getText()+" on address "+args[0]);
		            BlueNodeClient cl = new BlueNodeClient(jTextField1.getText(), args[0], Integer.parseInt(args[1]));
		            try {
		            	boolean check = cl.checkBlueNode();
		            	if (check) App.bn.ConsolePrint("BLUE NODE "+jTextField1.getText()+" ONLINE");
						else App.bn.ConsolePrint("BLUE NODE "+jTextField1.getText()+" OFFLINE");
					} catch (Exception e) {
						e.printStackTrace();
					}                  
	            } else {
	            	App.bn.ConsolePrint("BLUE NODE "+jTextField1.getText()+" OFFLINE");
	            }
	        }
        }
    }

	protected void jButton2ActionPerformed(ActionEvent arg0) {
		if (App.bn.joined) {
	    	if (!jTextField1.getText().isEmpty() && jTextField1.getText().length() <= App.max_str_len_small_size) {      
	    		TrackerClient tr = new TrackerClient();	
	        	String[] args = tr.getPhysicalBn(jTextField1.getText());	        	
	            if (args != null) {
		        	App.bn.ConsolePrint("ADDING BLUE NODE "+jTextField1.getText()+" on address "+args[0]);
		            BlueNodeClient cl = new BlueNodeClient(jTextField1.getText(), args[0], Integer.parseInt(args[1]));
		            try {
						cl.associateClient();						
					} catch (Exception e) {
						e.printStackTrace();
					}                  
	            } else {
	            	App.bn.ConsolePrint("BLUE NODE "+jTextField1.getText()+" OFFLINE");
	            }
	        }
        }
	}

    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
}
