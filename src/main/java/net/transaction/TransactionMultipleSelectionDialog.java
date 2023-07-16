package net.transaction;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.app.App;

@SuppressWarnings("serial")
public class TransactionMultipleSelectionDialog extends JDialog {

	private JCheckBox box_name, box_location;
	private JTextField field_name, field_location;
	private JButton apply;
	private Vector<Transaction> selection;

	public TransactionMultipleSelectionDialog(App app, TransactionPanel transPanel, Vector<Transaction> selection) {
		super(app.getJFrame(), "Selection Editor", true);
		this.selection = selection;

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel up = new JPanel();
		up.setBorder(BorderFactory.createTitledBorder("Transactions properties"));
		up.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.LINE_START;

		box_name = new JCheckBox();
		box_name.addActionListener(e -> onCheckBoxUpdate());
		up.add(box_name);
		JLabel label_name = new JLabel("Name");
		label_name.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridx++;
		gbc.weightx = 1;
		up.add(label_name, gbc);
		field_name = new JTextField();
		field_name.setPreferredSize(new Dimension(150, 25));
		field_name.setEnabled(false);
		box_name.addActionListener(e -> field_name.setEnabled(box_name.isSelected()));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 0;
		up.add(field_name, gbc);

		box_location = new JCheckBox();
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		box_location.addActionListener(e -> onCheckBoxUpdate());
		up.add(box_location, gbc);
		JLabel label_location = new JLabel("Location");
		label_location.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.weightx = 1;
		gbc.gridx++;
		up.add(label_location, gbc);
		field_location = new JTextField();
		field_location.setPreferredSize(new Dimension(150, 25));
		field_location.setEnabled(false);
		box_location.addActionListener(e -> field_location.setEnabled(box_location.isSelected()));
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 0;
		up.add(field_location, gbc);

		add(up);

		JPanel down = new JPanel();
		down.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 1;

		apply = new JButton("Apply");
		apply.addActionListener(e -> {
			apply(app);
			setVisible(false);
			dispose();
		});
		apply.setPreferredSize(new Dimension(150, 25));
		down.add(apply, gbc);

		JButton cancel = new JButton("Cancel");
		cancel.setPreferredSize(new Dimension(150, 25));
		cancel.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.gridx++;
		down.add(cancel, gbc);

		add(down);

		onCheckBoxUpdate();
		pack();
		setResizable(false);
		setLocationRelativeTo(null);

	}

	public void onCheckBoxUpdate() {
		apply.setEnabled(box_name.isSelected() || box_location.isSelected());
	}

	public void apply(App app) {
		for (Transaction t : selection) {
			if (box_name.isSelected()) {
				t.setName(field_name.getText());
			}
			if (box_location.isSelected()) {
				t.setLocation(field_location.getText());
			}
			try {
				t.saveIn(app.getDataBase());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}