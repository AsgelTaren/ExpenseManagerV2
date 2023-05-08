package net.category;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

import net.app.App;

@SuppressWarnings("serial")
public class CategoryListModel extends AbstractListModel<Category> {

	private App app;
	private ArrayList<Category> categories;

	public CategoryListModel(App app) {
		this.app = app;
	}

	public void query() {
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from categories;");
			categories = new ArrayList<>();
			while (set.next()) {
				categories.add(new Category(set.getInt("id"), set.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getSize() {
		return categories.size();
	}

	@Override
	public Category getElementAt(int index) {
		return categories.get(index);
	}

}