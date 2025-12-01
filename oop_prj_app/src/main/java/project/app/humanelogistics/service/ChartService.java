package project.app.humanelogistics.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ChartService {

    // Palette of colors to cycle through for multiple lines
    private static final Color[] SERIES_COLORS = {
            new Color(230, 126, 34),  // Orange
            new Color(52, 152, 219),  // Blue
            new Color(46, 204, 113),  // Green
            new Color(155, 89, 182),  // Purple
            new Color(231, 76, 60)    // Red
    };

    // --- UPDATED RESOLUTION SETTINGS ---
    private static final int CHART_WIDTH = 1600;
    private static final int CHART_HEIGHT = 1000;

    public File generateAndSaveChart(String title, String xAxis, String yAxis, TimeSeriesCollection dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, xAxis, yAxis,
                dataset, true, true, false
        );

        // Apply visual improvements
        applyTheme(chart);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, SERIES_COLORS[i % SERIES_COLORS.length]);
            // Thicker lines for high resolution
            renderer.setSeriesStroke(i, new BasicStroke(5.0f));
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesShapesFilled(i, true);
        }

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setDateFormatOverride(new SimpleDateFormat("MMM dd"));
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));

        // Font scaling for axis
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 18));
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 20));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 18));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 20));

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
        return file;
    }

    public File generateBarChart(String title, String xAxis, String yAxis, DefaultCategoryDataset dataset, String filepath) throws IOException {
        JFreeChart chart = ChartFactory.createBarChart(
                title, xAxis, yAxis,
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        applyTheme(chart);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Font scaling for axis
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 18));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 20));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 18));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 20));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(52, 152, 219)); // Blue bars
        renderer.setDrawBarOutline(false);

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
        return file;
    }

    public File generatePieChart(String title, DefaultCategoryDataset categoryDataset, String filepath) throws IOException {
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (int i = 0; i < categoryDataset.getColumnCount(); i++) {
            Comparable key = categoryDataset.getColumnKey(i);
            Number value = categoryDataset.getValue(0, i);
            pieDataset.setValue(key, value);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                title,
                pieDataset,
                true, // legend
                true, // tooltips
                false // urls
        );

        applyTheme(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 18)); // Larger labels
        plot.setNoDataMessage("No data available");
        plot.setCircular(true);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(Color.WHITE);

        for (int i = 0; i < pieDataset.getItemCount(); i++) {
            plot.setSectionPaint(pieDataset.getKey(i), SERIES_COLORS[i % SERIES_COLORS.length]);
        }

        File file = new File(filepath);
        ChartUtils.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
        return file;
    }


    private void applyTheme(JFreeChart chart) {
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        chart.setBackgroundPaint(Color.WHITE);
        if (chart.getPlot() != null) {
            chart.getPlot().setBackgroundPaint(Color.WHITE);
        }

        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 36));
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 22));
        }
    }
}