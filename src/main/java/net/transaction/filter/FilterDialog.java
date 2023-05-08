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

	public FilterDialog(App app, TransactionPanel transactionPanel) {
		super(app.getJFrame(), "Filters", true);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);

		tabs = new JTabbedPane();
		tabs.addTab("Category", categories = new FilterCategoryTab(app, transactionPanel.getFilterOptions()));
		tabs.setPreferredSize(new Dimension(500, 400));
		tabs.setBorder(BorderFactory.createTitledBorder("Filtering options"));

		add(tabs, gbc);

		JButton apply = new JButton("Apply filters");
		apply.addActionListener(e -> applyFilters(transactionPanel));
		gbc.weighty = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(apply, gbc);

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

	public FilterCategoryTab getCategoryTab() {
		return categories;
	}

}