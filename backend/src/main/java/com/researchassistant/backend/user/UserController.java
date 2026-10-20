package com.researchassistant.backend.user;
import com.researchassistant.backend.model.User;
import com.researchassistant.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController public class UserController {
 private final UserRepository users;private final PasswordEncoder encoder;
 public UserController(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public record Profile(Long id,String username,String email,String role){}
 private Profile view(User u){return new Profile(u.id,u.username,u.email,u.role.name());}
 @GetMapping("/api/users/me") public Profile me(Authentication a){return view(users.findByEmail(a.getName()).orElseThrow());}
 public record Update(@NotBlank @Size(max=50) String username){}
 @PutMapping("/api/users/me") public Profile update(@Valid @RequestBody Update body,Authentication a){
  User u=users.findByEmail(a.getName()).orElseThrow();
  if(users.existsByUsername(body.username())&&!u.username.equals(body.username())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,"Username taken");
  u.username=body.username();return view(users.save(u));
 }
 @PreAuthorize("hasRole('ADMIN')") @GetMapping("/api/admin/users") public List<Profile> all(){return users.findAll().stream().map(this::view).toList();}
 public record RoleChange(@NotNull User.Role role){}
 @PreAuthorize("hasRole('ADMIN')") @PatchMapping("/api/admin/users/{id}/role") public Profile role(@PathVariable Long id,@Valid @RequestBody RoleChange body){
  User u=users.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));u.role=body.role();return view(users.save(u));
 }
}
