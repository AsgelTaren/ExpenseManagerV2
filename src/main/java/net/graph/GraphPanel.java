package net.graph;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries;
import org.knowm.xchart.PieSeries.PieSeriesRenderStyle;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.PieStyler.AnnotationType;

import net.account.Account;
import net.app.App;
import net.app.Refreshable;
import net.category.Category;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements Refreshable {

	// App reference
	private App app;
	private HashMap<Integer, Category> categories;
	private HashMap<Integer, Account> accounts;

	private PieChart inputChart, outputChart;
	private XChartPanel<PieChart> inputPanel, outputPanel;

	private CategoryChart perMonth;
	private XChartPanel<CategoryChart> perMonthPanel;

	private PieChart perAccount;
	private XChartPanel<PieChart> perAccountPanel;

	public GraphPanel(App app) {
		super();

		this.app = app;

		inputChart = new PieChartBuilder().width(600).height(400).title("Inputs").build();
		setPieChartStyle(inputChart);
		inputPanel = new XChartPanel<>(inputChart);
		add(inputPanel);

		outputChart = new PieChartBuilder().width(600).height(400).title("Outputs").build();
		setPieChartStyle(outputChart);
		outputPanel = new XChartPanel<>(outputChart);
		add(outputPanel);

		perAccount = new PieChartBuilder().width(600).height(400).title("Per Account").build();
		setPieChartStyle(perAccount);
		perAccountPanel = new XChartPanel<>(perAccount);
		add(perAccountPanel);

		perMonth = new CategoryChartBuilder().width(1800).height(400).title("Per Month").build();
		perMonth.getStyler().setDatePattern("yyyy-MM");
		perMonth.getStyler().setToolTipsEnabled(true);
		perMonth.getStyler().setyAxisTickLabelsFormattingFunction(d -> d.intValue() + "\u20ac");
		setCategoryChartStyle(perMonth);
		perMonthPanel = new XChartPanel<>(perMonth);
		add(perMonthPanel);

		refresh();

		try {
			BitmapEncoder.saveBitmap(inputChart, "./testZone/test.png", BitmapFormat.PNG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void queryCategories() {
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from categories;");
			categories = new HashMap<>();
			while (set.next()) {
				categories.put(set.getInt("id"), new Category(set.getInt("id"), set.getString("name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void queryAccounts() {
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery("select * from accounts;");
			accounts = new HashMap<>();
			while (set.next()) {
				accounts.put(set.getInt("id"),
						new Account(set.getInt("id"), set.getString("name"), set.getFloat("balance")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void queryFlows(PieChart target, boolean output) {
		Set<Entry<String, PieSeries>> data = target.getSeriesMap().entrySet();
		Iterator<Entry<String, PieSeries>> it = data.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		try {
			PreparedStatement st = app.getDataBase().getConnection().prepareStatement(
					"select sum(amount) as \"total\",categories.name as \"cat_name\" from transactions"
							+ " join categories on categories.id = transactions.category where transactions.output = ? and categories.id != 6 group by categories.id;");
			st.setInt(1, output ? 1 : 0);
			ResultSet set = st.executeQuery();
			while (set.next()) {
				if (set.getFloat("total") > 0)
					target.addSeries(set.getString("cat_name"), set.getFloat("total"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void queryPerMonth() {
		// Removing old series from the chart
		Set<Entry<String, CategorySeries>> data = perMonth.getSeriesMap().entrySet();
		Iterator<Entry<String, CategorySeries>> it = data.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}

		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery(
					"select substring(date_application,1,7) as date_cropped, sum(case when output=1 then -amount else amount end) as \"sum\",category from transactions where category != 6"
							+ " group by date_cropped,category;");
			// Lists to store final data
			List<Date> dates = new ArrayList<>();
			HashMap<Integer, ArrayList<Float>> map = new HashMap<Integer, ArrayList<Float>>();
			categories.values().stream().forEach(cat -> map.put(cat.getId(), new ArrayList<>()));

			Date last = null;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
			try {
				while (set.next()) {
					Date target = format.parse(set.getString("date_cropped"));
					if (!target.equals(last)) {
						last = target;
						categories.values().stream().forEach(cat -> map.get(cat.getId()).add(0f));
						dates.add(target);
					}
					ArrayList<Float> array = map.get(set.getInt("category"));
					array.set(array.size() - 1, set.getFloat("sum"));
				}
				categories.values().stream().forEach(cat -> {
					if (map.get(cat.getId()).size() > 0)
						perMonth.addSeries(cat.getName(), dates, map.get(cat.getId()));
				});

			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void queryPerAccount() {
		// Removing old series from the chart
		Set<Entry<String, PieSeries>> data = perAccount.getSeriesMap().entrySet();
		Iterator<Entry<String, PieSeries>> it = data.iterator();
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
		try {
			ResultSet set = app.getDataBase().getStatement().executeQuery(
					"select sum(case when output=1 then -amount else amount end) + ifnull(accounts.balance,0) as \"sum\", accounts.name as \"account\" from transactions join accounts on accounts.id = transactions.account group by account;");
			while (set.next()) {
				perAccount.addSeries(set.getString("account"), set.getFloat("sum"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refresh() {
		queryCategories();
		queryAccounts();
		queryFlows(inputChart, false);
		inputPanel.revalidate();
		inputPanel.repaint();

		queryFlows(outputChart, true);
		outputPanel.revalidate();
		outputPanel.repaint();

		queryPerMonth();
		perMonthPanel.revalidate();
		perMonthPanel.repaint();

		queryPerAccount();
		perAccountPanel.revalidate();
		perAccountPanel.repaint();
	}

	private void setPieChartStyle(PieChart target) {
		target.getStyler().setDefaultSeriesRenderStyle(PieSeriesRenderStyle.Donut);
		target.getStyler().setSumVisible(true);
		target.getStyler().setPlotContentSize(0.5);
		target.getStyler().setAnnotationDistance(1.6);
		target.getStyler().setAnnotationType(AnnotationType.Value);
		target.getStyler().setDrawAllAnnotations(true);
		target.getStyler().setChartBackgroundColor(UIManager.getColor("Panel.background"));
		target.getStyler().setAnnotationsFontColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setPlotBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setLegendBorderColor(UIManager.getColor("Panel.foreground"));
		target.getStyler().setLegendBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setChartFontColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setLegendFont(target.getStyler().getLegendFont().deriveFont(15.0f));
		target.getStyler().setAnnotationsFont(target.getStyler().getAnnotationsFont().deriveFont(18.0f));
		target.getStyler().setToolTipBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setToolTipFont(target.getStyler().getToolTipFont().deriveFont(18.0f));
		target.getStyler().setChartTitleFont(target.getStyler().getChartTitleFont().deriveFont(18.0f));
		target.getStyler().setSumFormat("%.2f\u20ac");
		target.getStyler().setSumFont(target.getStyler().getSumFont().deriveFont(18.0f));
	}

	private void setCategoryChartStyle(CategoryChart target) {
		target.getStyler().setChartBackgroundColor(UIManager.getColor("Panel.background"));
		target.getStyler().setAnnotationsFontColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setPlotBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setLegendBorderColor(UIManager.getColor("Panel.foreground"));
		target.getStyler().setLegendBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setChartFontColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setLegendFont(target.getStyler().getLegendFont().deriveFont(15.0f));
		target.getStyler().setAnnotationsFont(target.getStyler().getAnnotationsFont().deriveFont(18.0f));
		target.getStyler().setToolTipBackgroundColor(UIManager.getColor("TextField.background"));
		target.getStyler().setToolTipFont(target.getStyler().getToolTipFont().deriveFont(18.0f));
		target.getStyler().setChartTitleFont(target.getStyler().getChartTitleFont().deriveFont(18.0f));
		target.getStyler().setXAxisTickLabelsColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setYAxisTickLabelsColor(UIManager.getColor("TextField.foreground"));
		target.getStyler().setAxisTickLabelsFont(target.getStyler().getAxisTickLabelsFont().deriveFont(15.0f));
	}

}