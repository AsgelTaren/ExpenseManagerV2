package net.graph;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.PieSeries;
import org.knowm.xchart.PieSeries.PieSeriesRenderStyle;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.PieStyler.AnnotationType;

import net.app.App;
import net.app.Refreshable;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements Refreshable {

	// App reference
	private App app;
	private PieChart inputChart, outputChart;
	private XChartPanel<PieChart> inputPanel, outputPanel;

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

		refresh();
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
							+ " join categories on categories.id = transactions.category where transactions.output = ? group by categories.id;");
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

	@Override
	public void refresh() {
		queryFlows(inputChart, false);
		inputPanel.revalidate();
		inputPanel.repaint();

		queryFlows(outputChart, true);
		outputPanel.revalidate();
		outputPanel.repaint();
	}

	private void setPieChartStyle(PieChart target) {
		target.getStyler().setDefaultSeriesRenderStyle(PieSeriesRenderStyle.Donut);
		target.getStyler().setSumVisible(true);
		target.getStyler().setPlotContentSize(0.5);
		target.getStyler().setAnnotationDistance(1.6);
		target.getStyler().setAnnotationType(AnnotationType.Percentage);
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
		target.getStyler().setToolTipsEnabled(true);
	}

}