package au.net.woodberry.files.integration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "woodberry.files.pcf")
@Configuration
@Data
@Slf4j
public class ConfigProps {

    @PostConstruct
    public void logConfig() {
        log.info("Loaded configuration properties: {}", this);
    }

    @NotNull
    private String incomingDirectory;

    @NotNull
    private String outgoingDirectory;

    @NotNull
    private String outgoingDirectoryErrors;
}
