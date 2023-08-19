package net.transaction.folder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.AbstractListModel;

import net.app.App;

@SuppressWarnings("serial")
public class FolderListModel extends AbstractListModel<Folder> {

	private App app;
	private Vector<Folder> folders;

	public FolderListModel(App app) {
		super();
		this.app = app;
		folders = new Vector<>();
	}

	public void query() {
		folders = new Vector<>();
		try {
			PreparedStatement st = app.getDataBase().getConnection().prepareStatement("select * from folders");
			ResultSet set = st.executeQuery();
			while (set.next()) {
				Folder folder = new Folder(set.getInt("id"), set.getString("name"));
				folders.add(folder);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getSize() {
		return folders.size();
	}

	@Override
	public Folder getElementAt(int index) {
		return folders.get(index);
	}

	public Vector<Folder> getFolders() {
		return folders;
	}
}
