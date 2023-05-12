package net.account;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.app.DataBase;

public class Account {

	private int id;
	private String name;
	private float balance;

	public Account(int id, String name, float balance) {
		this.id = id;
		this.name = name;
		this.balance = balance;
	}

	public void insertIntoDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("insert into accounts (name,balance) values (?,?);");
		st.setString(1, name);
		st.setFloat(2, balance);
		st.executeUpdate();
	}

	public void saveToDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection()
				.prepareStatement("update accounts set name=?, balance = ? where id=?;");
		st.setString(1, name);
		st.setFloat(2, balance);
		st.setInt(3, id);
		st.executeUpdate();
	}

	public void deleteFromDB(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement("delete from accounts where id=?;");
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Account a ? a.id == this.id : false;
	}

	@Override
	public int hashCode() {
		return id;
	}

}