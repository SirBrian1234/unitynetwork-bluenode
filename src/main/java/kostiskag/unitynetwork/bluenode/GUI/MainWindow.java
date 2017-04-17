package kostiskag.unitynetwork.bluenode.GUI;

import javax.swing.table.DefaultTableModel;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.tables.BlueNodesTable;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.RemoteHandle;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;

/**
 * This is the main bluenode window. 
 * There should be only one object from this class.
 * 
 * @author Konstantinos Kagiampakis
 */
public class MainWindow extends javax.swing.JFrame {

    private final DefaultTableModel localRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Virtual Address", "Hostname", "Username", "Physical Address", "Uplink Port", "Downlink Port"});;
    private final DefaultTableModel remoteRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Hostname", "Virtual Address", "Blue Node Name", "Checked"});;
    private final DefaultTableModel remoteBlueNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Name", "Physical Address", "Uplink Port", "Downlink Port"});
    public int messageCount = 0;

    public MainWindow() {
    	setTitle("Blue Node");
    	initComponents(); 
        if (!App.bn.network) {
    		jTabbedPane1.remove(2);
    	}
        
        jTable1.setDefaultEditor(Object.class, null);
        jTable2.setDefaultEditor(Object.class, null);
        jTable3.setDefaultEditor(Object.class, null);
    }   

    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField1.setFont(new Font("Tahoma", Font.PLAIN, 13));
        jTextField1.setText("BlueNode");
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox9 = new javax.swing.JCheckBox();
        jCheckBox10 = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton3.setToolTipText("Refresh the GUI representation to match the inner data.");
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        jButton5.setToolTipText("Refresh the GUI representation to match the inner data.");
        jPanel10 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jButton12 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton7.setToolTipText("Refresh the GUI representation to match the inner data.");
        jButton10 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();

