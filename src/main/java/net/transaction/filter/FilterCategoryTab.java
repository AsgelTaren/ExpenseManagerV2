package net.transaction.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import net.app.App;
import net.category.Category;

@SuppressWarnings("serial")
public class FilterCategoryTab extends SimpleFilterTab<Category> {

	public FilterCategoryTab(App app, FilterOptions filters) {
		super(app, "Category", filters);
	}

	@Override
	protected List<Category> getParamValues(App app) {
		try {
			Vector<Category> result = new Vector<>();
			ResultSet set = app.getDataBase().getStatement().executeQuery("SELECT * FROM categories;");
			while (set.next()) {
				result.add(new Category(set.getInt("id"), set.getString("name")));
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List<Category> getSelectedValues(FilterOptions filters) {
		return filters.getAllowedCategories();
	}

}