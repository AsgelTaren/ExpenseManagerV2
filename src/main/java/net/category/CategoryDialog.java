package net.category;

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
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.app.App;

@SuppressWarnings("serial")
public class CategoryDialog extends JDialog {

	public CategoryDialog(App app, CategoryListDialog parent, Category category) {
		super(app.getJFrame(), "Account", true);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Account infos"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		JLabel label_name = new JLabel("Name");
		panel.add(label_name, gbc);

		JTextField field_name = new JTextField();
		field_name.setPreferredSize(new Dimension(150, 25));
		field_name.setText(category.getName());
		gbc.gridx = 1;
		panel.add(field_name, gbc);

		add(panel);

		JPanel buttons = new JPanel();
		JButton left = new JButton(category.getId() == -1 ? "Create" : "Save");
		left.setPreferredSize(new Dimension(100, 25));
		left.addActionListener(e -> {
			boolean problem = false;
			if (field_name.getText().equals("")) {
				field_name.setBorder(BorderFactory.createLineBorder(Color.RED));
				problem = true;
			}
			try {
				if (!problem) {
					category.setName(field_name.getText());
					category.applyToDB(app.getDataBase());
					parent.refreshList();
					setVisible(false);
				}
			} catch (SQLException err) {
				err.printStackTrace();
			}
		});
		buttons.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		buttons.add(left, gbc);

		JButton right = new JButton("Cancel");
		right.setPreferredSize(new Dimension(100, 25));
		right.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.gridx = 1;
		buttons.add(right, gbc);

		add(buttons);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
	}
}