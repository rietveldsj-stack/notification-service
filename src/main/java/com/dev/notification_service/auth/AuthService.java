package com.dev.notification_service.auth;

import com.dev.notification_service.auth.dto.AuthResponse;
import com.dev.notification_service.auth.dto.LoginRequest;
import com.dev.notification_service.auth.dto.RegisterRequest;
import com.dev.notification_service.exceptionHandling.ConflictException;
import com.dev.notification_service.exceptionHandling.UnauthorizedException;
import com.dev.notification_service.security.JwtUtil;
import com.dev.notification_service.user.User;
import com.dev.notification_service.user.UserRepository;
import com.dev.notification_service.user.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request){

        if (userRepository.existsByEmail(request.email())){
            throw new ConflictException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .username(request.username())
                .password(hashedPassword)
                .email(request.email())
                .role(UserRole.USER)
                .build();


        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token);
    }

    public AuthResponse login (LoginRequest request) {

        UserDetails userDetails = loadUserByUsername(request.username());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())){
            throw new UnauthorizedException("Username or password incorrect");
        }

        String token = jwtUtil.generateToken(request.username());
        return new AuthResponse(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username or password incorrect"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
