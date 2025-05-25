package com.securemsg.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@Document("users")
@ToString(exclude = {"password", "photo"})
public class User implements UserDetails {
    @Id
    @NotBlank
    @Size(min = 12, max = 12)
    private String iin;

    @Indexed(unique = true)
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String fullName;

    private String phone;
    private String gender;
    private LocalDate birthDate;

    @NotBlank
    private String role;
    private String photo;
    private String theme;
    private String language;

    @Override
    public String getUsername() {
        return this.iin;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = (this.role != null ? this.role.toUpperCase() : "STUDENT");
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}