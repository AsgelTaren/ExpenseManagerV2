package net.transaction;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.app.App;
import net.transaction.filter.FilterDialog;
import net.transaction.filter.FilterOptions;

@SuppressWarnings("serial")
public class TransactionPanel extends JPanel implements KeyListener {

	private App app;

	private JTable table, workTable;
	private TransactionTableModel model;
	private TransactionWorkZoneModel workZoneModel;
	private JToolBar toolbar;
	private JTree queryTree, workTree;
	private TransactionTransferHandler transferHandler;

	private FilterOptions filters;

	public static final LinkedHashMap<String, Function<Transaction, Object>> metaCategories = new LinkedHashMap<>();
	static {
		String global = "Global";
		metaCategories.put("categories", trans -> trans.getCategory());
		metaCategories.put("accounts", trans -> trans.getAccount());
		metaCategories.put("months", trans -> YearMonth
				.from(trans.getDate_application().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		metaCategories.put("years", trans -> Year
				.from(trans.getDate_application().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		metaCategories.put("global", trans -> global);
	}

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
		table.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					editSelection();
				}
			}
		});

		JPanel queryPanel = new JPanel();
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query results"));
		queryPanel.setLayout(new GridBagLayout());
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		queryPanel.add(new JScrollPane(table), gbc);

		queryTree = new JTree();
		queryTree.setCellRenderer(new TransactionTreeRenderer(app));
		queryTree.setTransferHandler(transferHandler);
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		queryPanel.add(new JScrollPane(queryTree), gbc);

		workTree = new JTree();
		workTree.setCellRenderer(new TransactionTreeRenderer(app));
		workTree.setTransferHandler(transferHandler);
		gbc.gridx = 1;
		queryPanel.add(new JScrollPane(workTree), gbc);

		workZoneModel = new TransactionWorkZoneModel(app);
		workTable = new JTable(workZoneModel);
		workTable.setDefaultRenderer(String.class, new TransactionTableRenderer(app));
		workTable.setDragEnabled(true);
		workTable.setTransferHandler(new TransactionWorkZoneTransferHandler(this));
		workTable.setDropMode(DropMode.INSERT_ROWS);
		workTable.setFillsViewportHeight(true);
		workTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					for (int i : workTable.getSelectedRows()) {
						app.getWorkZone().remove(workZoneModel.getTransactions().get(i));
					}
					refreshWorkTable();
				}
			}

		});
		workTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					editSelectionInWorkZone();
				}
			}
		});
		gbc.gridx = 2;
		gbc.weightx = 3;
		queryPanel.add(new JScrollPane(workTable), gbc);

		gbc.weightx = gbc.weighty = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		add(queryPanel, gbc);

		model.query(filters.createRequest(app));
		updateTree();
		refreshWorkTable();
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
		edit(Arrays.stream(selection).<Transaction>mapToObj(index -> model.getTransactions().get(index))
				.collect(Collectors.toCollection(Vector::new)));
	}

	private void editSelectionInWorkZone() {
		int[] selection = workTable.getSelectedRows();
		edit(Arrays.stream(selection).<Transaction>mapToObj(index -> workZoneModel.getTransactions().get(index))
				.collect(Collectors.toCollection(Vector::new)));
	}

	private void edit(List<Transaction> transactions) {
		if (transactions.size() == 1) {
			TransactionDialog dialog = new TransactionDialog(app, this, transactions.get(0));
			dialog.setVisible(true);
		} else {
			TransactionMultipleSelectionDialog dialog = new TransactionMultipleSelectionDialog(app, this,
					new Vector<>(transactions));
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

	private DefaultMutableTreeNode createMetaNode(String id, List<Transaction> transactions,
			Function<Transaction, Object> separator) {
		DefaultMutableTreeNode result = new DefaultMutableTreeNode(
				new MetaCategory(id, app.getLangAtlas().getText("meta." + id)));
		HashMap<Object, TreeMeta> metas = new HashMap<>();
		Set<Object> keys = transactions.stream().map(separator).collect(Collectors.toSet());
		for (Object key : keys) {
			metas.put(key, new TreeMeta());
		}

		for (Transaction trans : transactions) {
			TreeMeta target = metas.get(separator.apply(trans));
			target.amount = target.amount + (trans.isOutput() ? -trans.getAmount() : trans.getAmount());
			target.count++;
			if (trans.isOutput()) {
				target.outputs++;
			} else {
				target.inputs++;
			}
			target.states[trans.getState().ordinal()]++;
		}

		for (Object key : keys) {
			TreeMeta target = metas.get(key);
			if (target.count > 0) {
				DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(key);

				for (int i = 0; i < TreeMeta.KEYS.length; i++) {
					DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode(new TreeMetaHolder(target, i));
					catNode.add(metaNode);
				}

				result.add(catNode);
			}
		}

		return result;
	}

	private void updateTree() {
		DefaultMutableTreeNode global = new DefaultMutableTreeNode("Data");
		for (Entry<String, Function<Transaction, Object>> entry : metaCategories.entrySet()) {
			global.add(createMetaNode(entry.getKey(), model.getTransactions(), entry.getValue()));
		}
		((DefaultTreeModel) queryTree.getModel()).setRoot(global);
		queryTree.treeDidChange();
		queryTree.revalidate();
		queryTree.repaint();
	}

	private void selectionChanged() {
		boolean selection = table.getSelectedColumn() != -1;
		toolbar.getComponent(1).setEnabled(selection);
		toolbar.getComponent(2).setEnabled(selection);
	}

	public void refreshTable() {
		model.query(filters.createRequest(app));
		toolbar.getComponent(3).setForeground(filters.isEmpty() ? UIManager.getColor("Label.foreground") : Color.GREEN);
		toolbar.getComponent(3).setFont(filters.isEmpty() ? UIManager.getFont("Label.font")
				: UIManager.getFont("Label.font").deriveFont(Font.BOLD));
		toolbar.repaint();
		table.revalidate();
		table.repaint();
		updateTree();
	}

	public void refreshWorkTable() {
		workZoneModel.refresh();
		workTable.revalidate();
		workTable.repaint();

		DefaultMutableTreeNode global = new DefaultMutableTreeNode("Data in work zone");
		for (Entry<String, Function<Transaction, Object>> entry : metaCategories.entrySet()) {
			global.add(createMetaNode(entry.getKey(), workZoneModel.getTransactions(), entry.getValue()));
		}
		((DefaultTreeModel) workTree.getModel()).setRoot(global);
		workTree.treeDidChange();
		workTree.revalidate();
		workTree.repaint();
	}

	public Vector<Transaction> getSelectedTransactions() {
		Vector<Transaction> res = new Vector<>();
		for (int i : table.getSelectedRows()) {
			res.add(model.getTransactions().get(i));
		}
		return res;
	}

	public Vector<Transaction> getSelectedTransactionsInWorkZone() {
		Vector<Transaction> res = new Vector<>();
		for (int i : workTable.getSelectedRows()) {
			res.add(workZoneModel.getTransactions().get(i));
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

	public TransactionWorkZoneModel getWorkZoneModel() {
		return workZoneModel;
	}

	public JTable getWorkTable() {
		return workTable;
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

	public record MetaCategory(String id, String name) {
		@Override
		public String toString() {
			return name;
		}
	}

}