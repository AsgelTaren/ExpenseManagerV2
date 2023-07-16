package net.transaction.filter;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.app.App;

@SuppressWarnings("serial")
public abstract class SimpleFilterTab<A> extends JPanel {

	private Vector<ParamHolder<A>> paramValues, shown;
	private JCheckBox checkbox_filter;
	private JButton select, unselect;
	private JTable table;
	private FilterTableModel<A> model;
	private String paramName;
	private DefaultTableCellRenderer defaultRenderer;

	public SimpleFilterTab(App app, String paramName, FilterOptions filters) {
		super();
		this.paramName = paramName;
		this.defaultRenderer = new DefaultTableCellRenderer();
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
		HashMap<A, ParamHolder<A>> holderMap = new HashMap<>();
		List<A> preHold = getParamValues(app);
		paramValues = new Vector<ParamHolder<A>>(preHold.size());
		for (A param : preHold) {
			ParamHolder<A> holder = new ParamHolder<>(param, false);
			paramValues.add(holder);
			holderMap.put(param, holder);
		}

		JPanel mid = new JPanel();
		mid.setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;

		JTextField search = new JTextField();
		search.addActionListener(e -> searchByName(search.getText()));
		mid.add(search, gbc);

		gbc.weighty = 1;
		gbc.gridy++;
		model = new FilterTableModel<A>(this);
		table = new JTable(model);
		table.setDefaultRenderer(String.class, (tab, value, isSelected, hasFocus, row,
				column) -> getTableCellRendererComponent(tab, value, isSelected, hasFocus, row, column));
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

		if (getSelectedValues(filters) != null) {
			checkbox_filter.setSelected(true);
			for (A param : getSelectedValues(filters)) {
				holderMap.get(param).used = true;
			}
		}

		onCheckBoxUpdate();

		shown = new Vector<>(paramValues);
	}

	private void searchByName(String value) {
		if (value == null || value.equals("")) {
			shown = new Vector<>(paramValues);

		} else {
			value = value.toLowerCase();
			shown = new Vector<>(paramValues.size());
			for (ParamHolder<A> holder : paramValues) {
				if (getStringForParam(holder.value).toLowerCase().contains(value)) {
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

	public void resetFilter() {
		for (ParamHolder<A> holder : paramValues) {
			holder.used = false;
		}
		checkbox_filter.setSelected(false);
		table.repaint();
	}

	public Vector<A> getAllowedValues() {
		Vector<A> res = new Vector<>();
		for (ParamHolder<A> holder : paramValues) {
			if (holder.used) {
				res.add(holder.value);
			}
		}
		return res;
	}

	protected abstract List<A> getParamValues(App app);

	protected abstract List<A> getSelectedValues(FilterOptions filters);

	protected Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	protected String getStringForParam(A param) {
		return param.toString();
	}

	private class ParamHolder<B> {

		private B value;
		private boolean used;

		private ParamHolder(B value, boolean used) {
			this.value = value;
			this.used = used;
		}

	}

	private class FilterTableModel<B> extends AbstractTableModel {

		private SimpleFilterTab<B> tab;

		public FilterTableModel(SimpleFilterTab<B> tab) {
			super();
			this.tab = tab;
		}

		@Override
		public int getRowCount() {
			return tab.shown.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return tab.shown.get(rowIndex).value;
			case 1:
				return tab.shown.get(rowIndex).used;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? String.class : Boolean.class;
		}

		@Override
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return tab.paramName;
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
				tab.shown.get(row).used = (Boolean) value;
			}
		}
	}

}
