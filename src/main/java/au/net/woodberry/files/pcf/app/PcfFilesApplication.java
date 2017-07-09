package au.net.woodberry.files.pcf.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "au.net.woodberry.files.pcf")
public class PcfFilesApplication {

    public static void main(String... args) {
        SpringApplication.run(PcfFilesApplication.class, args);
    }
}
