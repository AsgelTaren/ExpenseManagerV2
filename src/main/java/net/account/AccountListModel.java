package net.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

import net.app.App;

@SuppressWarnings("serial")
public class AccountListModel extends AbstractListModel<Account> {

	private App app;
	private ArrayList<Account> accounts;

	public AccountListModel(App app) {
		this.app = app;
	}

	public void query() {
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from accounts;");
			accounts = new ArrayList<>();
			while (set.next()) {
				accounts.add(new Account(set.getInt("id"), set.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getSize() {
		return accounts.size();
	}

	@Override
	public Account getElementAt(int index) {
		return accounts.get(index);
	}

}