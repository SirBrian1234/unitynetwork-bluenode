package kostiskag.unitynetwork.bluenode.GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTextField;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AssociatedBlueNodeClientGUI {

	private JFrame frmAssociatedBlueNode;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private final String name;
	private final BlueNodeInstance bn;
	private JLabel lblName;

	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public AssociatedBlueNodeClientGUI(String name) throws Exception {
		this.name = name;
		this.bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(name);
		initialize();
		lblName.setText(name);
		
		JButton btnCheckbluenode = new JButton("checkBlueNode()");
		btnCheckbluenode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
		        	BlueNodeClient cl = new BlueNodeClient(bn);
					if (cl.checkBlueNode()) {
						App.bn.ConsolePrint(bn.getName()+"is active.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnCheckbluenode.setBounds(10, 11, 119, 23);
		frmAssociatedBlueNode.getContentPane().add(btnCheckbluenode);
		
		JButton btnNewButton_9 = new JButton("BlueNode Table Release BlueNode");
		btnNewButton_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				releaseBn();
			}
		});
		btnNewButton_9.setBounds(10, 391, 215, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_9);
	}

	protected void releaseBn() {
		try {
			App.bn.blueNodesTable.releaseBn(name);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setVisible() {
		frmAssociatedBlueNode.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAssociatedBlueNode = new JFrame();
		frmAssociatedBlueNode.setTitle("Associated Blue Node Client Functions (Debug)");
		frmAssociatedBlueNode.setBounds(100, 100, 668, 463);
		frmAssociatedBlueNode.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmAssociatedBlueNode.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton(" UPing()");
		btnNewButton.setBounds(10, 40, 89, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("DPing()");
		btnNewButton_1.setBounds(10, 74, 89, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("removeThisBlueNodesProjection()");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeLocalProject();
			}
		});
		btnNewButton_2.setBounds(10, 108, 199, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("getRemoteRedNodes()");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BlueNodeClient cl = new BlueNodeClient(bn);
					cl.getRemoteRedNodes();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnNewButton_3.setBounds(10, 142, 143, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_3);
		
		JButton btnNewButton_4 = new JButton("exchangeRedNodes()");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BlueNodeClient cl = new BlueNodeClient(bn);
					cl.exchangeRedNodes();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnNewButton_4.setBounds(10, 176, 143, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_4);
		
		JButton btnNewButton_5 = new JButton("getRedNodeVaddressByHostname(String hostname)");
		btnNewButton_5.setBounds(10, 210, 283, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_5);
		
		JButton btnNewButton_6 = new JButton("getRedNodeHostnameByVaddress(String vaddress)");
		btnNewButton_6.setBounds(10, 244, 283, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_6);
		
		textField = new JTextField();
		textField.setBounds(303, 211, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(303, 245, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnNewButton_7 = new JButton("removeRedNodeProjectionByHn(String hostname)");
		btnNewButton_7.setBounds(10, 278, 283, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_7);
		
		textField_2 = new JTextField();
		textField_2.setBounds(303, 279, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		JButton btnRemoverednodeprojectionbyvaddrstringVaddress = new JButton("removeRedNodeProjectionByVaddr(String vaddress)");
		btnRemoverednodeprojectionbyvaddrstringVaddress.setBounds(10, 312, 283, 23);
		frmAssociatedBlueNode.getContentPane().add(btnRemoverednodeprojectionbyvaddrstringVaddress);
		
		textField_3 = new JTextField();
		textField_3.setBounds(303, 313, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		JButton btnNewButton_8 = new JButton("feedReturnRoute(String hostname, String vaddress)");
		btnNewButton_8.setBounds(10, 346, 323, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_8);
		
		textField_4 = new JTextField();
		textField_4.setBounds(343, 347, 156, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_4);
		textField_4.setColumns(10);
		
		textField_5 = new JTextField();
		textField_5.setBounds(509, 347, 119, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_5);
		textField_5.setColumns(10);
		
		JLabel lblPleaseFillIn = new JLabel("Please fill in the respective fields for each option and press the button");
		lblPleaseFillIn.setBounds(293, 49, 335, 14);
		frmAssociatedBlueNode.getContentPane().add(lblPleaseFillIn);
		
		lblName = new JLabel("Name");
		lblName.setForeground(new Color(153, 51, 0));
		lblName.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblName.setBounds(293, 13, 335, 14);
		frmAssociatedBlueNode.getContentPane().add(lblName);
	}
	
	protected void removeLocalProject() {		
    	
	}
}
