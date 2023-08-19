package net.transaction.folder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.app.DataBase;

public class Folder {

	private int id;
	private String name;

	public Folder(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void insertInto(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("insert into folders (name) values (?);");
		st.setString(1, name);
		st.executeUpdate();
	}

	public void saveInto(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("update folders set name=? where id = ?;");
		st.setString(1, name);
		st.setInt(2, id);
		st.executeUpdate();
	}

	public void updateInto(DataBase db) throws SQLException {
		if (id == -1) {
			insertInto(db);
		} else {
			saveInto(db);
		}
	}

	public void removeFrom(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("delete from folderMemberships where folder_id=?;");
		st.setInt(1, id);
		st.executeUpdate();

		st = db.getConnection().prepareStatement("delete from folders where id=?;");
		st.setInt(1, id);
		st.executeUpdate();
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}