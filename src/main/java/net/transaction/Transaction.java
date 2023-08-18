package net.transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;

import net.account.Account;
import net.app.DataBase;
import net.category.Category;

public class Transaction {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final Comparator<Transaction> COMPARATOR = (e1, e2) -> {
		int temp = e1.getDate_application().compareTo(e2.getDate_application());
		if (temp == 0) {
			return e1.getId() - e2.getId();
		}
		return temp;
	};

	private int id;
	private Account account;
	private Category category;
	private String name, location;
	private float amount;
	private boolean output;
	private TransactionState state;
	private Date date_creation, date_application;

	public Transaction(int id, Account account, Category category, String name, String location, float amount,
			Date date_creation, Date date_application, boolean output, TransactionState state) {
		this.id = id;
		this.account = account;
		this.category = category;
		this.name = name;
		this.location = location;
		this.amount = amount;
		this.date_creation = date_creation;
		this.date_application = date_application;
		this.output = output;
		this.state = state;
	}

	public void insertInto(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement(
				"insert into transactions (account,category,name,location,amount,date_creation,date_application,output,state) values (?,?,?,?,?,?,?,?,?)");
		st.setInt(1, account.getId());
		st.setInt(2, category.getId());
		st.setString(3, name);
		st.setString(4, location);
		st.setFloat(5, amount);
		st.setString(6, DATE_FORMAT.format(date_creation));
		st.setString(7, DATE_FORMAT.format(date_application));
		st.setBoolean(8, output);
		st.setString(9, state.name().toLowerCase());
		st.executeUpdate();
	}

	public void updateIn(DataBase db) throws SQLException {
		PreparedStatement st = db.getConnection().prepareStatement(
				"update transactions set account=?, category=?,name=?,location=?,amount=?,date_creation=?,date_application=?,output=?,state=? where id=?;");
		st.setInt(1, account.getId());
		st.setInt(2, category.getId());
		st.setString(3, name);
		st.setString(4, location);
		st.setFloat(5, amount);
		st.setString(6, DATE_FORMAT.format(date_creation));
		st.setString(7, DATE_FORMAT.format(date_application));
		st.setBoolean(8, output);
		st.setString(9, state.name().toLowerCase());
		st.setInt(10, id);
		st.executeUpdate();
	}

	public void saveIn(DataBase db) throws SQLException {
		if (id == -1) {
			insertInto(db);
		} else {
			updateIn(db);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public boolean isOutput() {
		return output;
	}

	public void setOutput(boolean output) {
		this.output = output;
	}

	public TransactionState getState() {
		return state;
	}

	public void setState(TransactionState state) {
		this.state = state;
	}

	public Date getDate_creation() {
		return date_creation;
	}

	public void setDate_creation(Date date_creation) {
		this.date_creation = date_creation;
	}

	public Date getDate_application() {
		return date_application;
	}

	public void setDate_application(Date date_application) {
		this.date_application = date_application;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Transaction t ? t.id == this.id : false;
	}

	@Override
	public int hashCode() {
		return id;
	}
}