package net.transaction;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

public class TransactionTransferable implements Transferable {

	public static final DataFlavor TRANSACTION_FLAVOR = new DataFlavor(Transaction.class, "Transaction Data Flavor");

	private List<Transaction> data;

	public TransactionTransferable(List<Transaction> data) {
		this.data = data;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { TRANSACTION_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return TRANSACTION_FLAVOR.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!TRANSACTION_FLAVOR.equals(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return data;
	}

}