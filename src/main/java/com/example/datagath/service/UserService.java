package com.example.datagath.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.example.datagath.dto.LoginForm;
import com.example.datagath.dto.SessionResponse;
import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.SessionToken;
import com.example.datagath.model.User;
import com.example.datagath.repository.CollectionTableRepository;
import com.example.datagath.repository.SessionTokenRepository;
import com.example.datagath.repository.UserRepository;

import java.util.List;
import java.util.ArrayList;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SessionTokenRepository sessionTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CollectionTableRepository collectionTableRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SessionTokenRepository sessionTokenRepository, CollectionTableRepository collectionTableRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenRepository = sessionTokenRepository;
        this.collectionTableRepository = collectionTableRepository;

    }


    



    public User validateSessionToken(String token) {

        SessionToken t = sessionTokenRepository.findByToken(token).orElse(null);

        Instant now = Instant.now();
        if (t != null) {
            if (now.isBefore(t.getExpiryDate())) {

                User user = userRepository.findById(t.getUserId()).orElse(null);

                if (user != null) {
                    return user;
                }
            }

        }
        return null;
    }

    public List<CollectionTable> GetAllDatasetsOfAUser(User user) {
        List<CollectionTable> datasets = new ArrayList<>();
        if (user != null) {
            Long userId = user.getId();

            List<CollectionTable> collections = collectionTableRepository.findByOwnerId(userId);

            for (CollectionTable collectionTable : collections) {
                datasets.add(collectionTable);
            }

        } else {
            return datasets;
        }
        return datasets;
    }

    public SessionResponse createUser(String name, String email, String password) {
        SessionResponse response = new SessionResponse();

        if (userRepository.findByEmail(email).isPresent() || userRepository.findByName(name).isPresent()) {
            response.setSuccess(false);
            return response;
        } else {
            String hashed = passwordEncoder.encode(password);
            userRepository.save(new User(email, name, hashed));
            return loginUser(name, password);// returns same sessionResponse doohickey with a new token
        } // god i hate java what on earth even is this code
    }

    private static final String DUMMY_HASH = "664322822d298ad17bd1736578dbf142a2df104c01587004b08b052d32ae6bfc";// hehhe

    public boolean checkCredentials(String username, String password) {
        User user = userRepository.findByName(username).orElse(userRepository.findByEmail(username).orElse(null));
        String hashToCheck = (user != null) ? user.getPassword() : DUMMY_HASH;
        return passwordEncoder.matches(password, hashToCheck) && user != null;
    }// am bored btw

    public SessionResponse loginUser(String name, String password) {
        SessionResponse reponse = new SessionResponse();
        User user = userRepository.findByName(name).orElse(userRepository.findByEmail(name).orElse(null));
        if (checkCredentials(user.getName(), password)) {
            Instant now = Instant.now();
            Instant expiry;
            SessionToken newToken = new SessionToken(
                    user.getId(),
                    now.plus(30, ChronoUnit.MINUTES),
                    now,
                    now);
            SessionToken TOKEN = sessionTokenRepository.save(newToken);
            System.out.println(TOKEN.getToken());

            reponse.setSuccess(true);
            reponse.setSessionToken(TOKEN);
        } else {
            reponse.setSuccess(false);
            reponse.setSessionToken(null);
        }
        return reponse;
    }

    public SessionResponse loginUser(LoginForm loginForm) {

        String name = loginForm.getName();
        String password = loginForm.getPassword();
        Boolean stayLogedIn = loginForm.getStayLoggedIn();

        SessionResponse reponse = new SessionResponse();

        

        User user = userRepository.findByName(name).orElse(userRepository.findByEmail(name).orElse(null));
        if (user !=null && checkCredentials(user.getName(), password)) {
            Instant now = Instant.now();
            Instant expiry = stayLogedIn?now.plus(30,ChronoUnit.DAYS):now.plus(30,ChronoUnit.MINUTES);
            SessionToken newToken = new SessionToken(
                    user.getId(),
                    expiry,
                    now,
                    now);
            SessionToken TOKEN = sessionTokenRepository.save(newToken);
            System.out.println(TOKEN.getToken());

            reponse.setSuccess(true);
            reponse.setSessionToken(TOKEN);
        } else {
            reponse.setSuccess(false);
            reponse.setSessionToken(null);
        }
        return reponse;
    }

    public SessionResponse loginUser(User user) {
        return loginUser(user.getName(), user.getPassword());
    }// override so i dont do some shit that breaks it all

    public void logoutUser(SessionToken sessionToken) {

        sessionTokenRepository.delete(sessionToken);
    }// I LOVE OVERRIDE METHODS

    public void logoutUser(String token) {
        SessionToken sessionToken = sessionTokenRepository.findByToken(token).orElse(null);
        if (sessionToken != null) {
            logoutUser(sessionToken);
        } else {
            System.out.println("somehow someone passed a non existant token to delete session??");
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found with id " + id));
    }

}
// clean