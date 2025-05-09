package dev.gfxv.blps.controller;

import dev.gfxv.blps.payload.response.MonetizationStatsResponse;
import dev.gfxv.blps.service.MonetizationService;
import dev.gfxv.blps.service.VideoService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    MonetizationService monetizationService;

    public UserController(MonetizationService monetizationService) {
        this.monetizationService = monetizationService;
    }

    @PostMapping("/me/request-monetization")
    public ResponseEntity<String> requestMonetization(Authentication authentication) {
        String username = getUsernameFromAuthentication(authentication);
        boolean success = monetizationService.requestMonetization(username);
        if (success) {
            return ResponseEntity.ok("Monetization request accepted and enabled");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not eligible for monetization");
        }
    }

    @GetMapping("/me/stats")
    public ResponseEntity<MonetizationStatsResponse> getMonetizationStats(Authentication authentication) {
        String username = getUsernameFromAuthentication(authentication);
        MonetizationStatsResponse stats = monetizationService.getMonetizationStats(username);
        return ResponseEntity.ok(stats);
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        return authentication == null ? "" : authentication.getName();
    }

    @PostMapping("/me/withdraw")
    public ResponseEntity<String> withdrawEarnings(
            @RequestParam Long amount,
            Authentication authentication) {
        String userName = authentication.getName();
        monetizationService.withdrawEarningsWithStripe(userName, amount);
        return ResponseEntity.ok("Withdrawal processed successfully");
    }
}
