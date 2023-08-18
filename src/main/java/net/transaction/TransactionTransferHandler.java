package net.transaction;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.account.Account;
import net.category.Category;

@SuppressWarnings("serial")
public class TransactionTransferHandler extends TransferHandler {

	private TransactionPanel panel;

	public TransactionTransferHandler(TransactionPanel panel) {
		this.panel = panel;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (c instanceof JTable) {
			List<Transaction> data = panel.getSelectedTransactions();
			if (data.size() == 0) {
				return null;
			}
			return new TransactionTransferable(data);
		}
		return null;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (support.getComponent() instanceof JTable
				|| !support.isDataFlavorSupported(TransactionTransferable.TRANSACTION_FLAVOR)
				|| !(support.getComponent() instanceof JTree)) {
			return false;
		}

		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath path = dropLocation.getPath();
		if (path == null) {
			return false;
		}
		if (path.getLastPathComponent() instanceof DefaultMutableTreeNode node) {
			Object target = node.getUserObject();
			return target instanceof Category || target instanceof Account;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		Object target = ((DefaultMutableTreeNode) ((JTree.DropLocation) support.getDropLocation()).getPath()
				.getLastPathComponent()).getUserObject();
		try {
			Vector<Transaction> data = (Vector<Transaction>) support.getTransferable()
					.getTransferData(TransactionTransferable.TRANSACTION_FLAVOR);
			if (target instanceof Category cat) {
				for (Transaction t : data) {
					t.setCategory(cat);
					t.saveIn(panel.getApp().getDataBase());
				}
				panel.refreshTable();
				return true;
			}
			if (target instanceof Account account) {
				for (Transaction t : data) {
					t.setAccount(account);
					t.saveIn(panel.getApp().getDataBase());
				}
				panel.refreshTable();
				return true;
			}

		} catch (UnsupportedFlavorException | IOException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
