package se.sowl.progapi.interest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import se.sowl.progapi.fixture.UserFixture;
import se.sowl.progapi.interest.request.EditInterestRequest;
import se.sowl.progdomain.interest.domain.Interest;
import se.sowl.progdomain.interest.domain.UserInterest;
import se.sowl.progdomain.interest.repository.InterestRepository;
import se.sowl.progdomain.interest.repository.UserInterestRepository;
import se.sowl.progdomain.oauth.domain.CustomOAuth2User;
import se.sowl.progdomain.user.domain.User;
import se.sowl.progdomain.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InterestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    private User testUser;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        interestRepository.deleteAll();

        testUser = userRepository.save(UserFixture.createUser(null, "Test User", "testuser", "test@example.com", "google"));
        interestRepository.saveAll(List.of(
                new Interest("SF"),
                new Interest("판타지"),
                new Interest("로맨스")
        ));
    }

    @Test
    @WithMockUser
    @Transactional
    @DisplayName("존재하지 않는 관심사 ID로 업데이트 시 실패해야 한다.")
    void editUserInterestsWithNonExistentId() throws Exception {
        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(testUser);
        List<Long> invalidInterestIdList = List.of(999L, 1000L);

        EditInterestRequest request = new EditInterestRequest(invalidInterestIdList);

        mockMvc.perform(put("/api/interests/user/edit")
                        .with(oauth2Login().oauth2User(customOAuth2User))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FAIL"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 관심사 ID가 포함되어 있습니다."))
                .andExpect(jsonPath("$.result").value(nullValue()));
    }

    @Test
    @WithMockUser
    @Transactional
    @DisplayName("사용자가 관심사를 업데이트할 수 있다.")
    void editUserInterests() throws Exception {
        CustomOAuth2User customOAuth2User = UserFixture.createCustomOAuth2User(testUser);

        // 임의의 관심사 ID 2와 3을 사용
        List<Long> newInterestIdList = Arrays.asList(2L, 3L);

        EditInterestRequest request = new EditInterestRequest(newInterestIdList);

        ResultActions resultActions = mockMvc.perform(put("/api/interests/user/edit")
                        .with(oauth2Login().oauth2User(customOAuth2User))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.result").value(nullValue()));

        List<UserInterest> result = userInterestRepository.findAllByUserId(testUser.getId()).get();

        // 결과 확인
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(ui -> ui.getInterest().getId()).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(newInterestIdList);

        // 추가: 실제 관심사 이름 확인
        List<String> resultInterestNames = result.stream()
                .map(ui -> ui.getInterest().getName())
                .collect(Collectors.toList());
        assertThat(resultInterestNames).containsExactlyInAnyOrder("판타지", "로맨스");
    }

}