package net.transaction.filter;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import net.app.App;
import net.transaction.TransactionState;

@SuppressWarnings("serial")
public class FilterStateTab extends SimpleFilterTab<TransactionState> {

	private String[] texts;
	private ImageIcon[] icons;

	public FilterStateTab(App app, FilterOptions filters) {
		super(app, "State", filters);
		texts = new String[TransactionState.values().length];
		icons = new ImageIcon[texts.length];

		for (TransactionState state : TransactionState.values()) {
			texts[state.ordinal()] = app.getLangAtlas().getText(state.getLocalizationName());
			icons[state.ordinal()] = app.getIconAtlas().getIcon(state.name().toLowerCase(), 16);
		}
	}

	@Override
	protected List<TransactionState> getParamValues(App app) {
		return Arrays.stream(TransactionState.values()).toList();
	}

	@Override
	protected List<TransactionState> getSelectedValues(FilterOptions filters) {
		return filters.getAllowedStates();
	}

	@Override
	protected Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel res = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value instanceof TransactionState state) {
			res.setForeground(state.getColor());
			res.setIcon(icons[state.ordinal()]);
			res.setText(texts[state.ordinal()]);
		}
		return res;
	}

	@Override
	protected String getStringForParam(TransactionState state) {
		return texts[state.ordinal()];
	}

}
