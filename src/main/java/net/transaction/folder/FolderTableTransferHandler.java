package net.transaction.folder;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.transaction.Transaction;
import net.transaction.TransactionTransferable;

@SuppressWarnings("serial")
public class FolderTableTransferHandler extends TransferHandler {

	private FolderPanel panel;

	public FolderTableTransferHandler(FolderPanel panel) {
		super();
		this.panel = panel;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (panel.getSelectedFolder() == null)
			return false;
		return support.isDataFlavorSupported(TransactionTransferable.TRANSACTION_FLAVOR) && support.isDrop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support))
			return false;
		try {
			Vector<Transaction> data = (Vector<Transaction>) support.getTransferable()
					.getTransferData(TransactionTransferable.TRANSACTION_FLAVOR);
			Folder folder = panel.getSelectedFolder();
			for (Transaction trans : data) {
				try {
					trans.addToFolder(panel.getApp().getDataBase(), folder);
				} catch (Exception e) {
					
				}
			}
			panel.refreshTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent component) {
		return COPY;
	}
}