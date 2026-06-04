package com.vux38.user.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.vux38.user.entity.UserProfile;
import com.vux38.user.repository.UserProfileRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile findByUserId(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User profile not found: " + userId));
    }

    public UserProfile create(UserProfile profile) {

        if (userProfileRepository.existsByEmail(profile.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        return userProfileRepository.save(profile);
    }

    public UserProfile update(Long userId, UserProfile request) {

        UserProfile profile = findByUserId(userId);

        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setPhone(request.getPhone());
        profile.setAvatar(request.getAvatar());
        profile.setBirthday(request.getBirthday());

        return userProfileRepository.save(profile);
    }

    public void delete(Long userId) {
        userProfileRepository.deleteById(userId);
    }
}