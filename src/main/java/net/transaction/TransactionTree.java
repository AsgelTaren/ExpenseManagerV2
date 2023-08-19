package net.transaction;

import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.app.App;
import net.transaction.TransactionPanel.MetaCategory;

@SuppressWarnings("serial")
public class TransactionTree extends JTree {

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

	private App app;
	private TransactionProvider provider;

	public TransactionTree(App app, TransactionProvider provider) {
		super();
		this.app = app;
		this.provider = provider;

		setCellRenderer(new TransactionTreeRenderer(app));
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

	public void updateTree(String name) {
		DefaultMutableTreeNode global = new DefaultMutableTreeNode(name);
		for (Entry<String, Function<Transaction, Object>> entry : metaCategories.entrySet()) {
			global.add(createMetaNode(entry.getKey(), provider.getTransactions(), entry.getValue()));
		}
		((DefaultTreeModel) getModel()).setRoot(global);
		treeDidChange();
		revalidate();
		repaint();
	}

	public void updateTree() {
		updateTree("Data");
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
