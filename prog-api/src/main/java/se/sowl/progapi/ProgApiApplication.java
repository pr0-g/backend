package se.sowl.progapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"se.sowl.progdomain"})
public class ProgApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgApiApplication.class, args);
    }

}
