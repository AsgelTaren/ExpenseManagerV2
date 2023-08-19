package net.transaction.folder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.app.App;

@SuppressWarnings("serial")
public class FolderDialog extends JDialog {

	public FolderDialog(App app, FolderPanel panel, Folder target) {
		super(app.getJFrame(), "Folder", true);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel up = new JPanel();
		up.setBorder(BorderFactory.createTitledBorder("Folder informations"));
		up.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 5, 2, 5);

		JLabel label_name = new JLabel("Name");
		label_name.setPreferredSize(new Dimension(150, 25));
		up.add(label_name, gbc);

		JTextField field_name = new JTextField();
		field_name.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_name, gbc);

		JLabel label_id = new JLabel("ID");
		label_id.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(label_id, gbc);

		JTextField field_id = new JTextField();
		field_id.setEnabled(false);
		field_id.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_id, gbc);

		add(up);

		JPanel down = new JPanel();
		down.setLayout(new GridBagLayout());
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.insets = new Insets(2, 2, 2, 2);

		JButton apply = new JButton(target == null ? "Create" : "Save");
		apply.addActionListener(e -> {
			if (!checkField(field_name)) {
				return;
			}
			Folder folder = target;
			if (folder == null) {
				folder = new Folder(-1, field_name.getText());
			} else {
				folder.setName(field_name.getText());
			}
			try {
				folder.updateInto(app.getDataBase());
			} catch (SQLException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(app.getJFrame(), "Unable to save folder to database", "SQL Error",
						JOptionPane.ERROR_MESSAGE);
			}
			setVisible(false);
			dispose();
			panel.refreshList();
			panel.refreshTable();
		});
		down.add(apply, gbc);

		JButton cancel = new JButton("Cancel");
		gbc.gridx++;
		down.add(cancel, gbc);

		add(down);

		if (target != null) {
			field_name.setText(target.getName());
			field_id.setText(target.getID() + "");
		}

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private boolean checkField(JTextField field) {
		if (field.getText() == null || field.getText().equals("")) {
			field.setBorder(BorderFactory.createLineBorder(Color.RED));
			return false;
		}
		return true;
	}
}
