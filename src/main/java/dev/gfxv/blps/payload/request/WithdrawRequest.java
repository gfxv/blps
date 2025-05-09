package dev.gfxv.blps.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawRequest {

    @NotNull
    private Long userId;

    @DecimalMin(value = "10")
    private Double amount;
}
