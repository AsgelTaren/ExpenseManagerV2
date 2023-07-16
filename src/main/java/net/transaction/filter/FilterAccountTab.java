package net.transaction.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import net.account.Account;
import net.app.App;

@SuppressWarnings("serial")
public class FilterAccountTab extends SimpleFilterTab<Account> {

	public FilterAccountTab(App app, FilterOptions filters) {
		super(app, "Accounts", filters);
	}

	@Override
	protected List<Account> getParamValues(App app) {
		try {
			Vector<Account> result = new Vector<>();
			ResultSet set = app.getDataBase().getStatement().executeQuery("SELECT * FROM accounts;");
			while (set.next()) {
				result.add(new Account(set.getInt("id"), set.getString("name"), set.getFloat("balance")));
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected List<Account> getSelectedValues(FilterOptions filters) {
		return filters.getAllowedAccounts();
	}

}
