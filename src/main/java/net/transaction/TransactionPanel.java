package net.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.account.Account;
import net.app.App;
import net.category.Category;
import net.transaction.filter.FilterDialog;
import net.transaction.filter.FilterOptions;

@SuppressWarnings("serial")
public class TransactionPanel extends JPanel implements KeyListener {

	private App app;

	private JTable table;
	private TransactionTableModel model;
	private JToolBar toolbar;
	private JTree categoryTree, accountTree;
	private TransactionTransferHandler transferHandler;

	private FilterOptions filters;

	public TransactionPanel(App app) {
		super();
		this.app = app;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		createToolBar();
		add(toolbar, gbc);

		transferHandler = new TransactionTransferHandler(this);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 1;
		gbc.weighty = 1;
		filters = new FilterOptions();
		model = new TransactionTableModel(app);
		table = new JTable(model);
		table.setDefaultRenderer(String.class, new TransactionTableRenderer(app));
		table.getSelectionModel().addListSelectionListener(e -> selectionChanged());
		table.addKeyListener(this);
		table.setTransferHandler(transferHandler);
		table.setDragEnabled(true);

		JPanel queryPanel = new JPanel();
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query results"));
		queryPanel.setLayout(new GridBagLayout());
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 5, 5, 5);
		queryPanel.add(new JScrollPane(table), gbc);

		categoryTree = new JTree();
		categoryTree.setCellRenderer(new TransactionTreeRenderer(app));
		categoryTree.setTransferHandler(transferHandler);
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		queryPanel.add(new JScrollPane(categoryTree), gbc);

		accountTree = new JTree();
		accountTree.setCellRenderer(new TransactionTreeRenderer(app));
		accountTree.setTransferHandler(transferHandler);
		gbc.gridx = 1;
		queryPanel.add(new JScrollPane(accountTree), gbc);

		gbc.weightx = gbc.weighty = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		add(queryPanel, gbc);

