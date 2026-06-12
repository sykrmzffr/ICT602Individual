package com.syakir.electricitycalculator;

/**
 * Model class representing a single electricity bill record.
 */
public class BillRecord {
    private int id;
    private String month;
    private double units;
    private double totalCharges;
    private double rebatePercent;
    private double finalCost;

    public BillRecord() {
    }

    public BillRecord(String month, double units, double totalCharges, double rebatePercent, double finalCost) {
        this.month = month;
        this.units = units;
        this.totalCharges = totalCharges;
        this.rebatePercent = rebatePercent;
        this.finalCost = finalCost;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public double getRebatePercent() {
        return rebatePercent;
    }

    public void setRebatePercent(double rebatePercent) {
        this.rebatePercent = rebatePercent;
    }

    public double getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    }
}
