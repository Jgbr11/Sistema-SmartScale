package br.com.milscale.milscale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.milscale")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "br.com.milscale.milscale.adapters.persistence")
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "br.com.milscale.milscale.domain")
@org.springframework.scheduling.annotation.EnableScheduling
public class MilScaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MilScaleApplication.class, args);
    }
}
