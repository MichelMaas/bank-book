package nl.maas.bankbook.frontend;

import nl.maas.bankbook.frontend.wicket.pages.OverviewPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.FileSystemResourceReference;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Path;

@SpringBootApplication
@EnableAsync
public class WicketApplication extends WebApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext run = new SpringApplicationBuilder()
                .sources(WicketApplication.class)
                .run(args);
    }

    public static void restart() {
        final ConfigurableApplicationContext[] ctx = {ContextProvider.ctx};
        ApplicationArguments args = ctx[0].getBean(ApplicationArguments.class);
//        BrowserManager manager = ctx[0].getBean(BrowserManager.class);
//        manager.close();
        Thread thread = new Thread(() -> {
            ctx[0].refresh();
        });
//
//        thread.setDaemon(false);
//        thread.start();
    }

    public WicketApplication() {
//        FileSystemResourceReference favicon = new FileSystemResourceReference("favicon", Path.of(this.getClass().getResource("/open/images/icon.png").getPath()));
//        mountResource("/images/icon.png", favicon);
    }

    @Override
    protected void internalInit() {
        super.internalInit();
        FileSystemResourceReference favicon = new FileSystemResourceReference("favicon", Path.of(this.getClass().getResource("/open/images/icon.png").getPath()));
        mountResource("/images/icon.png", favicon);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return OverviewPage.class;
    }
}
