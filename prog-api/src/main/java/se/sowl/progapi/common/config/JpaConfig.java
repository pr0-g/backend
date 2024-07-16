package se.sowl.progapi.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "se.sowl.progdomain")
public class JpaConfig {
}
