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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.app.App;
import net.app.WorkablePanel;
import net.transaction.filter.FilterDialog;
import net.transaction.filter.FilterOptions;

@SuppressWarnings("serial")
public class TransactionPanel extends JPanel implements KeyListener, WorkablePanel {

	private App app;

	private JTable table, workTable;
	private TransactionTableModel model;
	private TransactionWorkZoneModel workZoneModel;
	private JToolBar toolbar;
	private TransactionTree queryTree, workTree;
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

		queryTree = new TransactionTree(app, model);
		queryTree.setTransferHandler(transferHandler);
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		queryPanel.add(new JScrollPane(queryTree), gbc);

		JPanel workZone = new JPanel();
		workZone.setLayout(new GridBagLayout());
		workZoneModel = new TransactionWorkZoneModel(app);
		workTree = new TransactionTree(app, workZoneModel);
		workTree.setTransferHandler(transferHandler);
		gbc.gridx = 0;
		gbc.gridy = 0;
		workZone.add(new JScrollPane(workTree), gbc);

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
		gbc.gridx = 1;
		gbc.weightx = 2;
		workZone.add(new JScrollPane(workTable), gbc);
		workZone.setBorder(BorderFactory.createTitledBorder("Work Zone"));

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 3;
		queryPanel.add(workZone, gbc);

		gbc.weightx = gbc.weighty = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridy = 1;
		gbc.gridx = 0;
		add(queryPanel, gbc);

		model.query(filters.createRequest(app));
		queryTree.updateTree();
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
		queryTree.updateTree();
	}

	public void refreshWorkTable() {
		workZoneModel.refresh();
		workTable.revalidate();
		workTable.repaint();
		workTree.updateTree();
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
		if ((e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_A)
				|| e.getKeyCode() == KeyEvent.VK_ADD) {
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

	public record MetaCategory(String id, String name) {
		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public List<Transaction> getWorkedOnTransactions() {
		return getSelectedTransactionsInWorkZone();
	}

	@Override
	public void addTransactionsToWork(Collection<Transaction> data) {
		app.getWorkZone().addAll(data);

	}

	@Override
	public void refresh() {
		refreshWorkTable();
	}

}