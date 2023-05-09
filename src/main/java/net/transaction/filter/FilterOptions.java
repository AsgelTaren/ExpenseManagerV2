package net.transaction.filter;

import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

import net.account.Account;
import net.app.App;
import net.category.Category;

public class FilterOptions {

	private Vector<Category> categories;
	private Vector<Account> accounts;
	private Vector<String> locations;

	public FilterOptions(FilterDialog dialog) {
		if (dialog.getCategoryTab().isOn()) {
			categories = dialog.getCategoryTab().getAllowedCategories();
		}
		if (dialog.getAccountTab().isOn()) {
			accounts = dialog.getAccountTab().getAllowedAccount();
		}
		if (dialog.getLocationTab().isOn()) {
			locations = dialog.getLocationTab().getAllowedLocations();
		}
	}

	public FilterOptions() {

	}

	public String createRequest(App app) {
		StringBuilder result = new StringBuilder();
		result.append("SELECT * FROM transactions");

		ArrayList<String> options = new ArrayList<>();

		if (categories != null) {
			options.add("category IN "
					+ categories.stream().map(cat -> cat.getId() + "").collect(Collectors.joining(",", "(", ")")));
		}

		if (accounts != null) {
			options.add("account IN "
					+ accounts.stream().map(cat -> cat.getId() + "").collect(Collectors.joining(",", "(", ")")));
		}

		if (locations != null) {
			options.add("location IN " + locations.stream().collect(Collectors.joining("\",\"", "(\"", "\")")));
		}

		if (options.size() > 0) {
			String parameters = options.stream().collect(Collectors.joining(" AND ", " WHERE ", ""));
			result.append(parameters);
		}
		result.append(" ORDER BY date_creation");
		return result.toString();
	}

	public Vector<Category> getAllowedCategories() {
		return categories;
	}

	public Vector<Account> getAllowedAccounts() {
		return accounts;
	}

	public Vector<String> getAllowedLocations() {
		return locations;
	}

}