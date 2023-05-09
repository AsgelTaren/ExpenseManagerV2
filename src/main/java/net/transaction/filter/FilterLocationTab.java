package net.transaction.filter;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.account.Account;
import net.app.App;

@SuppressWarnings("serial")
public class FilterLocationTab extends JPanel {

	private Vector<String> locations;
	private boolean[] used;
	private JCheckBox checkbox_filter;
	private JButton select, unselect;
	private JTable table;
	private FilterLocationTableModel model;

	public FilterLocationTab(App app, FilterOptions filters) {
		super();
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);

		// Upper panel
		JPanel up = new JPanel();
		up.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label_left = new JLabel("Filter by locations");
		up.add(label_left);

		checkbox_filter = new JCheckBox();
		checkbox_filter.addActionListener(e -> {
			onCheckBoxUpdate();
		});
		up.add(checkbox_filter);

		add(up, gbc);

		// Mid panel
		try {
			ResultSet set = app.getDataBase().getStatement()
					.executeQuery("SELECT location FROM transactions GROUP BY location;");
			locations = new Vector<>();
			while (set.next()) {
				locations.add(set.getString("location"));
			}
			used = new boolean[locations.size()];
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JPanel mid = new JPanel();
		mid.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		model = new FilterLocationTableModel(this);
		table = new JTable(model);
		mid.add(new JScrollPane(table), gbc);

		select = new JButton("Select");
		select.setEnabled(false);
		select.addActionListener(e -> useSelection());
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mid.add(select, gbc);

		unselect = new JButton("Unselect");
		unselect.setEnabled(false);
		unselect.addActionListener(e -> unUseSelection());
		gbc.gridx++;
		mid.add(unselect, gbc);

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		add(mid, gbc);

		table.getSelectionModel().addListSelectionListener(e -> {
			boolean selection = table.getSelectedRows().length > 0;
			select.setEnabled(selection);
			unselect.setEnabled(selection);
		});

		if (filters.getAllowedLocations() != null) {
			checkbox_filter.setSelected(true);
			HashMap<String, Integer> accountMap = new HashMap<>();
			for (int i = 0; i < locations.size(); i++) {
				accountMap.put(locations.get(i), i);
			}

			for (String location : filters.getAllowedLocations()) {
				used[accountMap.get(location)] = true;
			}
		}

		onCheckBoxUpdate();
	}

	private void useSelection() {
		for (int i : table.getSelectedRows()) {
			used[i] = true;
		}
		table.repaint();
	}

	private void unUseSelection() {
		for (int i : table.getSelectedRows()) {
			used[i] = false;
		}
		table.repaint();
	}

	private void onCheckBoxUpdate() {
		table.setEnabled(checkbox_filter.isSelected());
		table.setFocusable(checkbox_filter.isSelected());
		table.clearSelection();
	}

	public boolean isOn() {
		return checkbox_filter.isSelected();
	}

	public Vector<String> getAllowedLocations() {
		Vector<String> res = new Vector<>();
		for (int i = 0; i < locations.size(); i++) {
			if (used[i]) {
				res.add(locations.get(i));
			}
		}
		return res;
	}

	public void resetFilter() {
		used = new boolean[locations.size()];
		checkbox_filter.setSelected(false);
		table.repaint();
	}

	private class FilterLocationTableModel extends AbstractTableModel {

		private FilterLocationTab tab;

		public FilterLocationTableModel(FilterLocationTab tab) {
			super();
			this.tab = tab;
		}

		@Override
		public int getRowCount() {
			return tab.locations.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return tab.locations.get(rowIndex);
			case 1:
				return tab.used[rowIndex];
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? Account.class : Boolean.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Location";
			case 1:
				return "Used";
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 1) {
				used[row] = (Boolean) value;
			}
		}
	}

}