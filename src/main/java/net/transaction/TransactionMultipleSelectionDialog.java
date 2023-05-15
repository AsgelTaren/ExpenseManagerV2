package net.transaction;

import java.util.Vector;

import javax.swing.JDialog;

import net.app.App;

@SuppressWarnings("serial")
public class TransactionMultipleSelectionDialog extends JDialog {

	public TransactionMultipleSelectionDialog(App app, TransactionPanel transPanel, Vector<Transaction> selection) {
		super(app.getJFrame(), "Selection Editor", true);
	}
}