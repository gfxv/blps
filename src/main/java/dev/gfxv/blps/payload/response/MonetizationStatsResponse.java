package dev.gfxv.blps.payload.response;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonetizationStatsResponse {
    @DecimalMin(value = "0.0", message = "Total earnings cannot be negative")
    private double totalEarnings;

    @DecimalMin(value = "0.0", message = "Earnings since last withdrawal cannot be negative")
    private double earningsSinceLastWithdrawal;
}
