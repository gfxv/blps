package dev.gfxv.blps.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "subscribers")
    private Long subscribers = 0L;

    @Column(name = "total_views", nullable = false)
    private Long totalViews = 0L; // default to 0

    @Column(name = "is_monetized", nullable = false)
    private boolean isMonetized = false;

    @Column(name = "last_withdrawal_amount", nullable = false)
    private Double lastWithdrawalAmount = 0.0;

}