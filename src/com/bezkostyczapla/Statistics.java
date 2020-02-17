package com.bezkostyczapla;

import java.util.ArrayList;

public class Statistics
{
    private String name;
    private double average;
    private double max;
    private double min;

    public Statistics(String name, double average, double max, double min)
    {
        this.name = name;
        this.average = average;
        this.max = max;
        this.min = min;
    }

    public static Statistics calculateStatistics(ArrayList<Currency> currencies)
    {
        if(currencies.size() == 0) return null;
        double max = 0, min = Double.MAX_VALUE;
        double sum = 0;
        String curr = currencies.get(0).getCurrency();
        for (var el:currencies)
        {
            sum += el.getMid();
            if(max < el.getMid())
                max = el.getMid();
            if(min > el.getMid())
                min = el.getMid();
        }
        double avg = sum / currencies.size();
        return new Statistics(curr, avg, max, min);
    }

    public String getName()
    {
        return name;
    }

    public double getAverage()
    {
        return average;
    }

    public double getMax()
    {
        return max;
    }

    public double getMin()
    {
        return min;
    }

    public static Statistics calculateStats(ArrayList<Gold> gold)
    {
        if(gold.size() == 0) return null;
        double max = 0, min = Double.MAX_VALUE;
        double sum = 0;
        String goldName = "Gold";
        for (var el:gold)
        {
            sum += el.getPrice();
            if(max < el.getPrice())
                max = el.getPrice();
            if(min > el.getPrice())
                min = el.getPrice();
        }
        double avg = sum / gold.size();
        return new Statistics(goldName, avg, max, min);
    }
    @Override
    public String toString()
    {
        return "Statistics for: " + name.toUpperCase() + ", max value: " + Double.toString(max) + ", min value: " + Double.toString(min) +
                ",average value: " + Double.toString(average);
    }
}
