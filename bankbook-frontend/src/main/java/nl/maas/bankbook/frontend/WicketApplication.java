package nl.maas.bankbook.frontend;

import nl.maas.bankbook.frontend.wicket.pages.MainPage;
import nl.maas.bankbook.utils.FileUtils;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.FileSystemResourceReference;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;


@SpringBootApplication
public class WicketApplication extends WebApplication {

    public static void main(String[] args) throws Exception {
        prepareIcon();
        SpringApplication.run(WicketApplication.class, args);
    }

    public static void restart() {
        final ConfigurableApplicationContext[] ctx = {ContextProvider.ctx};
        ApplicationArguments args = ctx[0].getBean(ApplicationArguments.class);
        BrowserManager manager = ctx[0].getBean(BrowserManager.class);
        manager.close();
        Thread thread = new Thread(() -> {
            ctx[0].close();
            ctx[0] = SpringApplication.run(WicketApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }

    public WicketApplication() {

    }

    @Override
    protected void internalInit() {
        super.internalInit();
        FileSystemResourceReference favicon = new FileSystemResourceReference("favicon", Path.of(FileUtils.getResourceAsFile("/open/images/bankbook.ico").getPath()));
        mountResource("/images/bankbook.ico", favicon);

    }

    private static void prepareIcon() {
        try {
            FileUtils.saveFileToAppFolder(FileUtils.getResourceAsFile("/open/images/bankbook.ico"), true);
            FileUtils.saveFileToAppFolder(FileUtils.getResourceAsFile("/open/images/bankbook.png"), true);
        } catch (Exception e) {
            System.out.println("Could not save icon: ${e.message}");
        }
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MainPage.class;
    }
}
