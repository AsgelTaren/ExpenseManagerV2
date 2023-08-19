package net.transaction;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import net.app.App;

@SuppressWarnings("serial")
public class TransactionWorkZoneModel extends AbstractTableModel implements TransactionProvider {

	private App app;
	private ArrayList<Transaction> transactions;

	public TransactionWorkZoneModel(App app) {
		super();
		this.app = app;
		refresh();
	}

	public void refresh() {
		transactions = new ArrayList<>(app.getWorkZone());
		transactions.sort(Transaction.COMPARATOR);
	}

	@Override
	public int getRowCount() {
		return transactions.size();
	}

	@Override
	public int getColumnCount() {
		return 9;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Transaction target = transactions.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return target.getName();
		case 1:
			return target.getLocation();
		case 2:
			return target.getCategory();
		case 3:
			return target.getAccount();
		case 4:
			return target.getAmount();
		case 5:
			return target.getDate_creation();
		case 6:
			return target.getDate_application();
		case 7:
			return target.isOutput();
		case 8:
			return target.getState();
		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return "Name";
		case 1:
			return "Location";
		case 2:
			return "Category";
		case 3:
			return "Account";
		case 4:
			return "Amount";
		case 5:
			return "Date of Creation";
		case 6:
			return "Date of Application";
		case 7:
			return "Type";
		case 8:
			return "State";
		}
		return null;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
	
	public ArrayList<Transaction> getTransactions(){
		return transactions;
	}

}
