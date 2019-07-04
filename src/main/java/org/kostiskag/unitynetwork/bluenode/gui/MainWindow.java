package org.kostiskag.unitynetwork.bluenode.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.kostiskag.unitynetwork.bluenode.App;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * This is the main bluenode window. 
 * There should be only one object from this class.
 * 
 * @author Konstantinos Kagiampakis
 */
public class MainWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 5505085647328297106L;
	private final Object lockLocal = new Object();
    private final Object lockBn = new Object();
    private final Object lockRRn = new Object();
    private final Object lockTraffic = new Object();
    private final Object lockConsole = new Object();
	private final DefaultTableModel localRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Hostname", "Virtual Address", "Physical Address", "Auth Port", "Send Port", "Receive Port"});
    private final DefaultTableModel remoteRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Hostname", "Virtual Address", "Blue Node Name", "Last Checked"});
    private final DefaultTableModel remoteBlueNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Name", "Is a Server", "Physical Address", "Auth Port", "Send Port", "Receive Port", "Last Checked"});
    private boolean autoScrollDownTraffic = true;
    private boolean autoScrollDownConsole = true;
    private boolean viewTraffic = true;
    private boolean[] viewType = new boolean[] { true, true, true, true };
	private boolean[] viewhostType = new boolean[] { true, true };
	private int messageCountConsole = 0;
    private int messageCountTraffic = 0;
    private About about;

    public MainWindow() {
    	setTitle("Blue Node");
    	initComponents(); 
        if (!App.bn.network) {
    		jTabbedPane1.remove(2); 
    		btnCollectTrackersPublic.setEnabled(false);
    		btnNewButton_1.setEnabled(false);    		
        } else if (App.bn.trackerPublicKey == null) {
			btnNewButton_1.setEnabled(false);
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
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jButton3 = new javax.swing.JButton();
        jButton3.setToolTipText("Refresh the GUI representation to match the inner data.");
        jPanel7 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jButton5 = new javax.swing.JButton();
        jButton5.setToolTipText("Refresh the GUI representation to match the inner data.");
        jPanel10 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jTable3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jButton7 = new javax.swing.JButton();
        jButton7.setToolTipText("Refresh the GUI representation to match the inner data.");
        jButton10 = new javax.swing.JButton();
        jButton10.setBackground(new Color(204, 51, 0));

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

        jButton2.setText("Clear Terminal");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextArea1.setRows(5);
        jScrollPane5.setViewportView(jTextArea1);
        
        checkBox = new JCheckBox();
        checkBox.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent arg0) {
        		autoScrollDownConsole = checkBox.isSelected();
        	}
        });
        checkBox.setText("Keep Scrolled Down");
        checkBox.setSelected(true);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4Layout.setHorizontalGroup(
        	jPanel4Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel4Layout.createSequentialGroup()
        			.addGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel4Layout.createSequentialGroup()
        					.addGap(263)
        					.addComponent(checkBox)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
        				.addGroup(jPanel4Layout.createSequentialGroup()
        					.addContainerGap()
        					.addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 553, GroupLayout.PREFERRED_SIZE)))
        			.addGap(92))
        );
        jPanel4Layout.setVerticalGroup(
        	jPanel4Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel4Layout.createSequentialGroup()
        			.addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel4Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
        				.addComponent(checkBox))
        			.addGap(6))
        );
        jPanel4.setLayout(jPanel4Layout);

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
        
        lblEchoAddress = new JLabel("Echo IP address");
        
        textField = new JTextField();
        textField.setEditable(false);
        textField.setColumns(10);
        
        btnNewButton = new JButton("About");
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (about == null) {
					about = new About();
				} else if (!about.isVisible()) {
					about = new About();
				}
        	}
        });
        
        btnNewButton_1 = new JButton("Manage BlueNode's Public Key");
        btnNewButton_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		new UploadKeyView().setVisible();
        	}
        });
        
        btnCollectTrackersPublic = new JButton("Collect Tracker's Public Key");
        btnCollectTrackersPublic.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		new CollectTrackerKeyView().setVisible();
        	}
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3Layout.setHorizontalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(btnNewButton, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(lblOperationMode)
        				.addComponent(txtOpMode, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel1)
        				.addComponent(jTextField1, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(lblEchoAddress)
        				.addComponent(textField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel2)
        				.addComponent(jTextField2, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel5)
        				.addComponent(jTextField5, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(btnCollectTrackersPublic)
        				.addComponent(jTextField4, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
        			.addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(lblOperationMode)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(txtOpMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jLabel1)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(lblEchoAddress)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel2)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel5)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jTextField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel4)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jTextField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(13)
        			.addComponent(btnCollectTrackersPublic)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(btnNewButton_1)
        			.addPreferredGap(ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
        			.addComponent(btnNewButton)
        			.addContainerGap())
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

        jButton1.setText("Clear");
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
            	updateLocalRns();
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6Layout.setHorizontalGroup(
        	jPanel6Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel6Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 1416, Short.MAX_VALUE)
        				.addComponent(jButton3))
        			.addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
        	jPanel6Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel6Layout.createSequentialGroup()
        			.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
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
        
        JButton btnAddRemote = new JButton("Tracker Lookup Remote Red Node");
        btnAddRemote.setBackground(new Color(204, 51, 0));
        btnAddRemote.addActionListener(new ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
        		trackerRNLookup(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9Layout.setHorizontalGroup(
        	jPanel9Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel9Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
        				.addComponent(jButton5, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
        				.addComponent(btnAddRemote))
        			.addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
        	jPanel9Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel9Layout.createSequentialGroup()
        			.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 402, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jButton5)
        			.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        			.addComponent(btnAddRemote))
        );
        jPanel9.setLayout(jPanel9Layout);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Remote Assosiated BlueNodes"));

        jTable3.setModel(remoteBlueNodeTableModel);
        jScrollPane3.setViewportView(jTable3);

        jButton7.setText("Refresh Table");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton10.setText("New BN Associate / Check");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	openNewBnWindow();
            }
        });
        
        btnOpenBlueNode = new JButton("Open Associated Blue Node Client Funtions");
        btnOpenBlueNode.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		openAssosiatedWindow();
        	}
        });
        btnOpenBlueNode.setBackground(new Color(153, 153, 0));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10Layout.setHorizontalGroup(
        	jPanel10Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel10Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel10Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
        				.addComponent(jButton7, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
        				.addGroup(jPanel10Layout.createSequentialGroup()
        					.addComponent(jButton10, GroupLayout.PREFERRED_SIZE, 270, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.UNRELATED)
        					.addComponent(btnOpenBlueNode, GroupLayout.PREFERRED_SIZE, 339, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
        	jPanel10Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel10Layout.createSequentialGroup()
        			.addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jButton7)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel10Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jButton10)
        				.addComponent(btnOpenBlueNode)))
        );
        jPanel10.setLayout(jPanel10Layout);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7Layout.setHorizontalGroup(
        	jPanel7Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(Alignment.LEADING, jPanel7Layout.createSequentialGroup()
        			.addContainerGap()
        			.addComponent(jPanel9, GroupLayout.PREFERRED_SIZE, 511, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(953, Short.MAX_VALUE))
        		.addGroup(jPanel7Layout.createSequentialGroup()
        			.addContainerGap(527, Short.MAX_VALUE)
        			.addComponent(jPanel10, GroupLayout.PREFERRED_SIZE, 937, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
        	jPanel7Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel7Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel7Layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(jPanel10, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
        				.addComponent(jPanel9, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
        			.addContainerGap())
        );
        jPanel7.setLayout(jPanel7Layout);

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

    private void openNewBnWindow() {
        new NonAssociatedBlueNodeClientView().setVisible(true);
    }
    
    protected void openAssosiatedWindow() {
		try {
			int row = jTable3.getSelectedRow();
			if (row >= 0) {
				try {
					new AssociatedBlueNodeClientView((String) jTable3.getValueAt(row, 0)).setVisible();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex){
			
		}
	}
	
	protected void trackerRNLookup(ActionEvent evt) {
		new TrackerLookupView().setVisible(true);
	}

	private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jToggleButton1.isSelected()) {
            viewTraffic = true;
        } else {
            viewTraffic = false;
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

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
    	updateBNs();
    }

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {
         viewType[0] = jCheckBox5.isSelected();
    }

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {
        viewType[1] = jCheckBox11.isSelected();
    }

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {
       viewType[2] = jCheckBox10.isSelected();
    }

    private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {
        viewType[3] = jCheckBox9.isSelected();
    }

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton3.isSelected()) {
            viewhostType[0] = true;
            viewhostType[1] = true;
        }
    }

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton1.isSelected()) {
            viewhostType[0] = true;
            viewhostType[1] = false;
        }
    }

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jRadioButton2.isSelected()) {
            viewhostType[0] = false;
            viewhostType[1] = true;
        }
    }

    private void jCheckBox12ActionPerformed(java.awt.event.ActionEvent evt) {
        autoScrollDownTraffic = jCheckBox12.isSelected();
    }
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private JCheckBox checkBox;
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
    private JButton btnOpenBlueNode;
    private JLabel lblEchoAddress;
    private JTextField textField;
    private JButton btnNewButton;
    private JButton btnCollectTrackersPublic;
    private JButton btnNewButton_1;
    
    public void enableUploadKey () {
    	btnNewButton_1.setEnabled(true);
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
    
    public void setEchoIpAddress(String addr) {
    	textField.setText(addr);
    }
    
    public void ConsolePrint(String message) {
    	synchronized (lockConsole) {
    		messageCountConsole++;
    		jTextArea1.append(message + "\n");    
    		
    		if (messageCountConsole > 10000) {
				messageCountConsole = 0;
				jTextArea1.setText("");
			}
			if (autoScrollDownConsole) {
				jTextArea1.select(jTextArea1.getHeight() + 10000, 0);
			}
		}    	
    }
    
    public void TrafficPrint(String message, int messageType, int hostType) {		
		synchronized (lockTraffic) {
			if (viewTraffic) {
				if (viewType[messageType] == true && viewhostType[hostType] == true) {
					messageCountTraffic++;
					jTextArea2.append(message + "\n");
				}
			}
			if (messageCountTraffic > 10000) {
				messageCountTraffic = 0;
				jTextArea2.setText("");
			}
			if (autoScrollDownTraffic) {
				jTextArea2.select(jTextArea2.getHeight() + 10000, 0);
			}
		}
    }

	public void updateLocalRns() {
		synchronized (lockLocal) {
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
	}

	public void updateRemoteRns() {
		synchronized (lockRRn) {
			String[][] obj = App.bn.blueNodeTable.buildRRNGUIObj();
			int rows = remoteRedNodeTableModel.getRowCount();
	        for (int i = 0; i < rows; i++) {
	        	remoteRedNodeTableModel.removeRow(0);
	        }
	        for (int i = 0; i < obj.length; i++) {
	        	remoteRedNodeTableModel.addRow(obj[i]);
	        }		
	        jTable2.setModel(remoteRedNodeTableModel);
	        repaint();
		}		
	}
	
	public void updateBNs() {
		synchronized (lockBn) {
			String[][] obj = App.bn.blueNodeTable.buildBNGUIObj();
			int rows = remoteBlueNodeTableModel.getRowCount();
	        for (int i = 0; i < rows; i++) {
	        	remoteBlueNodeTableModel.removeRow(0);
	        }
	        for (int i = 0; i < obj.length; i++) {
	        	remoteBlueNodeTableModel.addRow(obj[i]);
	        }	
	        jTable3.setModel(remoteBlueNodeTableModel);
	        repaint();
		}
	}
}
