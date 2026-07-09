package com.cartonerp.dto;

public class DashboardDTO {
    private long todayOrders;
    private long monthOutput;
    private long stockAlert;
    private double receivable;
    private double payable;
    private long totalCustomers;
    private long totalSuppliers;

    public long getTodayOrders() { return todayOrders; }
    public void setTodayOrders(long v) { this.todayOrders = v; }
    public long getMonthOutput() { return monthOutput; }
    public void setMonthOutput(long v) { this.monthOutput = v; }
    public long getStockAlert() { return stockAlert; }
    public void setStockAlert(long v) { this.stockAlert = v; }
    public double getReceivable() { return receivable; }
    public void setReceivable(double v) { this.receivable = v; }
    public double getPayable() { return payable; }
    public void setPayable(double v) { this.payable = v; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long v) { this.totalCustomers = v; }
    public long getTotalSuppliers() { return totalSuppliers; }
    public void setTotalSuppliers(long v) { this.totalSuppliers = v; }
}
