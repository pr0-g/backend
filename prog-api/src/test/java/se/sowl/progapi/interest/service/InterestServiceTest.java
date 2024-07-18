package se.sowl.progapi.interest.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.repository.InterestRepository;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class InterestServiceTest {

    @Autowired
    private InterestService interestService;

    @Autowired
    private InterestRepository interestRepository;

    @AfterEach
    void tearDown() {
        interestRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("관심사가 없으면 빈 배열을 응답해야한다.")
    void emptyInterest() {
        // given

        // when
        List<Interest> list = interestService.getList();

        // then
        assertThat(list.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("관심가가 있으면 관심사 목록을 응답해야한다.")
    void interestExist() {
        // given
        Interest interest1 = new Interest("SF");
        Interest interest2 = new Interest("판타지");
        Interest interest3 = new Interest("로맨스");
        interestRepository.saveAll(List.of(interest1, interest2, interest3));

        // when
        List<Interest> list = interestService.getList();

        // then
        assertThat(list.size()).isEqualTo(3);
    }
}
