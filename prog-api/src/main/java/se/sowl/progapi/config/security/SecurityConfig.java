package se.sowl.progapi.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import se.sowl.progapi.oauth.service.OAuthService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuthService oAuthService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .logout(logout -> logout.logoutSuccessUrl("/"))
            .oauth2Login(oauth2Login -> oauth2Login
                .defaultSuccessUrl("/oauth2/login/info", true)
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuthService))
            );
        return http.build();
    }
}
