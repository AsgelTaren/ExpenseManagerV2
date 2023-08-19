package net.transaction.folder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

import net.account.Account;
import net.app.App;
import net.app.Utils;
import net.category.Category;
import net.transaction.Transaction;
import net.transaction.TransactionState;

@SuppressWarnings("serial")
public class FolderTableModel extends AbstractTableModel {
	private App app;
	private int folder;
	private ArrayList<Transaction> transactions;
	private HashMap<Integer, Account> accounts;
	private HashMap<Integer, Category> categories;

	public FolderTableModel(App app) {
		super();
		this.app = app;
		transactions = new ArrayList<>();
	}

	public void setFolder(int folder) {
		this.folder = folder;
	}

	public int getFolder() {
		return folder;
	}

	public void query() {
		try {
			PreparedStatement st = app.getDataBase().getConnection().prepareStatement("select * from accounts;");
			ResultSet set = st.executeQuery();
			accounts = new HashMap<>();
			while (set.next()) {
				accounts.put(set.getInt("id"),
						new Account(set.getInt("id"), set.getString("name"), set.getFloat("balance")));
			}

			st = app.getDataBase().getConnection().prepareStatement("select * from categories;");
			set = st.executeQuery();
			categories = new HashMap<>();
			while (set.next()) {
				categories.put(set.getInt("id"), new Category(set.getInt("id"), set.getString("name")));
			}

			st = app.getDataBase().getConnection().prepareStatement(
					"select * from transactions join folderMemberships as mem on mem.transaction_id = transactions.id where mem.folder_id =(?);");
			st.setInt(1, folder);
			set = st.executeQuery();
			transactions = new ArrayList<>();
			while (set.next()) {
				transactions.add(new Transaction(set.getInt("id"), accounts.get(set.getInt("account")),
						categories.get(set.getInt("category")), set.getString("name"), set.getString("location"),
						set.getFloat("amount"),
						Utils.parseDate(Transaction.DATE_FORMAT, set.getString("date_creation")),
						Utils.parseDate(Transaction.DATE_FORMAT, set.getString("date_application")),
						set.getBoolean("output"), TransactionState.valueOf(set.getString("state").toUpperCase())));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
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
