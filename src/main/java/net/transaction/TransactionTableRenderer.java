package net.transaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import net.app.App;
import net.transaction.TransactionTableModel.StatusHolder;

@SuppressWarnings("serial")
public class TransactionTableRenderer extends DefaultTableCellRenderer {

	private String input, output, pending, done;
	private ImageIcon inputIcon, outputIcon, pendingIcon, doneIcon;
	private Font bold;

	public TransactionTableRenderer(App app) {
		this.input = app.getLangAtlas().getText("transaction.input");
		this.output = app.getLangAtlas().getText("transaction.output");
		this.pending = app.getLangAtlas().getText("transaction.pending");
		this.done = app.getLangAtlas().getText("transaction.done");
		this.inputIcon = app.getIconAtlas().getIcon("input", 16);
		this.outputIcon = app.getIconAtlas().getIcon("output", 16);
		this.pendingIcon = app.getIconAtlas().getIcon("pending", 16);
		this.doneIcon = app.getIconAtlas().getIcon("done", 16);
		bold = UIManager.getFont("Table.font").deriveFont(Font.BOLD);
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
		if (value instanceof StatusHolder holder) {
			boolean status = holder.isDone();
			res.setForeground(status ? Color.GREEN : Color.MAGENTA);
			res.setText(status ? done : pending);
			res.setFont(bold);
			res.setIcon(status ? doneIcon : pendingIcon);
		}
		if (value instanceof Date date) {
			res.setText(Transaction.DATE_FORMAT.format(date));
		}
		return res;
	}

}
