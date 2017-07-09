package au.net.woodberry.files.pcf.app;

import au.net.woodberry.files.pcf.integration.ConfigProps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;

@SpringBootApplication
@ComponentScan(basePackages = "au.net.woodberry.files.pcf")
@Component
@Slf4j
public class PcfFilesApplication {

    @Autowired
    private ConfigProps configProps;

    @Autowired
    private Environment environment;

    public void copyFiles() throws Exception {
        val source = new File(PcfFilesApplication.class.getResource("/pcf_file.csv").toURI());
        val destination = new File(configProps.getIncomingDirectory() + "/pcf_file.csv");
        if (destination.exists()) {
            log.info("Removing existing file: {}", destination);
            destination.delete();
        }
        log.info("Copying file from: {} to: {}", source, destination);
        FileCopyUtils.copy(source, destination);
    }

    public static void main(String... args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(PcfFilesApplication.class, args);
        PcfFilesApplication app = ctx.getBean(PcfFilesApplication.class);
        app.copyFiles();
    }
}
