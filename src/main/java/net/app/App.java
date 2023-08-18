package net.app;

import java.io.File;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import net.account.AccountListDialog;
import net.category.CategoryListDialog;
import net.graph.GraphPanel;
import net.transaction.Transaction;
import net.transaction.TransactionPanel;

public class App {

	// UI
	private JFrame frame;
	private JMenuBar menubar;
	private JTabbedPane tabs;

	// Atlas
	private IconAtlas iconAtlas;
	private LangAtlas langAtlas;

	// DataBase
	private DataBase db;

	private HashSet<Transaction> workZone;

	public App() {

	}

	public void create() {
		// Loading all icons
		iconAtlas = new IconAtlas();
		iconAtlas.loadIcons();

		langAtlas = new LangAtlas("fr");
		langAtlas.loadLang();

		workZone = new HashSet<>();

		// Connecting to the database
		db = new DataBase(new File("testZone/test.db"));
		// db = new DataBase();

		// UI
		frame = new JFrame("ExpenseManagerV2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(iconAtlas.getRawImage("logo", 64));
		// Menu bar
		createJMenuBar();
		frame.setJMenuBar(menubar);

		// Tabs
		tabs = new JTabbedPane();
		tabs.addTab("Transactions", new TransactionPanel(this));
		tabs.addTab("Graphs", new GraphPanel(this));
		tabs.addChangeListener(e -> {
			if (tabs.getSelectedComponent() instanceof Refreshable r) {
				r.refresh();
			}
		});
		frame.add(tabs);

		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	private void createJMenuBar() {
		menubar = new JMenuBar();

		// Data Menu
		JMenu data = new JMenu(langAtlas.getText("menu.data"));

		JMenuItem accounts = new JMenuItem(langAtlas.getText("menu.account.show"));
		accounts.setIcon(iconAtlas.getIcon("list", 16));
		accounts.addActionListener(e -> {
			AccountListDialog dialog = new AccountListDialog(this);
			dialog.setVisible(true);
		});
		data.add(accounts);

		JMenuItem categories = new JMenuItem(langAtlas.getText("menu.categories.show"));
		categories.setIcon(iconAtlas.getIcon("list", 16));
		categories.addActionListener(e -> {
			CategoryListDialog dialog = new CategoryListDialog(this);
			dialog.setVisible(true);
		});
		data.add(categories);

		menubar.add(data);
	}

	public JFrame getJFrame() {
		return frame;
	}

	public DataBase getDataBase() {
		return db;
	}

	public IconAtlas getIconAtlas() {
		return iconAtlas;
	}

	public LangAtlas getLangAtlas() {
		return langAtlas;
	}

	public HashSet<Transaction> getWorkZone() {
		return workZone;
	}
}