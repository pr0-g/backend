package se.sowl.progapi.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<User> getList() {
        return userRepository.findAll();
    }

    @Transactional
    public void setUserNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId).orElseThrow();
        user.updateNickname(newNickname);
        userRepository.save(user);
    }

}
