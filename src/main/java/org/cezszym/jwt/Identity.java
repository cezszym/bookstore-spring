package org.cezszym.jwt;

import org.cezszym.repository.UserRepository;
import org.cezszym.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Identity {

    private final UserRepository userRepository;
    public Identity(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User getCurrent(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
        return userOptional.orElse(null);
    }
}
