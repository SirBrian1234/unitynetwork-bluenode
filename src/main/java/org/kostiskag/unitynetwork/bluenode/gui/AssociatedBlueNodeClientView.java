package org.kostiskag.unitynetwork.bluenode.gui;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.function.BooleanSupplier;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;

import org.kostiskag.unitynetwork.bluenode.ModeOfOperation;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
final class AssociatedBlueNodeClientView {

	private JFrame frmAssociatedBlueNode;
	private JLabel lblName;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;

	private final ModeOfOperation mode;
	private final BooleanSupplier isJoinedNetwork;
	private final BlueNode bn;
	private final Runnable releaseBn;

	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public AssociatedBlueNodeClientView(ModeOfOperation mode, BooleanSupplier isJoinedNetwork, BlueNode bn, Runnable releaseBn) {
		if (mode != ModeOfOperation.NETWORK || isJoinedNetwork == null || bn == null) {
				throw new IllegalArgumentException("This window may only be called on network");
		} else {
			this.mode = mode;
			this.isJoinedNetwork = isJoinedNetwork;
			this.bn = bn;
			this.releaseBn = releaseBn;

			initialize();
			lblName.setText(bn.getHostname());
			frmAssociatedBlueNode.setVisible(true);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAssociatedBlueNode = new JFrame();
		frmAssociatedBlueNode.setTitle("Associated Blue Node Client Functions (Debug)");
		frmAssociatedBlueNode.setBounds(100, 100, 668, 545);
		frmAssociatedBlueNode.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmAssociatedBlueNode.getContentPane().setLayout(null);

		JButton btnCheckbluenode = new JButton("checkBlueNode()");
		btnCheckbluenode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check();
			}
		});
		btnCheckbluenode.setBounds(10, 11, 166, 23);
		frmAssociatedBlueNode.getContentPane().add(btnCheckbluenode);

		JButton btnNewButton_9 = new JButton("BlueNode Table release selected Blue Node");
		btnNewButton_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				releaseBn();
			}
		});
		btnNewButton_9.setBounds(10, 391, 292, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_9);

		textField_6 = new JTextField();
		textField_6.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField_6.setBounds(107, 446, 306, 22);
		frmAssociatedBlueNode.getContentPane().add(textField_6);
		textField_6.setColumns(10);

		JLabel lblResponce = new JLabel("Responce");
		lblResponce.setForeground(Color.RED);
		lblResponce.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblResponce.setBounds(24, 449, 71, 16);
		frmAssociatedBlueNode.getContentPane().add(lblResponce);

		JButton btnGivelocalrednodes = new JButton("giveLocalRedNodes()");
		btnGivelocalrednodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				giveLocalRNs();
			}
		});
		btnGivelocalrednodes.setBounds(221, 141, 199, 23);
		frmAssociatedBlueNode.getContentPane().add(btnGivelocalrednodes);

		JButton btnNewButton = new JButton(" uping()");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uping();
			}
		});
		btnNewButton.setBounds(10, 40, 89, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("dping()");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dping();
			}
		});
		btnNewButton_1.setBounds(10, 74, 89, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("removeThisBlueNodesProjection()");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeLocalProject();
			}
		});
		btnNewButton_2.setBounds(10, 108, 225, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("getRemoteRedNodes()");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getRemoteRNs();
			}
		});
		btnNewButton_3.setBounds(10, 142, 199, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_3);
		
		JButton btnNewButton_4 = new JButton("exchangeRedNodes()");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exchangeRns();
			}
		});
		btnNewButton_4.setBounds(10, 176, 199, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_4);
		
		JButton btnNewButton_5 = new JButton("getRedNodeVaddressByHostname(String hostname)");
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getRNVaddr();
			}
		});
		btnNewButton_5.setBounds(10, 210, 335, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_5);
		
		JButton btnNewButton_6 = new JButton("getRedNodeHostnameByVaddress(String vaddress)");
		btnNewButton_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getRNHostname();
			}
		});
		btnNewButton_6.setBounds(10, 244, 335, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_6);
		
		textField = new JTextField();
		textField.setBounds(357, 211, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setBounds(357, 245, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnNewButton_7 = new JButton("removeRedNodeProjectionByHn(String hostname)");
		btnNewButton_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeRNprojectByHn();
			}
		});
		btnNewButton_7.setBounds(10, 278, 335, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_7);
		
		textField_2 = new JTextField();
		textField_2.setBounds(357, 279, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		JButton btnRemoverednodeprojectionbyvaddrstringVaddress = new JButton("removeRedNodeProjectionByVaddr(String vaddress)");
		btnRemoverednodeprojectionbyvaddrstringVaddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeRNprojectByVaddr();
			}
		});
		btnRemoverednodeprojectionbyvaddrstringVaddress.setBounds(10, 312, 335, 23);
		frmAssociatedBlueNode.getContentPane().add(btnRemoverednodeprojectionbyvaddrstringVaddress);
		
		textField_3 = new JTextField();
		textField_3.setBounds(357, 313, 215, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_3);
		textField_3.setColumns(10);
		
		JButton btnNewButton_8 = new JButton("feedReturnRoute(String hostname, String vaddress)");
		btnNewButton_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				feedRetrunRoute();
			}
		});
		btnNewButton_8.setBounds(10, 346, 335, 23);
		frmAssociatedBlueNode.getContentPane().add(btnNewButton_8);
		
		textField_4 = new JTextField();
		textField_4.setBounds(357, 347, 119, 20);
		frmAssociatedBlueNode.getContentPane().add(textField_4);
		textField_4.setColumns(10);
		
		textField_5 = new JTextField();
		textField_5.setBounds(488, 346, 119, 20);
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

	private void check() {
		try {
        	BlueNodeClient cl = new BlueNodeClient(bn);
			if (cl.checkBlueNode()) {
				textField_6.setText(bn.getHostname()+"is active.");
			} else {
				textField_6.setText(bn.getHostname()+"is offline.");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	private void uping() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			textField_6.setText(cl.uPing()+"");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void dping() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			textField_6.setText(cl.dPing()+"");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void removeLocalProject() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.removeThisBlueNodesProjection();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getRemoteRNs() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.getRemoteRedNodes();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	private void giveLocalRNs() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.giveLocalRedNodes();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	private void exchangeRns() {
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.exchangeRedNodes();
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}

	private void feedRetrunRoute() {
		String hostname = textField_4.getText();
		String vaddress = textField_5.getText();
		
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.feedReturnRoute(hostname, vaddress);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void removeRNprojectByHn() {
		String hostname = textField_2.getText();
		
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.removeRedNodeProjectionByHn(hostname);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void removeRNprojectByVaddr() {
		String vaddr = textField_3.getText();
		
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			cl.removeRedNodeProjectionByVaddr(vaddr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getRNHostname() {
		String vaddress = textField_1.getText();
		
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			textField_6.setText(cl.getRedNodeHostnameByVaddress(VirtualAddress.valueOf(vaddress)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void getRNVaddr() {
		String hostname = textField.getText();
		
		try {
			BlueNodeClient cl = new BlueNodeClient(bn);
			String answer = cl.getRedNodeVaddressByHostname(hostname);
			System.out.println(answer);
			textField_6.setText(answer);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void releaseBn() {
		try {
			releaseBn.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
