package com.cartonerp.dto;

import java.util.List;

public class ProfitAnalysisDTO {
    private double revenue;
    private double cost;
    private double profit;
    private int year;
    private int month;
    private List<MonthlyData> monthly;

    public static class MonthlyData {
        private int month;
        private double revenue;
        private double cost;
        private double profit;
        public MonthlyData(int m, double r, double c, double p) { month = m; revenue = r; cost = c; profit = p; }
        public int getMonth() { return month; }
        public double getRevenue() { return revenue; }
        public double getCost() { return cost; }
        public double getProfit() { return profit; }
    }

    public double getRevenue() { return revenue; }
    public void setRevenue(double v) { this.revenue = v; }
    public double getCost() { return cost; }
    public void setCost(double v) { this.cost = v; }
    public double getProfit() { return profit; }
    public void setProfit(double v) { this.profit = v; }
    public int getYear() { return year; }
    public void setYear(int v) { this.year = v; }
    public int getMonth() { return month; }
    public void setMonth(int v) { this.month = v; }
    public List<MonthlyData> getMonthly() { return monthly; }
    public void setMonthly(List<MonthlyData> v) { this.monthly = v; }
}
