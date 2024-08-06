package en.pchz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "en.pchz")
@ConfigurationPropertiesScan
@PropertySource("classpath:database.properties")
public class Main {
    public static void main(String[] args) {
        var context = SpringApplication.run(Main.class, args);
    }
}