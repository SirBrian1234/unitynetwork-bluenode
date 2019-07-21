package org.kostiskag.unitynetwork.bluenode.gui;

import java.util.Map;
import java.util.HashMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;

import org.kostiskag.unitynetwork.common.entry.NodeType;

import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 * This is the main bluenode window. 
 * There should be only one object from this class.
 * 
 * @author Konstantinos Kagiampakis
 */
public final class MainWindow extends JFrame {
    
    private static MainWindow MAIN_WINDOW;
    private static final int TEXT_AREA_MAX_MESSAGE_VALUE = 10000;
    private static final long serialVersionUID = 5505085647328297106L;

    private final DefaultTableModel localRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Hostname", "Virtual Address", "Physical Address", "Auth Port", "Send Port", "Receive Port"});
    private final DefaultTableModel remoteRedNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Hostname", "Virtual Address", "Blue Node Name", "Last Checked"});
    private final DefaultTableModel remoteBlueNodeTableModel = new DefaultTableModel(new String[][]{}, new String[]{"Name", "Is a Server", "Physical Address", "Auth Port", "Send Port", "Receive Port", "Last Checked"});

    //synchronised locks
    private final Object lockLocal = new Object();
    private final Object lockBn = new Object();
    private final Object lockRRn = new Object();
    private final Object lockTraffic = new Object();
    private final Object lockConsole = new Object();

    //instance data
    private final MainWindowPrefs prefs;
    private boolean autoScrollDownTraffic = true;
    private boolean autoScrollDownConsole = true;
    private boolean viewTraffic = true;
    private int messageCountConsole = 0;
    private int messageCountTraffic = 0;
    private final Map<AppLogger.MessageType, Boolean> viewType = new HashMap<>();
    private final Map<NodeType, Boolean> viewhostType = new HashMap<>();
	
    private About about;

    public static class MainWindowPrefs {
        final String bluenodeName;
        final int authPort;
        final int maxRednodeEntries;
        final int startPort;
        final int endPort;
        final boolean networkMode;
        final boolean isTrackerKeySet;
        final boolean useListMode;

        public MainWindowPrefs(String bluenodeName, int authPort, int maxRednodeEntries, int startPort, int endPort, boolean networkMode, boolean isTrackerKeySet, boolean useListMode) {
            this.bluenodeName = bluenodeName;
            this.authPort = authPort;
            this.maxRednodeEntries = maxRednodeEntries;
            this.startPort = startPort;
            this.endPort = endPort;
            this.networkMode = networkMode;
            this.isTrackerKeySet = isTrackerKeySet;
            this.useListMode = useListMode;
        }
    }
    
    public static MainWindow newInstance(MainWindowPrefs prefs) {
        if (MAIN_WINDOW == null) {
            MAIN_WINDOW = new MainWindow(prefs);
        }
        return MAIN_WINDOW;
    }

    public static MainWindow getInstance() {
        return MAIN_WINDOW;
    }

    private MainWindow(MainWindowPrefs prefs) {
        this.prefs = prefs;
        for (AppLogger.MessageType m : AppLogger.MessageType.values()) {
            viewType.put(m, true);    
        }
        for (NodeType n : NodeType.values()) {
            viewhostType.put(n, true);
        }
        
        initComponents();
        bluenodeNameTextField.setText(prefs.bluenodeName);
        authPortTextField.setText("" + prefs.authPort);
        maxEntriesTextField.setText("" + prefs.maxRednodeEntries);
        portRangeTextField.setText("" + prefs.startPort + "-" + prefs.endPort);

        if (prefs.networkMode) {
            txtOpMode.setText("Network");
            if (prefs.isTrackerKeySet) {
                uploadPublicKeyButton.setEnabled(false);
            }
        } else if (prefs.useListMode){
            txtOpMode.setText("Standalone/List");
        } else {
            txtOpMode.setText("Standalone/Plain");
            jTabbedPane1.remove(2);
            btnCollectTrackersPublic.setEnabled(false);
            uploadPublicKeyButton.setEnabled(false);
        }

        this.setVisible(true);
    }   

