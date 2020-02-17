package com.bezkostyczapla;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Chart extends JFrame
{
        private ArrayList<Statistics> stats;
        private Statistics goldStats;
        private static final long serialVersionUID = 6294689542092367723L;

        public Chart(String title, ArrayList<Gold> golds, Statistics goldStats) {
            super(title);
            this.goldStats=goldStats;
            // Create dataset
            XYDataset dataset = createDataset(golds,0);

            // Create chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    " ",
                    "Time [days]",
                    "Gold Rate [zł / g]",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false);


            // Create Panel
            ChartPanel panel = new ChartPanel(chart);
            JButton button = new JButton("Display Statistics!");
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JFrame frame = new JFrame("Statistics");
                    frame.setSize(800, 400);
                    frame.setLocationRelativeTo(panel);

                    frame.setVisible(true);

                    frame.setMinimumSize(new Dimension(800, 400));
                    statisticsForm statsF=new statisticsForm();
                    statsF.getStatisticsList().setModel(new DefaultListModel());
                    DefaultListModel<String> model = (DefaultListModel<String>) statsF.getStatisticsList().getModel();

                    model.addElement("Statistcs for: " + goldStats.getName());
                    model.addElement("Minimum Value: " + goldStats.getMin());
                    model.addElement("Maximum Value: " + goldStats.getMax());
                    model.addElement("Average Value: " + goldStats.getAverage());
                    model.addElement(" ");


                    frame.setContentPane(statsF.getMainPanel());

                    frame.setVisible(true);
                }
            });
            panel.add(button);

            setContentPane(panel);
        }

    public Chart(String title, ArrayList<ArrayList<Currency>> currencies, ArrayList<Statistics> stats) {
        super(title);

        this.stats=stats;

        XYDataset dataset = createDataset(currencies);

        // Create chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                " ",
                "Time [days]",
                "Currency Rate [zł]",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Create Panel
        ChartPanel panel = new ChartPanel(chart);
        JButton button = new JButton("Display Statistics!");
        button.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFrame frame = new JFrame("Statistics");
                frame.setSize(800, 400);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

                frame.setMinimumSize(new Dimension(800, 400));
                statisticsForm statsF=new statisticsForm();
                statsF.getStatisticsList().setModel(new DefaultListModel());
                DefaultListModel<String> model = (DefaultListModel<String>) statsF.getStatisticsList().getModel();

                for(var el:stats) {
                    model.addElement("Statistcs for: " + el.getName());
                    model.addElement("Minimum Value: " + el.getMin());
                    model.addElement("Maximum Value: " + el.getMax());
                    model.addElement("Average Value: " + el.getAverage());
                    model.addElement(" ");
                }

                frame.setContentPane(statsF.getMainPanel());
                frame.setVisible(true);
            }
        });
        panel.add(button);
        this.getContentPane().add(panel, BorderLayout.SOUTH);

        setContentPane(panel);
    }

        private XYDataset createDataset(ArrayList<Gold> golds,int k) {
            XYSeriesCollection dataset = new XYSeriesCollection();

            XYSeries series = new XYSeries("Gold");
            double i = 0;
            for(var gold:golds)
            {
                i++;
                series.add(i, gold.getPrice());
            }
            //Add series to dataset
            dataset.addSeries(series);
            return dataset;
        }

    private XYDataset createDataset(ArrayList<ArrayList<Currency>> currencies) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (var el:currencies)
        {
            if(el.size() <= 0) continue;
            XYSeries series = new XYSeries(el.get(0).getCode());
            double i = 0;
            for(var el2:el)
            {
                i++;
                series.add(i, el2.getMid());
            }
            //Add series to dataset
            dataset.addSeries(series);
        }
        return dataset;
    }
}
