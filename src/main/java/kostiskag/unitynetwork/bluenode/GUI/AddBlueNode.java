package kostiskag.unitynetwork.bluenode.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingBlueNodeFunctions;

/**
 *
 * @author kostis
 */
public class AddBlueNode extends javax.swing.JFrame implements Runnable {

	BlueNodeInstance bn;
	
    /**
     * Creates new form AddBlueNode
     */
    public AddBlueNode() {
    	if (App.bn.joined) {
    		initComponents();
    	}
    }
    
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Adding a new BlueNode");

        jButton1.setText("ASSOCIATE");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setText("Full Associate");

        jLabel4.setText("Hostname");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(jLabel1))
        				.addGroup(layout.createSequentialGroup()
        					.addContainerGap()
        					.addGroup(layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(jCheckBox1)
        						.addGroup(layout.createSequentialGroup()
        							.addComponent(jLabel4)
        							.addPreferredGap(ComponentPlacement.UNRELATED)
        							.addComponent(jTextField1, 111, 111, 111))))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(22)
        					.addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(jLabel1)
        			.addGap(18)
        			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel4)
        				.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jCheckBox1)
        			.addGap(18)
        			.addComponent(jButton1)
        			.addContainerGap(117, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (!jTextField1.getText().isEmpty() && jTextField1.getText().length() <= App.max_str_len_small_size) {            
        	String phAddress = TrackingBlueNodeFunctions.getPhysical(jTextField1.getText());
            App.bn.ConsolePrint("ADDING BLUE NODE "+jTextField1.getText()+" on address "+phAddress);
            BlueNodeClient.addRemoteBlueNode(phAddress, 7000, jTextField1.getText(), jCheckBox1.isSelected());                    
        } else {
            App.bn.ConsolePrint("NOT JOINED YET");
        }
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextField1;
   
    public void run() {
        new AddBlueNode().setVisible(true);
    }
}
