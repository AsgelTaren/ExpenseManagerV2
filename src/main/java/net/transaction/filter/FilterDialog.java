package net.transaction.filter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import net.app.App;
import net.transaction.TransactionPanel;

@SuppressWarnings("serial")
public class FilterDialog extends JDialog {

	// Tabs
	private JTabbedPane tabs;
	private FilterCategoryTab categories;
	private FilterAccountTab accounts;
	private FilterLocationTab locations;
	private FilterStateTab states;

	public FilterDialog(App app, TransactionPanel transactionPanel) {
		super(app.getJFrame(), "Filters", true);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 2;
		tabs = new JTabbedPane();
		tabs.setPreferredSize(new Dimension(500, 400));
		tabs.setBorder(BorderFactory.createTitledBorder("Filtering options"));
		tabs.addTab("Category", categories = new FilterCategoryTab(app, transactionPanel.getFilterOptions()));
		tabs.addTab("Account", accounts = new FilterAccountTab(app, transactionPanel.getFilterOptions()));
		tabs.addTab("Location", locations = new FilterLocationTab(app, transactionPanel.getFilterOptions()));
		tabs.addTab("State", states = new FilterStateTab(app, transactionPanel.getFilterOptions()));

		add(tabs, gbc);

		JButton apply = new JButton("Apply filters");
		apply.addActionListener(e -> applyFilters(transactionPanel));
		gbc.weighty = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(apply, gbc);

		JButton reset = new JButton("Reset filters");
		reset.addActionListener(e -> resetFilters(transactionPanel));
		gbc.gridx++;
		add(reset, gbc);

		pack();
		setLocationRelativeTo(null);
		setResizable(false);
	}

	private void applyFilters(TransactionPanel panel) {
		panel.setFilterOptions(new FilterOptions(this));
		panel.refreshTable();
		setVisible(false);
		dispose();
	}

	private void resetFilters(TransactionPanel panel) {
		categories.resetFilter();
		accounts.resetFilter();
		locations.resetFilter();
		states.resetFilter();
	}

	public FilterCategoryTab getCategoryTab() {
		return categories;
	}

	public FilterAccountTab getAccountTab() {
		return accounts;
	}

	public FilterLocationTab getLocationTab() {
		return locations;
	}

	public FilterStateTab getStateTab() {
		return states;
	}

}