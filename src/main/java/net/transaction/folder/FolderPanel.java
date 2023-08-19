package net.transaction.folder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

import net.app.App;
import net.app.WorkablePanel;
import net.transaction.Transaction;
import net.transaction.TransactionTableRenderer;
import net.transaction.TransactionTree;
import net.transaction.TransactionWorkZoneModel;
import net.transaction.TransactionWorkZoneTransferHandler;

@SuppressWarnings("serial")
public class FolderPanel extends JPanel implements WorkablePanel {

	private App app;
	private JToolBar toolbar;
	private JList<Folder> list;
	private FolderListModel listModel;

	private JTable table, workTable;
	private FolderTableModel tableModel;
	private TransactionWorkZoneModel workZoneModel;
	private TransactionTree tree;

	public FolderPanel(App app) {
		super();
		this.app = app;

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridx = gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;

		gbc.fill = GridBagConstraints.HORIZONTAL;

		createToolBar();
		add(toolbar, gbc);

		listModel = new FolderListModel(app);
		list = new JList<>(listModel);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		gbc.gridy = 1;
		gbc.weighty = 2;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(list), gbc);

		tableModel = new FolderTableModel(app);
		tableModel.setFolder(1);
		table = new JTable(tableModel);
		table.setDefaultRenderer(String.class, new TransactionTableRenderer(app));
		table.setFillsViewportHeight(true);
		table.setEnabled(false);
		table.setDragEnabled(true);
		table.setTransferHandler(new FolderTableTransferHandler(this));
		table.setDropMode(DropMode.INSERT_ROWS);
		table.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					Vector<Transaction> temp = new Vector<>();
					Folder target = getSelectedFolder();
					if (target == null)
						return;
					for (int i : table.getSelectedRows()) {
						temp.add(tableModel.getTransactions().get(i));
					}
					int choice = JOptionPane.showConfirmDialog(app.getJFrame(),
							"Are you sure you want to remove the currently " + temp.size()
									+ " selected transactions from the folder " + target.getName() + "?");
					if (choice == JOptionPane.YES_OPTION) {
						for (Transaction trans : temp) {
							try {
								trans.removeFromFolder(app.getDataBase(), target);
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
						refreshTable();
					}
				}
			}
		});
		gbc.gridx++;
		gbc.weightx = 3;
		add(new JScrollPane(table), gbc);

		tree = new TransactionTree(app, tableModel);
		gbc.gridx++;
		add(new JScrollPane(tree), gbc);

		workZoneModel = new TransactionWorkZoneModel(app);
		workTable = new JTable(workZoneModel);
		workTable.setFillsViewportHeight(true);
		workTable.setDragEnabled(true);
		workTable.setTransferHandler(new TransactionWorkZoneTransferHandler(this));
		workTable.setDefaultRenderer(String.class, new TransactionTableRenderer(app));
		workTable.setDropMode(DropMode.INSERT_ROWS);
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.weightx = 1;
		gbc.gridy = 2;
		add(new JScrollPane(workTable), gbc);

		list.getSelectionModel().addListSelectionListener(e -> {
			if (list.getSelectedIndex() != -1) {
				tableModel.setFolder(listModel.getFolders().get(list.getSelectedIndex()).getID());
				refreshTable();
				tree.updateTree(listModel.getFolders().get(list.getSelectedIndex()).getName());
			} else {
				tableModel.setFolder(0);
			}
			toolbar.getComponent(1).setEnabled(list.getSelectedIndex() != -1);
			toolbar.getComponent(2).setEnabled(list.getSelectedIndex() != -1);
			table.setEnabled(list.getSelectedIndex() != -1);
			tableModel.query();
			tree.updateTree();
		});

		refreshList();
		refreshTable();
		tree.updateTree();
	}

	private void createToolBar() {
		toolbar = new JToolBar();

		JButton add = new JButton(app.getLangAtlas().getText("menu.add"));
		add.setIcon(app.getIconAtlas().getIcon("add", 32));
		add.addActionListener(e -> newFolderDialog());
		toolbar.add(add);

		JButton edit = new JButton(app.getLangAtlas().getText("menu.edit"));
		edit.setIcon(app.getIconAtlas().getIcon("edit", 32));
		edit.setEnabled(false);
		edit.addActionListener(e -> editSelection());
		toolbar.add(edit);

		JButton remove = new JButton(app.getLangAtlas().getText("menu.remove"));
		remove.setIcon(app.getIconAtlas().getIcon("remove", 32));
		remove.setEnabled(false);
		remove.addActionListener(e -> {
			if (list.getSelectedIndex() != -1) {
				Folder target = listModel.getFolders().get(list.getSelectedIndex());
				int choice = JOptionPane.showConfirmDialog(app.getJFrame(),
						"Do you really want to remove the following folder: " + target.getName(), "Warning",
						JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					try {
						target.removeFrom(app.getDataBase());
						list.clearSelection();
						table.clearSelection();
						tree.clearSelection();
						refreshList();
					} catch (SQLException e1) {
						JOptionPane.showMessageDialog(app.getJFrame(),
								"Error while trying to remove selected folder. Reason: " + e1.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		toolbar.add(remove);
	}

	public void refreshList() {
		listModel.query();
		list.setModel(new DefaultListModel<>());
		list.setModel(listModel);
		list.revalidate();
		list.repaint();
	}

	public void refreshTable() {
		tableModel.query();
		table.revalidate();
		table.repaint();
		tree.updateTree();
	}

	public void newFolderDialog() {
		FolderDialog dialog = new FolderDialog(app, this, null);
		dialog.setVisible(true);
	}

	public void refreshWorkZone() {
		workZoneModel.refresh();
		workTable.revalidate();
		workTable.repaint();
	}

	public void editSelection() {
		FolderDialog dialog = new FolderDialog(app, this, listModel.getFolders().get(list.getSelectedIndex()));
		dialog.setVisible(true);
	}

	public void onFocus() {
		refreshWorkZone();
	}

	@Override
	public List<Transaction> getWorkedOnTransactions() {
		Vector<Transaction> res = new Vector<>();
		for (int i : workTable.getSelectedRows()) {
			res.add(workZoneModel.getTransactions().get(i));
		}
		return res;
	}

	@Override
	public void addTransactionsToWork(Collection<Transaction> data) {
		app.getWorkZone().addAll(data);
	}

	@Override
	public void refresh() {
		refreshWorkZone();

	}

	public Folder getSelectedFolder() {
		return (Folder) list.getSelectedValue();
	}

	public App getApp() {
		return app;
	}
}