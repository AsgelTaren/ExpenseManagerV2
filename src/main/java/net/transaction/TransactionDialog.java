package net.transaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.account.Account;
import net.app.App;
import net.app.Utils;
import net.category.Category;

@SuppressWarnings("serial")
public class TransactionDialog extends JDialog {

	public TransactionDialog(App app, TransactionPanel transactionPanel, Transaction transaction) {
		super(app.getJFrame(), app.getLangAtlas().getText("menu.transaction"), true);

		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel up = new JPanel();
		up.setBorder(BorderFactory.createTitledBorder("Transaction informations"));
		up.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Name
		JLabel label_name = new JLabel("Name");
		label_name.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		up.add(label_name, gbc);
		JTextField field_name = new JTextField();
		field_name.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_name, gbc);

		// Location
		JLabel label_location = new JLabel("Location");
		label_location.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy++;
		up.add(label_location, gbc);
		JTextField field_location = new JTextField();
		field_location.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_location, gbc);

		// Category
		JLabel label_category = new JLabel("Category");
		label_category.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy++;
		gbc.gridwidth = 1;
		up.add(label_category, gbc);
		Vector<Category> categories = null;
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from categories;");
			categories = new Vector<>(set.getFetchSize());
			while (set.next()) {
				categories.add(new Category(set.getInt("id"), set.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JComboBox<Category> field_category = new JComboBox<>(categories);
		field_category.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_category, gbc);

		// Account
		JLabel label_account = new JLabel("Account");
		label_account.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy = 4;
		gbc.gridx = 1;
		up.add(label_account, gbc);
		Vector<Account> accounts = null;
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from accounts;");
			accounts = new Vector<>(set.getFetchSize());
			while (set.next()) {
				accounts.add(new Account(set.getInt("id"), set.getString("name"), set.getFloat("balance")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JComboBox<Account> field_account = new JComboBox<>(accounts);
		field_account.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_account, gbc);

		// Date of creation
		JLabel label_date_creation = new JLabel("Date of Creation");
		label_date_creation.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy++;
		gbc.gridx = 0;
		up.add(label_date_creation, gbc);
		JFormattedTextField field_date_creation = new JFormattedTextField(Transaction.DATE_FORMAT);
		field_date_creation.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_date_creation, gbc);

		// Date of application
		JLabel label_date_application = new JLabel("Date of Application");
		label_date_application.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy = 6;
		gbc.gridx = 1;
		up.add(label_date_application, gbc);
		JFormattedTextField field_date_application = new JFormattedTextField(Transaction.DATE_FORMAT);
		field_date_creation.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_date_application, gbc);

		// Type
		JLabel label_type = new JLabel("Type");
		label_type.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy++;
		up.add(label_type, gbc);
		JComboBox<Boolean> field_type = new JComboBox<>(new Boolean[] { true, false });
		field_type.setRenderer(new BooleanCustomComboBoxRenderer(app.getLangAtlas().getText("transaction.output"),
				app.getLangAtlas().getText("transaction.input"), Color.RED, Color.GREEN,
				app.getIconAtlas().getIcon("output", 16), app.getIconAtlas().getIcon("input", 16)));
		field_type.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_type, gbc);

		// Done
		JLabel label_status = new JLabel("Status");
		label_status.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy = 8;
		gbc.gridx = 1;
		up.add(label_status, gbc);
		JComboBox<TransactionState> field_state = new JComboBox<>(TransactionState.values());
		field_state.setRenderer(new StateComboBoxRenderer(app));
		gbc.gridy++;
		up.add(field_state, gbc);

		// Amount
		JLabel label_amount = new JLabel("Amount");
		label_amount.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridy++;
		gbc.gridx = 0;
		up.add(label_amount, gbc);
		SpinnerNumberModel number_model = new SpinnerNumberModel(0, 0, Float.MAX_VALUE, 0.01);
		JSpinner field_amount = new JSpinner(number_model);
		field_amount.setPreferredSize(new Dimension(150, 25));
		gbc.gridy++;
		up.add(field_amount, gbc);

		// ID
		JLabel label_id = new JLabel("ID");
		label_id.setAlignmentX(Component.LEFT_ALIGNMENT);
		gbc.gridx = 1;
		gbc.gridy = 10;
		up.add(label_id, gbc);
		JTextField field_id = new JTextField();
		field_id.setPreferredSize(new Dimension(150, 25));
		field_id.setEditable(false);
		field_id.setFocusable(false);
		gbc.gridy++;
		up.add(field_id, gbc);

		add(up);

		JPanel down = new JPanel();
		down.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;

		JButton left = new JButton(transaction == null ? "Create" : "Save");
		left.setPreferredSize(new Dimension(150, 25));
		down.add(left, gbc);

		JButton right = new JButton(app.getLangAtlas().getText("menu.cancel"));
		right.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		gbc.gridx++;
		right.setPreferredSize(new Dimension(150, 25));
		down.add(right, gbc);

		add(down);

		if (transaction != null) {
			field_name.setText(transaction.getName());
			field_location.setText(transaction.getLocation());
			field_category.setSelectedItem(transaction.getCategory());
			field_account.setSelectedItem(transaction.getAccount());
			field_date_creation.setText(Transaction.DATE_FORMAT.format(transaction.getDate_creation()));
			field_date_application.setText(Transaction.DATE_FORMAT.format(transaction.getDate_application()));
			field_type.setSelectedItem(transaction.isOutput());
			field_state.setSelectedItem(transaction.getState());
			field_amount.setValue(transaction.getAmount());
			field_id.setText(transaction.getId() + "");
		}

		left.addActionListener(e -> {
			Border border = UIManager.getBorder("TextField.border");
			boolean problem = false;
			Date date_creation = Utils.parseDate(Transaction.DATE_FORMAT, field_date_creation.getText());
			field_date_creation.setBorder(border);
			if (date_creation == null) {
				problem = true;
				field_date_creation.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
			Date date_application = Utils.parseDate(Transaction.DATE_FORMAT, field_date_application.getText());
			field_date_application.setBorder(border);
			if (date_application == null) {
				problem = true;
				field_date_application.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
			field_name.setBorder(border);
			if (field_name.getText().equals("")) {
				problem = true;
				field_name.setBorder(BorderFactory.createLineBorder(Color.RED));
			}

			if (problem) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			if (transaction != null) {
				transaction.setName(field_name.getText());
				transaction.setLocation(field_location.getText());
				transaction.setCategory((Category) field_category.getSelectedItem());
				transaction.setAccount((Account) field_account.getSelectedItem());
				transaction.setDate_creation(Utils.parseDate(Transaction.DATE_FORMAT, field_date_creation.getText()));
				transaction.setDate_application(
						Utils.parseDate(Transaction.DATE_FORMAT, field_date_application.getText()));
				transaction.setOutput((Boolean) field_type.getSelectedItem());
				transaction.setState((TransactionState) field_state.getSelectedItem());
				transaction.setAmount(((Number) field_amount.getValue()).floatValue());
			}
			Transaction res = transaction == null
					? new Transaction(-1, (Account) field_account.getSelectedItem(),
							(Category) field_category.getSelectedItem(), field_name.getText(), field_location.getText(),
							((Number) field_amount.getValue()).floatValue(), date_creation, date_application,
							(Boolean) field_type.getSelectedItem(), (TransactionState) field_state.getSelectedItem())
					: transaction;
			try {
				res.saveIn(app.getDataBase());
				transactionPanel.refreshTable();
				transactionPanel.refreshWorkTable();
				setVisible(false);
				dispose();
			} catch (SQLException err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(app.getJFrame(), "Unable to create new transaction", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		// Adding escape keystroke
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				"close");
		getRootPane().getActionMap().put("close", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();

			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private class BooleanCustomComboBoxRenderer extends BasicComboBoxRenderer {

		private String yes, no;
		private Color yesColor, noColor;
		private ImageIcon yesIcon, noIcon;

		public BooleanCustomComboBoxRenderer(String yes, String no, Color yesColor, Color noColor, ImageIcon yesIcon,
				ImageIcon noIcon) {
			super();
			this.yes = yes;
			this.no = no;
			this.yesColor = yesColor;
			this.noColor = noColor;
			this.yesIcon = yesIcon;
			this.noIcon = noIcon;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel res = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof Boolean bool) {
				res.setText(bool ? yes : no);
				res.setForeground(bool ? yesColor : noColor);
				res.setIcon(bool ? yesIcon : noIcon);
			}
			return res;
		}
	}

	private class StateComboBoxRenderer extends BasicComboBoxRenderer {

		private ImageIcon[] icons;
		private String[] texts;

		private StateComboBoxRenderer(App app) {
			icons = new ImageIcon[TransactionState.values().length];
			texts = new String[icons.length];

			for (TransactionState state : TransactionState.values()) {
				icons[state.ordinal()] = app.getIconAtlas().getIcon(state.name().toLowerCase(), 16);
				texts[state.ordinal()] = app.getLangAtlas().getText(state.getLocalizationName());
			}
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel res = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof TransactionState state) {
				res.setForeground(state.getColor());
				res.setText(texts[state.ordinal()]);
				res.setIcon(icons[state.ordinal()]);
			}
			return res;
		}
	}

}
