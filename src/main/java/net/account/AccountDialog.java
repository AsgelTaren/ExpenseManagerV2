package net.account;

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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.app.App;

@SuppressWarnings("serial")
public class AccountDialog extends JDialog {

	public AccountDialog(App app, AccountListDialog parent, Account account) {
		super(app.getJFrame(), app.getLangAtlas().getText("menu.account"), true);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(app.getLangAtlas().getText("menu.account.infos")));
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
		field_name.setText(account.getName());
		gbc.gridx = 1;
		panel.add(field_name, gbc);

		JLabel label_balance = new JLabel("Balance");
		gbc.gridx = 0;
		gbc.gridy++;
		panel.add(label_balance, gbc);

		SpinnerNumberModel model = new SpinnerNumberModel();
		model.setValue((double) account.getBalance());
		model.setMaximum(null);
		model.setMinimum(null);
		model.setStepSize(0.01);
		JSpinner field_balance = new JSpinner(model);
		field_balance.setPreferredSize(new Dimension(150, 25));
		gbc.gridx++;
		panel.add(field_balance, gbc);

		add(panel);

		JPanel buttons = new JPanel();
		JButton left = new JButton(account.getId() == -1 ? app.getLangAtlas().getText("menu.create")
				: app.getLangAtlas().getText("menu.save"));
		left.setPreferredSize(new Dimension(100, 25));
		left.addActionListener(e -> {
			boolean problem = false;
			if (field_name.getText().equals("")) {
				field_name.setBorder(BorderFactory.createLineBorder(Color.RED));
				problem = true;
			}
			try {
				if (!problem) {
					account.setName(field_name.getText());
					account.setBalance(((Number) field_balance.getValue()).floatValue());
					account.applyToDB(app.getDataBase());
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

		JButton right = new JButton(app.getLangAtlas().getText("menu.cancel"));
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