    private void initComponents() {

        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        jPanel4 = new JPanel();
        jPanel5 = new JPanel();
        jPanel6 = new JPanel();
        jPanel7 = new JPanel();
        jPanel8 = new JPanel();
        jPanel9 = new JPanel();
        jPanel10 = new JPanel();
        localRednodeTable = new JTable();
        localRednodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        remoteRednodeTable = new JTable();
        remoteRednodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        remoteBluenodeTable = new JTable();
        remoteBluenodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jMenuItem1 = new JMenuItem();
        jCheckBoxMenuItem1 = new JCheckBoxMenuItem();
        jMenuItem5 = new JMenuItem();
        buttonGroup1 = new ButtonGroup();
        jTabbedPane1 = new JTabbedPane();
        jScrollPane5 = new JScrollPane();
        jScrollPane6 = new JScrollPane();
        authPortTextField = new JTextField();
        bluenodeNameTextField = new JTextField();
        bluenodeNameTextField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bluenodeNameTextField.setText("BlueNode");
        maxEntriesTextField = new JTextField();
        portRangeTextField = new JTextField();
        oneUserConnectedCheckBox = new JCheckBox();
        receivedFromLocalRnCheckBox = new JCheckBox();
        sentDataToRnCheckBox = new JCheckBox();
        sentDataToBnCheckBox = new JCheckBox();
        receivedBnDataCheckBox = new JCheckBox();
        authServiceCheckBox = new JCheckBox();
        jToggleButton1 = new JToggleButton();
        messageVerboseTextArea = new JTextArea();
        trafficVerboseTextArea = new JTextArea();
        showkeepAliveCheckBox = new JCheckBox();
        showRoutingCheckBox = new JCheckBox();
        shpwAcksCheckBox = new JCheckBox();
        jSeparator1 = new JSeparator();
        optViewOnlyRnsRadioButton = new JRadioButton();
        optViewOnlyBnsRadioButton = new JRadioButton();
        optViewBothRadioButton = new JRadioButton();
        showPingsCheckBox = new JCheckBox();
        jCheckBox12 = new JCheckBox();
        jScrollPane2 = new JScrollPane();
        jButton1 = new JButton();
        jButton2 = new JButton();
        refreshLocalRnsTableButton = new JButton();
        refreshLocalRnsTableButton.setToolTipText("Refresh the GUI representation to match the inner data.");
        jButton5 = new JButton();
        jButton5.setToolTipText("Refresh the GUI representation to match the inner data.");
        jButton7 = new JButton();
        jButton7.setToolTipText("Refresh the GUI representation to match the inner data.");
        jButton10 = new JButton();
        jButton10.setBackground(new Color(204, 51, 0));
        jScrollPane1 = new JScrollPane();
        jScrollPane3 = new JScrollPane();

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
            if(PromptResult==JOptionPane.YES_OPTION) {
                Bluenode.getInstance().terminate();
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

        messageVerboseTextArea.setColumns(20);
        messageVerboseTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        messageVerboseTextArea.setRows(5);
        jScrollPane5.setViewportView(messageVerboseTextArea);
        
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

        jLabel2.setText("Auth Port (TCP)");
        jLabel1.setText("Hostname");
        jLabel4.setText("Max Number of RN Hosts");
        jLabel5.setText("UDP Port Range");

        authPortTextField.setEditable(false);
        bluenodeNameTextField.setEditable(false);
        maxEntriesTextField.setEditable(false);
        portRangeTextField.setEditable(false);
        
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
        
        uploadPublicKeyButton = new JButton("Manage BlueNode's Public Key");
        uploadPublicKeyButton.addActionListener(new ActionListener() {
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
        				.addComponent(uploadPublicKeyButton, GroupLayout.PREFERRED_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel1)
        				.addComponent(bluenodeNameTextField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(lblEchoAddress)
        				.addComponent(textField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel2)
        				.addComponent(authPortTextField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel5)
        				.addComponent(portRangeTextField, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
        				.addComponent(btnCollectTrackersPublic)
        				.addComponent(maxEntriesTextField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
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
        			.addComponent(bluenodeNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(lblEchoAddress)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel2)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(authPortTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel5)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(portRangeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jLabel4)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(maxEntriesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(13)
        			.addComponent(btnCollectTrackersPublic)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(uploadPublicKeyButton)
        			.addPreferredGap(ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
        			.addComponent(btnNewButton)
        			.addContainerGap())
        );
        jPanel3.setLayout(jPanel3Layout);

        jLabel1.getAccessibleContext().setAccessibleName("jLabel1");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Info Variables"));

        oneUserConnectedCheckBox.setText("At least one user was connected");
        oneUserConnectedCheckBox.setEnabled(false);

        receivedFromLocalRnCheckBox.setText("Has downloaded data from a local Red Node");
        receivedFromLocalRnCheckBox.setActionCommand("hasDownloadedContentFromBoundHosts");
        receivedFromLocalRnCheckBox.setEnabled(false);

        sentDataToRnCheckBox.setText("Has uploaded data to a local Red Node");
        sentDataToRnCheckBox.setEnabled(false);

        sentDataToBnCheckBox.setText("Has uploaded data to another Blue Node");
        sentDataToBnCheckBox.setEnabled(false);

        receivedBnDataCheckBox.setText("Has downloaded data from another Blue Node");
        receivedBnDataCheckBox.setEnabled(false);

        authServiceCheckBox.setText("Is Auth Service Online");
        authServiceCheckBox.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sentDataToRnCheckBox)
                            .addComponent(sentDataToBnCheckBox))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(receivedBnDataCheckBox)
                            .addComponent(receivedFromLocalRnCheckBox)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(authServiceCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(oneUserConnectedCheckBox)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oneUserConnectedCheckBox)
                    .addComponent(authServiceCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sentDataToRnCheckBox)
                    .addComponent(receivedFromLocalRnCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sentDataToBnCheckBox)
                    .addComponent(receivedBnDataCheckBox))
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

        trafficVerboseTextArea.setColumns(20);
        trafficVerboseTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        trafficVerboseTextArea.setRows(5);
        jScrollPane6.setViewportView(trafficVerboseTextArea);

        showkeepAliveCheckBox.setSelected(true);
        showkeepAliveCheckBox.setText("view keep alives");
        showkeepAliveCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepAliveCheckBoxActionPerformed(evt);
            }
        });

        showRoutingCheckBox.setSelected(true);
        showRoutingCheckBox.setText("view routing");
        showRoutingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRoutingCheckBoxActionPerformed(evt);
            }
        });

        shpwAcksCheckBox.setSelected(true);
        shpwAcksCheckBox.setText("view acks");
        shpwAcksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shpwAcksCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(optViewOnlyRnsRadioButton);
        optViewOnlyRnsRadioButton.setText("Only view Local Red Nodes");
        optViewOnlyRnsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optViewOnlyRnsRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(optViewOnlyBnsRadioButton);
        optViewOnlyBnsRadioButton.setText("Only View Blue Nodes");
        optViewOnlyBnsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optViewOnlyBnsRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(optViewBothRadioButton);
        optViewBothRadioButton.setSelected(true);
        optViewBothRadioButton.setText("View Both LRNs and BNs");
        optViewBothRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optViewBothRadioButtonRadioButtonActionPerformed(evt);
            }
        });

        showPingsCheckBox.setSelected(true);
        showPingsCheckBox.setText("view pings");
        showPingsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPingsCheckBoxActionPerformed(evt);
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
                        .addComponent(optViewBothRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optViewOnlyRnsRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optViewOnlyBnsRadioButton)
                        .addGap(48, 48, 48))
                    .addComponent(jScrollPane6)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(showkeepAliveCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showPingsCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(shpwAcksCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showRoutingCheckBox))
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
                    .addComponent(optViewOnlyRnsRadioButton)
                    .addComponent(optViewOnlyBnsRadioButton)
                    .addComponent(optViewBothRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showkeepAliveCheckBox)
                    .addComponent(showRoutingCheckBox)
                    .addComponent(shpwAcksCheckBox)
                    .addComponent(showPingsCheckBox))
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

        localRednodeTable.setModel(localRedNodeTableModel);
        jScrollPane2.setViewportView(localRednodeTable);

        refreshLocalRnsTableButton.setText("Refresh Table");
        refreshLocalRnsTableButton.addActionListener(new java.awt.event.ActionListener() {
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
        				.addComponent(refreshLocalRnsTableButton))
        			.addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
        	jPanel6Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel6Layout.createSequentialGroup()
        			.addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(refreshLocalRnsTableButton))
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

        remoteRednodeTable.setModel(remoteRedNodeTableModel);
        jScrollPane1.setViewportView(remoteRednodeTable);

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

        remoteBluenodeTable.setModel(remoteBlueNodeTableModel);
        jScrollPane3.setViewportView(remoteBluenodeTable);

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

        this.setTitle("Blue Node");
        localRednodeTable.setDefaultEditor(Object.class, null);
        remoteRednodeTable.setDefaultEditor(Object.class, null);
        remoteBluenodeTable.setDefaultEditor(Object.class, null);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        trafficVerboseTextArea.setText("");
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        messageVerboseTextArea.setText("");
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        updateRemoteRns();
    }

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        updateBNs();
    }

    private void keepAliveCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        viewType.put(AppLogger.MessageType.KEEP_ALIVE, showkeepAliveCheckBox.isSelected());
    }

    private void showPingsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        viewType.put(AppLogger.MessageType.PINGS, showPingsCheckBox.isSelected());
    }

    private void shpwAcksCheckBoxActionPerformed(ActionEvent evt) {
        viewType.put(AppLogger.MessageType.ACKS, shpwAcksCheckBox.isSelected());
    }

    private void showRoutingCheckBoxActionPerformed(ActionEvent evt) {
        viewType.put(AppLogger.MessageType.ROUTING, showRoutingCheckBox.isSelected());
    }

    private void optViewBothRadioButtonRadioButtonActionPerformed(ActionEvent evt) {
        if (optViewBothRadioButton.isSelected()) {
            viewhostType.put(NodeType.REDNODE, true);
            viewhostType.put(NodeType.BLUENODE, true);
        }
    }

    private void optViewOnlyRnsRadioButtonActionPerformed(ActionEvent evt) {
        if (optViewOnlyRnsRadioButton.isSelected()) {
            viewhostType.put(NodeType.REDNODE, true);
            viewhostType.put(NodeType.BLUENODE, false);
        }
    }

    private void optViewOnlyBnsRadioButtonActionPerformed(ActionEvent evt) {
        if (optViewOnlyBnsRadioButton.isSelected()) {
            viewhostType.put(NodeType.REDNODE, false);
            viewhostType.put(NodeType.BLUENODE, true);
        }
    }

    private void jCheckBox12ActionPerformed(ActionEvent evt) {
        autoScrollDownTraffic = jCheckBox12.isSelected();
    }

    private void trackerRNLookup(ActionEvent evt) {
        new TrackerLookupView().setVisible(true);
    }

    private void openNewBnWindow() {
        new NonAssociatedBlueNodeClientView().setVisible(true);
    }

    private void openAssosiatedWindow() {
		try {
			int row = remoteBluenodeTable.getSelectedRow();
			if (row >= 0) {
				try {
					new AssociatedBlueNodeClientView((String) remoteBluenodeTable.getValueAt(row, 0)).setVisible();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex){

		}
	}

	private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jToggleButton1.isSelected()) {
            viewTraffic = true;
        } else {
            viewTraffic = false;
        }
    }

    public void enableUploadPublicKey() {
    	uploadPublicKeyButton.setEnabled(true);
    }

    public void setEchoIpAddress(String addr) {
    	textField.setText(addr);
    }

    public void setAuthServiceAsEnabled() {
        authServiceCheckBox.setSelected(true);
    }

    public void setOneUserAsConnected() {
        oneUserConnectedCheckBox.setSelected(true);
    }

    public void setReceivedLocalRnData() {
        receivedFromLocalRnCheckBox.setSelected(true);
    }

    public void setReceivedBnData() {
        receivedBnDataCheckBox.setSelected(true);
    }

    public void setSentDataToRn() {
        sentDataToRnCheckBox.setSelected(true);
    }

    public void setSentDataToBn() {
        sentDataToBnCheckBox.setSelected(true);
    }

    public void consolePrint(String message) {
    	synchronized (lockConsole) {
    		messageCountConsole++;
    		messageVerboseTextArea.append(message + "\n");

    		if (messageCountConsole > TEXT_AREA_MAX_MESSAGE_VALUE) {
				messageCountConsole = 0;
				messageVerboseTextArea.setText("");
			}
			if (autoScrollDownConsole) {
				messageVerboseTextArea.select(messageVerboseTextArea.getHeight() + TEXT_AREA_MAX_MESSAGE_VALUE, 0);
			}
		}
    }

    public void trafficPrint(String message, AppLogger.MessageType messageType, NodeType hostType) {
		synchronized (lockTraffic) {
			if (viewTraffic && viewType.get(messageType) && viewhostType.get(hostType)) {
					messageCountTraffic++;
					trafficVerboseTextArea.append(message + "\n");
			}
			if (messageCountTraffic > TEXT_AREA_MAX_MESSAGE_VALUE) {
				messageCountTraffic = 0;
				trafficVerboseTextArea.setText("");
			}
			if (autoScrollDownTraffic) {
				trafficVerboseTextArea.select(trafficVerboseTextArea.getHeight() + TEXT_AREA_MAX_MESSAGE_VALUE, 0);
			}
		}
    }

    public void updateLocalRns() {
        String[][] guiObj = LocalRedNodeTable.getInstance().buildGUIObj();
        updateLocalRns(guiObj);
    }

    public void updateLocalRns(String[][] guiObj) {
        synchronized (lockLocal) {
            for (int i = 0; i < localRedNodeTableModel.getRowCount(); i++) {
                localRedNodeTableModel.removeRow(0);
            }
            for (int i = 0; i < guiObj.length; i++) {
                localRedNodeTableModel.addRow(guiObj[i]);
            }
            localRednodeTable.setModel(localRedNodeTableModel);
            repaint();
        }
    }

    public void updateRemoteRns() {
        String[][] guiObj = Bluenode.getInstance().blueNodeTable.buildRRNGUIObj();
        updateRemoteRns(guiObj);
    }

    public void updateRemoteRns(String[][] guiObj) {
        synchronized (lockRRn) {
            for (int i = 0; i < remoteRedNodeTableModel.getRowCount(); i++) {
                remoteRedNodeTableModel.removeRow(0);
            }
            for (int i = 0; i < guiObj.length; i++) {
                remoteRedNodeTableModel.addRow(guiObj[i]);
            }
            remoteRednodeTable.setModel(remoteRedNodeTableModel);
            repaint();
        }
    }

    public void updateBNs() {
        String[][] guiObj = Bluenode.getInstance().blueNodeTable.buildBNGUIObj();
        updateBNs(guiObj);
    }

    public void updateBNs(String[][] guiObj) {
        synchronized (lockBn) {
            for (int i = 0; i < remoteBlueNodeTableModel.getRowCount(); i++) {
                remoteBlueNodeTableModel.removeRow(0);
            }
            for (int i = 0; i < guiObj.length; i++) {
                remoteBlueNodeTableModel.addRow(guiObj[i]);
            }
            remoteBluenodeTable.setModel(remoteBlueNodeTableModel);
            repaint();
        }
    }

    private JPanel jPanel1;
    private JPanel jPanel10;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JPanel jPanel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane5;
    private JScrollPane jScrollPane6;
    private JSeparator jSeparator1;
    private JTabbedPane jTabbedPane1;
    private JMenuItem jMenuItem1;
    private JMenuItem jMenuItem5;
    private JTable localRednodeTable;
    private JTable remoteRednodeTable;
    private JTable remoteBluenodeTable;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel lblEchoAddress;
    private ButtonGroup buttonGroup1;
    private JToggleButton jToggleButton1;
    private JButton jButton1;
    private JButton jButton10;
    private JButton jButton2;
    private JButton refreshLocalRnsTableButton;
    private JButton jButton5;
    private JButton jButton7;
    private JButton btnNewButton;
    private JButton btnCollectTrackersPublic;
    private JButton uploadPublicKeyButton;
    private JButton btnOpenBlueNode;
    private JRadioButton optViewOnlyRnsRadioButton;
    private JRadioButton optViewBothRadioButton;
    private JRadioButton optViewOnlyBnsRadioButton;
    private JCheckBoxMenuItem jCheckBoxMenuItem1;
    private JCheckBox checkBox;
    private JCheckBox shpwAcksCheckBox;
    private JCheckBox showPingsCheckBox;
    private JCheckBox jCheckBox12;
    private JCheckBox oneUserConnectedCheckBox;
    private JCheckBox receivedFromLocalRnCheckBox;
    private JCheckBox sentDataToRnCheckBox;
    private JCheckBox showkeepAliveCheckBox;
    private JCheckBox sentDataToBnCheckBox;
    private JCheckBox receivedBnDataCheckBox;
    private JCheckBox authServiceCheckBox;
    private JCheckBox showRoutingCheckBox;
    private JTextArea messageVerboseTextArea;
    private JTextArea trafficVerboseTextArea;
    private JTextField bluenodeNameTextField;
    private JTextField authPortTextField;
    private JTextField maxEntriesTextField;
    private JTextField portRangeTextField;
    private JTextField textField;
    private JTextField txtOpMode;
}
