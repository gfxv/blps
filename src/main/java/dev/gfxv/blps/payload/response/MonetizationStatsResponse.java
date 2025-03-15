package dev.gfxv.blps.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonetizationStatsResponse {
    private double totalEarnings;
    private double earningsSinceLastWithdrawal;
}
