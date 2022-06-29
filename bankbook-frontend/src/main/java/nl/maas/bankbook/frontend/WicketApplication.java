package nl.maas.bankbook.frontend;

import nl.maas.bankbook.frontend.wicket.caches.PropertiesCache;
import nl.maas.bankbook.frontend.wicket.pages.YearOverviewPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.FileSystemResourceReference;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Path;

@SpringBootApplication
@EnableAsync
public class WicketApplication extends WebApplication {

    private static final String USER_HOME = "user-home";

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext run = new SpringApplicationBuilder()
                .sources(WicketApplication.class)
                .run();
    }

    public static void restart() {
        final ConfigurableApplicationContext[] ctx = {ContextProvider.ctx};
        Thread thread = new Thread(() -> {
            ctx[0].getBean(PropertiesCache.class);
        });
        thread.setDaemon(false);
        thread.start();
    }


    @Override
    protected void internalInit() {
        super.internalInit();
        FileSystemResourceReference favicon = new FileSystemResourceReference("favicon", Path.of(this.getClass().getResource("/open/images/icon.png").getPath()));
        mountResource("/images/icon.png", favicon);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return YearOverviewPage.class;
    }
}
