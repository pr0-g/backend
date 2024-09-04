package se.sowl.progapi.interest.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final InterestRepository interestRepository;

    public List<Interest> getList() {
        return interestRepository.findAll();
    }

    public Optional<Interest> getPostInerest(Long postId){
        return interestRepository.findById(postId);
    }
}
