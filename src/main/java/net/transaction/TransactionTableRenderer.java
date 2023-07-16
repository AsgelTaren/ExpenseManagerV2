package net.transaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import net.app.App;

@SuppressWarnings("serial")
public class TransactionTableRenderer extends DefaultTableCellRenderer {

	public static final DateFormat renderFormat = new SimpleDateFormat("EEEE dd MMMM yyyy");

	private String input, output;
	private ImageIcon inputIcon, outputIcon;
	private Font bold;
	private String[] stateTexts;
	private ImageIcon[] stateIcons;

	public TransactionTableRenderer(App app) {
		this.input = app.getLangAtlas().getText("transaction.input");
		this.output = app.getLangAtlas().getText("transaction.output");
		this.inputIcon = app.getIconAtlas().getIcon("input", 16);
		this.outputIcon = app.getIconAtlas().getIcon("output", 16);
		bold = UIManager.getFont("Table.font").deriveFont(Font.BOLD);

		stateTexts = new String[TransactionState.values().length];
		stateIcons = new ImageIcon[stateTexts.length];

		for (TransactionState state : TransactionState.values()) {
			stateTexts[state.ordinal()] = app.getLangAtlas().getText(state.getLocalizationName());
			stateIcons[state.ordinal()] = app.getIconAtlas().getIcon(state.name().toLowerCase(), 16);
		}
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JLabel res = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		res.setForeground(UIManager.getColor("Table.foreground"));
		res.setFont(UIManager.getFont("Table.font"));
		res.setIcon(null);
		if (value instanceof Boolean isOutput) {
			res.setForeground(isOutput ? Color.RED : Color.GREEN);
			res.setText(isOutput ? output : input);
			res.setFont(bold);
			res.setIcon(isOutput ? outputIcon : inputIcon);
		}
		if (value instanceof TransactionState state) {
			res.setIcon(stateIcons[state.ordinal()]);
			res.setForeground(state.getColor());
			res.setFont(bold);
			res.setText(stateTexts[state.ordinal()]);
		}
		if (value instanceof Date date) {
			res.setText(renderFormat.format(date));
		}
		return res;
	}

}
