package net.transaction;

import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

@SuppressWarnings("serial")
public class TransactionWorkZoneTransferHandler extends TransferHandler {

	private TransactionPanel panel;

	public TransactionWorkZoneTransferHandler(TransactionPanel panel) {
		this.panel = panel;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (c instanceof JTable) {
			List<Transaction> data = panel.getSelectedTransactionsInWorkZone();
			if (data.size() == 0) {
				return null;
			}
			return new TransactionTransferable(data);
		}
		return null;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		return support.isDataFlavorSupported(TransactionTransferable.TRANSACTION_FLAVOR) && support.isDrop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		try {
			Vector<Transaction> data = (Vector<Transaction>) support.getTransferable()
					.getTransferData(TransactionTransferable.TRANSACTION_FLAVOR);
			panel.getApp().getWorkZone().addAll(data);
			panel.refreshWorkTable();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

}
