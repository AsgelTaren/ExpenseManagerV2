package net.account;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.app.App;

@SuppressWarnings("serial")
public class AccountListDialog extends JDialog {

	private JList<Account> list;
	private AccountListModel model;
	private JButton edit, remove;
	private JTextField field_sum;
	private HashMap<Integer, Integer> numberOfTrans;

	public AccountListDialog(App app) {
		super(app.getJFrame(), "Accounts list", true);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new GridBagLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder("Accounts"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;

		model = new AccountListModel(app);
		model.query();
		list = new JList<>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		listPanel.add(new JScrollPane(list), gbc);

		// Number of transactions
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery(
					"select count(transactions.id) as \"COUNT_TRANS\",accounts.id as \"ID\" from accounts left join transactions on accounts.id = transactions.account group by accounts.id;");
			numberOfTrans = new HashMap<>();
			while (set.next()) {
				numberOfTrans.put(set.getInt("ID"), set.getInt("COUNT_TRANS"));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		JButton add = new JButton("Add");
		add.addActionListener(e -> {
			AccountDialog dialog = new AccountDialog(app, this, new Account(-1, ""));
			dialog.setVisible(true);
		});
		gbc.gridy = 1;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		listPanel.add(add, gbc);

		edit = new JButton("Edit");
		edit.setEnabled(false);
		edit.addActionListener(e -> {
			AccountDialog dialog = new AccountDialog(app, this, model.getElementAt(list.getSelectedIndex()));
			dialog.setVisible(true);
		});
		gbc.gridx = 1;
		listPanel.add(edit, gbc);

		remove = new JButton("Remove");
		remove.addActionListener(e -> {
			int choice = JOptionPane.showConfirmDialog(this, "Do you really want to delete this account?", "Warning",
					JOptionPane.YES_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				Account target = model.getElementAt(list.getSelectedIndex());
				try {
					target.deleteFromDB(app.getDataBase());
					refreshList();
					list.clearSelection();
				} catch (SQLException err) {
					err.printStackTrace();
				}
			}
		});
		remove.setEnabled(false);
		list.getSelectionModel().addListSelectionListener(e -> {
			listSelection();
		});
		gbc.gridx = 2;
		listPanel.add(remove, gbc);

		add(listPanel);

		JPanel accountPanel = new JPanel();
		accountPanel.setLayout(new GridBagLayout());
		accountPanel.setBorder(BorderFactory.createTitledBorder("Account informations"));
		JLabel label_sum = new JLabel("Number of transactions");
		label_sum.setPreferredSize(new Dimension(150, 25));
		label_sum.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		accountPanel.add(label_sum, gbc);

		field_sum = new JTextField();
		field_sum.setEditable(false);
		field_sum.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		accountPanel.add(field_sum, gbc);

		add(accountPanel);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);
	}

	private void listSelection() {
		boolean temp = list.getSelectedIndex() > -1;
		remove.setEnabled(temp);
		edit.setEnabled(temp);

		Account target = list.getSelectedIndex() > -1 ? model.getElementAt(list.getSelectedIndex()) : null;
		field_sum.setText(target != null ? numberOfTrans.get(target.getId()) + "" : "");
		field_sum.repaint();
	}

	public void refreshList() {
		model.query();
		list.revalidate();
		list.repaint();
	}

}