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
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import net.app.App;
import net.category.Category;

@SuppressWarnings("serial")
public class FilterCategoryTab extends JPanel {

	private Vector<FilterCategoryHolder> categories, shown;
	private JCheckBox checkbox_filter;
	private JButton select, unselect;
	private JTable table;
	private FilterCategoryTableModel model;

	public FilterCategoryTab(App app, FilterOptions filters) {
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
		JLabel label_left = new JLabel("Filter by categories");
		up.add(label_left);

		checkbox_filter = new JCheckBox();
		checkbox_filter.addActionListener(e -> {
			onCheckBoxUpdate();
		});
		up.add(checkbox_filter);

		add(up, gbc);

		// Mid panel
		HashMap<Category, FilterCategoryHolder> catMap = new HashMap<>();
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("SELECT * FROM categories;");
			categories = new Vector<>();
			while (set.next()) {
				FilterCategoryHolder holder = new FilterCategoryHolder(
						new Category(set.getInt("id"), set.getString("name")), false);
				categories.add(holder);
				catMap.put(holder.cat, holder);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JPanel mid = new JPanel();
		mid.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;

		JTextField search = new JTextField();
		search.addActionListener(e -> searchByName(search.getText()));
		mid.add(search, gbc);

		gbc.weighty = 1;
		gbc.gridy++;
		model = new FilterCategoryTableModel(this);
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

		if (filters.getAllowedCategories() != null) {
			checkbox_filter.setSelected(true);
			for (Category cat : filters.getAllowedCategories()) {
				catMap.get(cat).used = true;
			}
		}
		onCheckBoxUpdate();

		shown = new Vector<>(categories);
	}

	private void searchByName(String value) {
		if (value == null || value.equals("")) {
			shown = new Vector<>(categories);

		} else {
			value = value.toLowerCase();
			shown = new Vector<>(categories.size());
			for (FilterCategoryHolder holder : categories) {
				if (holder.cat.getName().toLowerCase().contains(value)) {
					shown.add(holder);
				}
			}
		}
		table.revalidate();
		table.repaint();
		table.clearSelection();
	}

	private void useSelection() {
		for (int i : table.getSelectedRows()) {
			shown.get(i).used = true;
		}
		table.repaint();
	}

	private void unUseSelection() {
		for (int i : table.getSelectedRows()) {
			shown.get(i).used = false;
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

	public Vector<Category> getAllowedCategories() {
		Vector<Category> res = new Vector<>();
		for (FilterCategoryHolder holder : categories) {
			if (holder.used) {
				res.add(holder.cat);
			}
		}
		return res;
	}

	public void resetFilter() {
		for (FilterCategoryHolder holder : categories) {
			holder.used = false;
		}
		checkbox_filter.setSelected(false);
		table.repaint();
	}

	private class FilterCategoryHolder {
		private Category cat;
		private boolean used;

		public FilterCategoryHolder(Category cat, boolean used) {
			this.cat = cat;
			this.used = used;
		}
	}

	private class FilterCategoryTableModel extends AbstractTableModel {

		private FilterCategoryTab tab;

		public FilterCategoryTableModel(FilterCategoryTab tab) {
			super();
			this.tab = tab;
		}

		@Override
		public int getRowCount() {
			return shown.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return tab.shown.get(rowIndex).cat;
			case 1:
				return tab.shown.get(rowIndex).used;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? Category.class : Boolean.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "Category";
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
				shown.get(row).used = (Boolean) value;
			}
		}
	}

}