        jMenuItem1.setText("jMenuItem1");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        jMenuItem5.setText("jMenuItem5");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            { 
                String ObjButtons[] = {"Yes","No"};
                int PromptResult = JOptionPane.showOptionDialog(null,"Are you sure you wish to terminate this Blue Node?\nThis may result in a partial network termination.\nIf you decide to close the BLue Node, it will send the appropriate kill signals to the connected Red Nodes.","",JOptionPane.DEFAULT_OPTION,JOptionPane.WARNING_MESSAGE,null,ObjButtons,ObjButtons[1]);
                if(PromptResult==JOptionPane.YES_OPTION)
                {
                    App.bn.localRedNodesTable.exitAll();
                    if (App.bn.joined) {
            			try {
            				App.bn.leaveNetworkAndDie();
            			} catch (Exception e) {
            				e.printStackTrace();
            				App.bn.die();
            			}
                    }
                	App.bn.die();
                }
            }
        });
        
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel1.setPreferredSize(new java.awt.Dimension(800, 600));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Terminal"));

        jButton2.setText("Clean Terminal");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane5.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));

        jTextField2.setEditable(false);

        jLabel2.setText("Auth Port (TCP)");

        jTextField1.setEditable(false);
        
        jLabel1.setText("Hostname");

        jLabel4.setText("Max Number of RN Hosts");

        jTextField4.setEditable(false);

        jLabel5.setText("UDP Port Range");

        jTextField5.setEditable(false);
        
        JLabel lblOperationMode = new JLabel("Operation Mode");
        
        txtOpMode = new JTextField();
        txtOpMode.setFont(new Font("Tahoma", Font.BOLD, 13));
        txtOpMode.setForeground(new Color(153, 0, 0));
        txtOpMode.setText("Network");
        txtOpMode.setEditable(false);
        txtOpMode.setColumns(10);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3Layout.setHorizontalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel3Layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(jTextField5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
        				.addComponent(jTextField1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
        				.addComponent(lblOperationMode, Alignment.LEADING)
        				.addComponent(jLabel1, Alignment.LEADING)
        				.addComponent(txtOpMode, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
        				.addGroup(Alignment.LEADING, jPanel3Layout.createSequentialGroup()
        					.addComponent(jLabel2)
        					.addGap(46))
        				.addComponent(jTextField2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
        				.addComponent(jLabel4, Alignment.LEADING)
        				.addComponent(jLabel5, Alignment.LEADING)
        				.addComponent(jTextField4, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
        			.addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(lblOperationMode)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(txtOpMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(jLabel1)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(jLabel2)
        			.addGap(8)
        			.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(jLabel5)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jTextField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(18)
        			.addComponent(jLabel4)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(145, Short.MAX_VALUE))
        );
        jPanel3.setLayout(jPanel3Layout);

        jLabel1.getAccessibleContext().setAccessibleName("jLabel1");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Info Variables"));

        jCheckBox2.setText("At least one user was connected");
        jCheckBox2.setEnabled(false);

        jCheckBox3.setText("Has downloaded data from a local Red Node");
        jCheckBox3.setActionCommand("hasDownloadedContentFromBoundHosts");
        jCheckBox3.setEnabled(false);

        jCheckBox4.setText("Has uploaded data to a local Red Node");
        jCheckBox4.setEnabled(false);

        jCheckBox6.setText("Has uploaded data to another Blue Node");
        jCheckBox6.setEnabled(false);

        jCheckBox7.setText("Has downloaded data from another Blue Node");
        jCheckBox7.setEnabled(false);

        jCheckBox8.setText("Is Auth Service Online");
        jCheckBox8.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox6))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox7)
                            .addComponent(jCheckBox3)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jCheckBox8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Traffic"));

        jButton1.setText("Clean");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jToggleButton1.setSelected(true);
        jToggleButton1.setText("View Traffic");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea2.setRows(5);
        jScrollPane6.setViewportView(jTextArea2);

        jCheckBox5.setSelected(true);
        jCheckBox5.setText("view keep alives");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });

        jCheckBox9.setSelected(true);
        jCheckBox9.setText("view routing");
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox9ActionPerformed(evt);
            }
        });

        jCheckBox10.setSelected(true);
        jCheckBox10.setText("view acks");
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox10ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Only view Local Red Nodes");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Only View Blue Nodes");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setSelected(true);
        jRadioButton3.setText("View Both LRNs and BNs");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jCheckBox11.setSelected(true);
        jCheckBox11.setText("view pings");
        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox11ActionPerformed(evt);
            }
        });

        jCheckBox12.setSelected(true);
        jCheckBox12.setText("Keep Scrolled Down");
        jCheckBox12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jRadioButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addGap(48, 48, 48))
                    .addComponent(jScrollPane6)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jCheckBox5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox9))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox12)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jToggleButton1)
                    .addComponent(jCheckBox12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox9)
                    .addComponent(jCheckBox10)
                    .addComponent(jCheckBox11))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
        				.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        				.addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jPanel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jPanel8, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        				.addGroup(jPanel1Layout.createSequentialGroup()
        					.addContainerGap()
        					.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
        						.addGroup(jPanel1Layout.createSequentialGroup()
        							.addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
        							.addPreferredGap(ComponentPlacement.RELATED)
        							.addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
        			.addContainerGap())
        );
        jPanel1.setLayout(jPanel1Layout);

        jTabbedPane1.addTab("General", jPanel1);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Local Red Nodes"));

        jTable1.setModel(localRedNodeTableModel);
        jScrollPane2.setViewportView(jTable1);

        jButton3.setText("Refresh Table");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6Layout.setHorizontalGroup(
        	jPanel6Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel6Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 1356, Short.MAX_VALUE)
        				.addComponent(jButton3))
        			.addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
        	jPanel6Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel6Layout.createSequentialGroup()
        			.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jButton3))
        );
        jPanel6.setLayout(jPanel6Layout);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Local Red Nodes", jPanel2);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Remote Red Nodes"));

        jTable2.setModel(remoteRedNodeTableModel);
        jScrollPane1.setViewportView(jTable2);

        jButton5.setText("Refresh Table");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9Layout.setHorizontalGroup(
        	jPanel9Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel9Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel9Layout.createSequentialGroup()
        					.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 808, Short.MAX_VALUE)
        					.addContainerGap())
        				.addGroup(jPanel9Layout.createSequentialGroup()
        					.addComponent(jButton5, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
        					.addGap(688))))
        );
        jPanel9Layout.setVerticalGroup(
        	jPanel9Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
        			.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jButton5))
        );
        jPanel9.setLayout(jPanel9Layout);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Remote Assosiated BlueNodes"));

        jTable3.setModel(remoteBlueNodeTableModel);
        jScrollPane3.setViewportView(jTable3);

        jButton12.setText("Check Online");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton7.setText("Refresh Table");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton10.setText("Add Static Entry");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton8.setText("Remove Selected");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton13.setText("Status");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("Get Remote Red Nodes");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton15.setText("Exchange Red Nodes");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton7)
                    .addComponent(jButton10)
                    .addComponent(jButton8)
                    .addComponent(jButton12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton13)
                    .addComponent(jButton14)
                    .addComponent(jButton15)))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(614, 614, 614))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Remote Red Nodes", jPanel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE))
        );

        pack();
    }

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jToggleButton1.isSelected() == true) {
            App.bn.viewTraffic = true;
        } else {
            App.bn.viewTraffic = false;
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        jTextArea2.setText("");
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        jTextArea1.setText("");
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
    	updateRemoteRns();
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        updateLocalRns();
    }

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
    	updateBNs();
    }

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Thread(new AddBlueNode()).run();
            }
        });
    }

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] table = jTable3.getSelectedRows();
        RemoteHandle.removeBlueNodes(table);
    }

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] table = jTable3.getSelectedRows();
        RemoteHandle.upingBlueNodes(table);
    }

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jTable3.getSelectedRow() != -1) {
        	String name = (String) jTable3.getValueAt(jTable3.getSelectedRow(), 0);
            new BlueStatus(name);
        }
    }

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] table = jTable3.getSelectedRows();
        RemoteHandle.GetRemoteRedNodes(table);
    }

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {
        int[] table = jTable3.getSelectedRows();
        RemoteHandle.ExchangeRedNodes(table);
    }

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {
         App.bn.viewType[0] = jCheckBox5.isSelected();
    }

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {
        App.bn.viewType[1] = jCheckBox11.isSelected();
    }

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {
       App.bn.viewType[2] = jCheckBox10.isSelected();
    }

    private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {
        App.bn.viewType[3] = jCheckBox9.isSelected();
    }

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton3.isSelected()) {
            App.bn.viewhostType[0] = true;
            App.bn.viewhostType[1] = true;
        }
    }

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton1.isSelected()) {
            App.bn.viewhostType[0] = true;
            App.bn.viewhostType[1] = false;
        }
    }

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton2.isSelected()) {
            App.bn.viewhostType[0] = false;
            App.bn.viewhostType[1] = true;
        }
    }

    private void jCheckBox12ActionPerformed(java.awt.event.ActionEvent evt) {
        App.bn.autoScrollDown = jCheckBox12.isSelected();
    }
    
    public void setBlueNodeInfo() {    
        jTextField1.setText(App.bn.name);
        jTextField2.setText("" + App.bn.authPort);
        jTextField4.setText("" + App.bn.maxRednodeEntries);
        jTextField5.setText("" + App.bn.startPort + "-" + App.bn.endPort);
        if (App.bn.network) {
        	txtOpMode.setText("Network");
        } else if (App.bn.useList){
        	txtOpMode.setText("Standalone/List");
        } else {
        	txtOpMode.setText("Standalone/Plain");
        }
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    public static javax.swing.JCheckBox jCheckBox2;
    public static javax.swing.JCheckBox jCheckBox3;
    public static javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    public static javax.swing.JCheckBox jCheckBox6;
    public static javax.swing.JCheckBox jCheckBox7;
    public static javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    public javax.swing.JTable jTable1;
    public javax.swing.JTable jTable2;
    public javax.swing.JTable jTable3;
    public static javax.swing.JTextArea jTextArea1;
    public static javax.swing.JTextArea jTextArea2;
    public static javax.swing.JTextField jTextField1;
    public static javax.swing.JTextField jTextField2;
    public static javax.swing.JTextField jTextField4;
    public static javax.swing.JTextField jTextField5;
    private javax.swing.JToggleButton jToggleButton1;
    private JTextField txtOpMode;

	public synchronized void updateLocalRns() {
		String[][] obj = App.bn.localRedNodesTable.buildGUIObj();
		int rows = localRedNodeTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
            localRedNodeTableModel.removeRow(0);
        }
        for (int i = 0; i < obj.length; i++) {
            localRedNodeTableModel.addRow(obj[i]);
        }
        
        jTable1.setModel(localRedNodeTableModel);
        repaint();
	}

	public synchronized void updateBNs() {
		String[][] obj = App.bn.blueNodesTable.buildBNGUIObj();
		int rows = remoteBlueNodeTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
        	remoteBlueNodeTableModel.removeRow(0);
        }
        for (int i = 0; i < obj.length; i++) {
        	remoteBlueNodeTableModel.addRow(obj[i]);
        }	
        jTable2.setModel(remoteBlueNodeTableModel);
        repaint();
	}

	public synchronized void updateRemoteRns() {
		String[][] obj = App.bn.blueNodesTable.buildRRNGUIObj();
		int rows = remoteRedNodeTableModel.getRowCount();
        for (int i = 0; i < rows; i++) {
        	remoteRedNodeTableModel.removeRow(0);
        }
        for (int i = 0; i < obj.length; i++) {
        	remoteRedNodeTableModel.addRow(obj[i]);
        }		
        jTable3.setModel(remoteRedNodeTableModel);
        repaint();
	}
}
