package com.vux38.base.service;

import com.vux38.base.module.user.entity.User;
import com.vux38.base.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {

        System.out.println("Query Database");

        return userRepository.findById(id)
                .orElseThrow();
    }
}