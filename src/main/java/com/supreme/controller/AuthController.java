package com.supreme.controller;

import com.supreme.config.JwtProvider;
import com.supreme.model.User;
import com.supreme.repository.UserRepository;
import com.supreme.request.LoginRequest;
import com.supreme.response.AuthResponse;
import com.supreme.service.CustomUserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private UserRepository userRepository;

    private BCryptPasswordEncoder PasswordEncoder;

    private CustomUserServiceImplementation serviceImplementation;


    @Autowired
    public AuthController(UserRepository userRepository,  CustomUserServiceImplementation serviceImplementation) {
        this.userRepository = userRepository;
        this.PasswordEncoder = new BCryptPasswordEncoder();
        this.serviceImplementation = serviceImplementation;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserhandler(@RequestBody User user)throws Exception {

            String email = user.getEmail();
            String password = user.getPassword();
            String fullName = user.getFullName();
            String roles = user.getRole();

            User isEmailExist = userRepository.findByEmail(email);

            if (isEmailExist!=null){
                throw new Exception("Email Already used with Another Account");
            }

            //create user
        User createUser = new User();
            createUser.setEmail(email);
            createUser.setFullName(fullName);
            createUser.setRole(roles);
            createUser.setPassword(PasswordEncoder.encode(password));

            User savedUser = userRepository.save(createUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = JwtProvider.genrationToken(authentication);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("Register Success");
        authResponse.setStatus(true);

        return new ResponseEntity<AuthResponse>(authResponse, HttpStatus.OK);
    }
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest request){

        String userName = request.getEmail();
        String password = request.getPassword();

        System.out.println(userName + " ----- " + password);

        Authentication authentication = authenticate(userName, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = JwtProvider.genrationToken(authentication);
        AuthResponse authResponse = new AuthResponse();

        authResponse.setMessage("Login success");
        authResponse.setJwt(token);
        authResponse.setStatus(true);

        return new ResponseEntity<AuthResponse>(authResponse,HttpStatus.OK);
    }

    private Authentication authenticate(String userName, String password) {
        UserDetails userDetails = serviceImplementation.loadUserByUsername(userName);

        System.out.println("signIn userDeatails - null " + userDetails);

        if (userDetails == null){
            throw  new BadCredentialsException("Invalid username or password");
        }
        if (!PasswordEncoder.matches(password, userDetails.getPassword())){
            System.out.println("signIn userDetails - password not match " + userDetails);
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
    }
}
