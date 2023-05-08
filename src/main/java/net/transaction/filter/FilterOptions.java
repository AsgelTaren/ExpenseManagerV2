package net.transaction.filter;

import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

import net.app.App;
import net.category.Category;

public class FilterOptions {

	private Vector<Category> categories;

	public FilterOptions(FilterDialog dialog) {
		if (dialog.getCategoryTab().isOn()) {
			categories = dialog.getCategoryTab().getAllowedCategories();
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

}