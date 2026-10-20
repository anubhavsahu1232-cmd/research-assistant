package com.researchassistant.backend.auth;
import com.researchassistant.backend.model.User;
import com.researchassistant.backend.repository.UserRepository;
import com.researchassistant.backend.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt;
 public AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt){this.users=users;this.encoder=encoder;this.jwt=jwt;}
 public record Register(@NotBlank @Size(max=50) String username,@Email @NotBlank String email,@NotBlank @Size(min=8,max=72) String password){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 @PostMapping("/register") public ResponseEntity<?> register(@Valid @RequestBody Register request){
  if(users.existsByEmail(request.email())||users.existsByUsername(request.username())) return ResponseEntity.status(409).body(Map.of("error","Email or username already exists"));
  User user=new User();user.username=request.username();user.email=request.email();user.passwordHash=encoder.encode(request.password());user.role=User.Role.USER;users.save(user);
  return ResponseEntity.status(201).body(Map.of("token",jwt.generate(user.email),"userId",user.id,"role",user.role.name()));
 }
 @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody Login request){
  var user=users.findByEmail(request.email());
  if(user.isEmpty()||!encoder.matches(request.password(),user.get().passwordHash)) return ResponseEntity.status(401).body(Map.of("error","Invalid credentials"));
  return ResponseEntity.ok(Map.of("token",jwt.generate(user.get().email),"userId",user.get().id,"role",user.get().role.name()));
 }
}