		model.query(filters.createRequest(app));
		updateCategoryTree();
		updateAccountTree();
	}

	private void createToolBar() {
		toolbar = new JToolBar();

		JButton add = new JButton(app.getLangAtlas().getText("menu.add"));
		add.setIcon(app.getIconAtlas().getIcon("add", 32));
		add.addActionListener(e -> addTransaction());
		toolbar.add(add);

		JButton edit = new JButton(app.getLangAtlas().getText("menu.edit"));
		edit.setIcon(app.getIconAtlas().getIcon("edit", 32));
		edit.addActionListener(e -> editSelection());
		edit.setEnabled(false);
		toolbar.add(edit);

		JButton remove = new JButton(app.getLangAtlas().getText("menu.remove"));
		remove.setIcon(app.getIconAtlas().getIcon("remove", 32));
		remove.addActionListener(e -> removeSelection());
		remove.setEnabled(false);
		toolbar.add(remove);

		JButton filter = new JButton(app.getLangAtlas().getText("menu.filter"));
		filter.setIcon(app.getIconAtlas().getIcon("filter", 32));
		filter.addActionListener(e -> showFilters());
		toolbar.add(filter);

		JButton refresh = new JButton(app.getLangAtlas().getText("menu.refresh"));
		refresh.setIcon(app.getIconAtlas().getIcon("refresh", 32));
		refresh.addActionListener(e -> refreshTable());
		toolbar.add(refresh);
	}

	private void showFilters() {
		FilterDialog dialog = new FilterDialog(app, this);
		dialog.setVisible(true);
	}

	private void addTransaction() {
		TransactionDialog dialog = new TransactionDialog(app, this, null);
		dialog.setVisible(true);
	}

	private void editSelection() {
		int[] selection = table.getSelectedRows();
		if (selection.length == 1) {
			TransactionDialog dialog = new TransactionDialog(app, this, model.getTransactions().get(selection[0]));
			dialog.setVisible(true);
		} else {
			TransactionMultipleSelectionDialog dialog = new TransactionMultipleSelectionDialog(app, this,
					Arrays.stream(selection).<Transaction>mapToObj(index -> model.getTransactions().get(index))
							.collect(Collectors.toCollection(Vector::new)));
			dialog.setVisible(true);
		}
	}

	private void removeSelection() {
		int[] selection = table.getSelectedRows();
		int choice = JOptionPane.showConfirmDialog(app.getJFrame(),
				String.format("Do you really want to remove the current %d selected transactions?", selection.length),
				"Removal", JOptionPane.YES_NO_OPTION);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		StringJoiner joiner = new StringJoiner(",");
		for (int i : selection) {
			joiner.add(model.getTransactions().get(i).getId() + "");
		}
		try {
			app.getDataBase().getStatement()
					.executeUpdate("delete from transactions where id in (" + joiner.toString() + ");");
			refreshTable();
			table.clearSelection();
			selectionChanged();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(app.getJFrame(), "Unable to remove the current selection", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateCategoryTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Categories");
		HashMap<Category, TreeMeta> meta = new HashMap<>();
		for (Entry<Integer, Category> entry : model.getCategoriesMap().entrySet()) {
			meta.put(entry.getValue(), new TreeMeta());
		}

		for (Transaction trans : model.getTransactions()) {
			TreeMeta target = meta.get(trans.getCategory());
			target.amount = target.amount + (trans.isOutput() ? -trans.getAmount() : trans.getAmount());
			target.count++;
			if (trans.isOutput()) {
				target.outputs++;
			} else {
				target.inputs++;
			}
			target.states[trans.getState().ordinal()]++;
		}

		for (Entry<Integer, Category> entry : model.getCategoriesMap().entrySet()) {
			TreeMeta target = meta.get(entry.getValue());
			if (target.count > 0) {
				DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(entry.getValue());

				for (int i = 0; i < TreeMeta.KEYS.length; i++) {
					DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode(new TreeMetaHolder(target, i));
					catNode.add(metaNode);
				}

				root.add(catNode);
			}
		}

		((DefaultTreeModel) categoryTree.getModel()).setRoot(root);
		categoryTree.treeDidChange();
		categoryTree.revalidate();
		categoryTree.repaint();
	}

	private void updateAccountTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Accounts");
		HashMap<Account, TreeMeta> meta = new HashMap<>();
		for (Entry<Integer, Account> entry : model.getAccountsMap().entrySet()) {
			meta.put(entry.getValue(), new TreeMeta());
		}

		for (Transaction trans : model.getTransactions()) {
			TreeMeta target = meta.get(trans.getAccount());
			target.amount = target.amount + (trans.isOutput() ? -trans.getAmount() : trans.getAmount());
			target.count++;
			if (trans.isOutput()) {
				target.outputs++;
			} else {
				target.inputs++;
			}
			target.states[trans.getState().ordinal()]++;
		}

		for (Entry<Integer, Account> entry : model.getAccountsMap().entrySet()) {
			TreeMeta target = meta.get(entry.getValue());
			if (target.count > 0) {
				DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(entry.getValue());

				for (int i = 0; i < TreeMeta.KEYS.length; i++) {
					DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode(new TreeMetaHolder(target, i));
					catNode.add(metaNode);
				}

				root.add(catNode);
			}
		}

		((DefaultTreeModel) accountTree.getModel()).setRoot(root);
		accountTree.treeDidChange();
		accountTree.revalidate();
		accountTree.repaint();
	}

	private void selectionChanged() {
		boolean selection = table.getSelectedColumn() != -1;
		toolbar.getComponent(1).setEnabled(selection);
		toolbar.getComponent(2).setEnabled(selection);
	}

	public void refreshTable() {
		model.query(filters.createRequest(app));
		table.revalidate();
		table.repaint();
		updateCategoryTree();
		updateAccountTree();
	}

	public Vector<Transaction> getSelectedTransactions() {
		Vector<Transaction> res = new Vector<>();
		for (int i : table.getSelectedRows()) {
			res.add(model.getTransactions().get(i));
		}
		return res;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A) {
			table.clearSelection();
			addTransaction();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public TransactionTableModel getTransactionTableModel() {
		return model;
	}

	public void setFilterOptions(FilterOptions filters) {
		this.filters = filters;
	}

	public FilterOptions getFilterOptions() {
		return filters;
	}

	public App getApp() {
		return app;
	}

	public class TreeMeta {

		public static final String[] KEYS = Stream
				.concat(Arrays.stream(new String[] { "amount", "count", "input", "output" }),
						Arrays.stream(TransactionState.values()).map(state -> state.name().toLowerCase()))
				.toArray(i -> new String[i]);
		public static final Boolean[] IS_INT = Stream.concat(Arrays.stream(new Boolean[] { false, true, true, true }),
				Arrays.stream(TransactionState.values()).map(state -> true)).toArray(i -> new Boolean[i]);
		public float amount;
		public int count;
		public int inputs;
		public int outputs;
		public int[] states;

		public TreeMeta() {
			states = new int[TransactionState.values().length];

		}
	}

	public class TreeMetaHolder {

		private TreeMeta meta;
		private int type;

		public TreeMetaHolder(TreeMeta meta, int type) {
			this.meta = meta;
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public float getValue() {
			if (type >= 4) {
				return meta.states[type - 4];
			}
			switch (type) {
			case 0:
				return meta.amount;
			case 1:
				return meta.count;
			case 2:
				return meta.inputs;
			case 3:
				return meta.outputs;
			}
			return -1;
		}

	}

}