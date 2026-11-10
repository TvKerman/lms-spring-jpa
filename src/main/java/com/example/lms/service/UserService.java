package com.example.lms.service;

import com.example.lms.entity.Profile;
import com.example.lms.entity.User;
import com.example.lms.enums.Role;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.ProfileRepository;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public User createUser(String name, String email, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email=" + email + " already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        User saved = userRepository.save(user);
        log.info("Created user id={}, email={}, role={}", saved.getId(), email, role);
        return saved;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email=" + email + " not found"));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public Profile createOrUpdateProfile(Long userId, String bio, String avatarUrl) {
        User user = findById(userId);
        Profile profile = profileRepository.findByUserId(userId).orElseGet(Profile::new);
        profile.setUser(user);
        profile.setBio(bio);
        profile.setAvatarUrl(avatarUrl);
        log.info("Upserted profile for user id={}", userId);
        return profileRepository.save(profile);
    }
}