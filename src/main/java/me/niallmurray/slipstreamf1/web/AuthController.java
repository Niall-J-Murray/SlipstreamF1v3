package me.niallmurray.slipstreamf1.web;

import jakarta.validation.Valid;
import me.niallmurray.slipstreamf1.domain.ERole;
import me.niallmurray.slipstreamf1.domain.Role;
import me.niallmurray.slipstreamf1.domain.User;
import me.niallmurray.slipstreamf1.payload.request.LoginRequest;
import me.niallmurray.slipstreamf1.payload.request.SignupRequest;
import me.niallmurray.slipstreamf1.payload.response.JwtResponse;
import me.niallmurray.slipstreamf1.payload.response.MessageResponse;
import me.niallmurray.slipstreamf1.repositories.RoleRepository;
import me.niallmurray.slipstreamf1.repositories.UserRepository;
import me.niallmurray.slipstreamf1.security.jwt.JwtUtils;
import me.niallmurray.slipstreamf1.security.services.UserDetailsImpl;
import me.niallmurray.slipstreamf1.service.TeamService;
import me.niallmurray.slipstreamf1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  UserService userService;
  @Autowired
  TeamService teamService;
  @Autowired
  AuthenticationManager authenticationManager;
  @Autowired
  UserRepository userRepository;
  @Autowired
  RoleRepository roleRepository;
  @Autowired
  PasswordEncoder encoder;
  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()));


    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "admin" -> {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);
          }
          case "mod" -> {
            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(modRole);
          }
          default -> {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
          }
        }
      });
    }

    user.setRoles(roles);


    userService.createUser(user);
    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

    teamService.deleteExpiredTestTeams();

    return ResponseEntity.ok(new JwtResponse(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles));
  }

  @PostMapping("/signout")
  public ResponseEntity<?> updateUserLogout(@Valid @RequestBody String request) {
    Long userId = Long.valueOf(request.substring(10, (request.length() - 1)));

    User user = userRepository.findById(userId).orElseThrow(null);
    if (user != null) {
      userService.updateLastLogout(userId);
      if (user.getTeam() != null) {
        teamService.saveAllTeams(user.getTeam().getLeague());
      }
    }
    return ResponseEntity.ok(new MessageResponse("Logout Successful"));
  }
}
