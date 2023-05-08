package net.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.StringJoiner;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import net.app.App;
import net.transaction.filter.FilterDialog;
import net.transaction.filter.FilterOptions;

@SuppressWarnings("serial")
public class TransactionPanel extends JPanel {

	private App app;

	private JTable table;
	private TransactionTableModel model;
	private JToolBar toolbar;

	private FilterOptions filters;

	public TransactionPanel(App app) {
		super();
		this.app = app;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		createToolBar();
		add(toolbar, gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weighty = 1;
		filters = new FilterOptions();
		model = new TransactionTableModel(app);
		model.query(filters.createRequest(app));
		table = new JTable(model);
		table.setDefaultRenderer(String.class, new TransactionTableRenderer(app));
		table.getSelectionModel().addListSelectionListener(e -> selectionChanged());

		add(new JScrollPane(table), gbc);
	}

	private void createToolBar() {
		toolbar = new JToolBar();

		JButton add = new JButton(app.getLangAtlas().getText("menu.add"));
		add.setIcon(app.getIconAtlas().getIcon("add", 32));
		add.addActionListener(e -> addTransaction());
		toolbar.add(add);

		JButton edit = new JButton(app.getLangAtlas().getText("menu.edit"));
		edit.setIcon(app.getIconAtlas().getIcon("edit", 32));
		edit.addActionListener(e -> editSelection());
		edit.setEnabled(false);
		toolbar.add(edit);

		JButton remove = new JButton(app.getLangAtlas().getText("menu.remove"));
		remove.setIcon(app.getIconAtlas().getIcon("remove", 32));
		remove.addActionListener(e -> removeSelection());
		remove.setEnabled(false);
		toolbar.add(remove);

		JButton filter = new JButton(app.getLangAtlas().getText("menu.filter"));
		filter.setIcon(app.getIconAtlas().getIcon("filter", 32));
		filter.addActionListener(e -> showFilters());
		toolbar.add(filter);

		JButton refresh = new JButton(app.getLangAtlas().getText("menu.refresh"));
		refresh.setIcon(app.getIconAtlas().getIcon("refresh", 32));
		refresh.addActionListener(e -> refreshTable());
		toolbar.add(refresh);
	}

	private void showFilters() {
		FilterDialog dialog = new FilterDialog(app, this);
		dialog.setVisible(true);
	}

	private void addTransaction() {
		TransactionDialog dialog = new TransactionDialog(app, this, null);
		dialog.setVisible(true);
	}

	private void editSelection() {
		int[] selection = table.getSelectedRows();
		if (selection.length == 1) {
			TransactionDialog dialog = new TransactionDialog(app, this, model.getTransactions().get(selection[0]));
			dialog.setVisible(true);
		}
	}

	private void removeSelection() {
		int[] selection = table.getSelectedRows();
		int choice = JOptionPane.showConfirmDialog(app.getJFrame(),
				String.format("Do you really want to remove the current %d selected transactions?", selection.length),
				"Removal", JOptionPane.YES_NO_OPTION);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		StringJoiner joiner = new StringJoiner(",");
		for (int i : selection) {
			joiner.add(model.getTransactions().get(i).getId() + "");
		}
		try {
			app.getDataBase().getStatement()
					.executeUpdate("delete from transactions where id in (" + joiner.toString() + ");");
			refreshTable();
			table.clearSelection();
			selectionChanged();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(app.getJFrame(), "Unable to remove the current selection", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void selectionChanged() {
		boolean selection = table.getSelectedColumn() != -1;
		toolbar.getComponent(1).setEnabled(selection);
		toolbar.getComponent(2).setEnabled(selection);
	}

	public void refreshTable() {
		model.query(filters.createRequest(app));
		table.revalidate();
		table.repaint();
	}

	public TransactionTableModel getTransactionTableModel() {
		return model;
	}

	public void setFilterOptions(FilterOptions filters) {
		this.filters = filters;
	}

	public FilterOptions getFilterOptions() {
		return filters;
	}

}