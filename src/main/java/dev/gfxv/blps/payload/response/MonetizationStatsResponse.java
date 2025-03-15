package dev.gfxv.blps.payload.response;

public class MonetizationStatsResponse {
    private double totalEarnings;
    private double earningsSinceLastWithdrawal;

    public MonetizationStatsResponse(double totalEarnings, double earningsSinceLastWithdrawal) {
        this.totalEarnings = totalEarnings;
        this.earningsSinceLastWithdrawal = earningsSinceLastWithdrawal;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public double getEarningsSinceLastWithdrawal() {
        return earningsSinceLastWithdrawal;
    }

    public void setEarningsSinceLastWithdrawal(double earningsSinceLastWithdrawal) {
        this.earningsSinceLastWithdrawal = earningsSinceLastWithdrawal;
    }
}
