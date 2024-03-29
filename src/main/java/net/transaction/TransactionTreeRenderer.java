package net.transaction;

import java.awt.Component;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.account.Account;
import net.app.App;
import net.category.Category;
import net.transaction.TransactionPanel.MetaCategory;
import net.transaction.TransactionTree.TreeMeta;
import net.transaction.TransactionTree.TreeMetaHolder;

@SuppressWarnings("serial")
public class TransactionTreeRenderer extends DefaultTreeCellRenderer {

	private ImageIcon catClosedIcon, catOpenIcon;
	private ImageIcon[] metaIcons;
	private String[] metaPrefixes;
	private HashMap<String, ImageIcon> metaCatsIcons;

	public TransactionTreeRenderer(App app) {
		super();
		catClosedIcon = app.getIconAtlas().getIcon("cat-closed", 16);
		catOpenIcon = app.getIconAtlas().getIcon("cat-open", 16);

		metaIcons = new ImageIcon[TreeMeta.KEYS.length];
		metaPrefixes = new String[metaIcons.length];
		for (int i = 0; i < metaIcons.length; i++) {
			metaIcons[i] = app.getIconAtlas().getIcon(TreeMeta.KEYS[i], 16);
			metaPrefixes[i] = app.getLangAtlas().getText("meta." + TreeMeta.KEYS[i]);
		}
		metaCatsIcons = new HashMap<>();
		for (String key : TransactionTree.metaCategories.keySet()) {
			metaCatsIcons.put(key, app.getIconAtlas().getIcon(key, 16));
		}
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		JLabel res = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		res.setIcon(null);
		Object target = ((DefaultMutableTreeNode) value).getUserObject();
		if (target instanceof TreeMetaHolder holder) {
			res.setText("<html><nobr>" + metaPrefixes[holder.getType()] + ": <font color='#ffbebe'>"
					+ (TreeMeta.IS_INT[holder.getType()] ? Integer.toString((int) holder.getValue())
							: String.format("%.2f", holder.getValue()))
					+ "</font></nobr></html>");
			res.setIcon(metaIcons[holder.getType()]);
		}
		if (target instanceof Category | target instanceof Account | target instanceof YearMonth
				| target instanceof Year) {
			res.setIcon(expanded ? catOpenIcon : catClosedIcon);
		}
		if (target instanceof MetaCategory cat) {
			res.setIcon(metaCatsIcons.get(cat.id()));
		}
		return res;
	}
}
