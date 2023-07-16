package net.transaction.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import net.app.App;

@SuppressWarnings("serial")
public class FilterLocationTab extends SimpleFilterTab<String> {

	public FilterLocationTab(App app, FilterOptions filters) {
		super(app, "Locations", filters);
	}

	@Override
	protected List<String> getParamValues(App app) {
		try {
			Vector<String> result = new Vector<>();
			ResultSet set = app.getDataBase().getStatement()
					.executeQuery("SELECT location FROM transactions GROUP BY location;");
			while (set.next()) {
				result.add(set.getString("location"));
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List<String> getSelectedValues(FilterOptions filters) {
		return filters.getAllowedLocations();
	}

}