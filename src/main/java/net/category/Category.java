package net.category;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.app.DataBase;

public class Category {

	private int id;
	private String name;

	public Category(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void insertIntoDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("insert into categories (name) values (?)");
		st.setString(1, name);
		st.executeUpdate();
	}

	public void saveToDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("update categories set name=? where id=?;");
		st.setString(1, name);
		st.setInt(2, id);
		st.executeUpdate();
	}

	public void deleteFromDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("delete from categories where id=?;");
		st.setInt(1, id);
		st.executeUpdate();
	}

	public void applyToDB(DataBase db) throws SQLException {
		if (id == -1) {
			insertIntoDB(db);
		} else {
			saveToDB(db);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Category cat ? cat.id == this.id : false;
	}
}
