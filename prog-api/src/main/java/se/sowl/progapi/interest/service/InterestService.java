package se.sowl.progapi.interest.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final InterestRepository interestRepository;

    public List<Interest> getList() {
        return interestRepository.findAll();
    }
}
