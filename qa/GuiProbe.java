import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.cef.browser.CefBrowser;
import nl.maas.bankbook.client.*;

/** Exercise the actual shared viewer. Extra browser flags apply only inside QA. */
public class GuiProbe {
    public static void main(String[] args) {
        try { run(); } catch (Throwable ex) { ex.printStackTrace(); Runtime.getRuntime().halt(1); }
    }
    private static void run() throws Exception {
        Field defaults = me.friwi.jcefmaven.CefAppBuilder.class.getDeclaredField("DEFAULT_JCEF_ARGS");
        defaults.setAccessible(true);
        ((List<String>)defaults.get(null)).addAll(List.of("--no-sandbox", "--disable-gpu", "--disable-dev-shm-usage"));
        System.err.println("QA: start gedeelde viewer");
        BankbookClient client = new BankbookClient(); client.start();
        System.err.println("QA: gedeelde viewer gestart");
        Object viewer = client.getViewer();
        if (!viewer.getClass().getName().equals("nl.maas.wicket.framework.viewer.CEFViewer")) throw new AssertionError("Andere viewer dan framework CEFViewer");
        Field browserField=viewer.getClass().getDeclaredField("browser");browserField.setAccessible(true);
        Field frameField=viewer.getClass().getDeclaredField("frame");frameField.setAccessible(true);
        CefBrowser browser=(CefBrowser)browserField.get(viewer);
        AtomicReference<String> html=new AtomicReference<>("");
        JFrame frame=null;boolean ready=false;
        long end=System.currentTimeMillis()+25000;
        while(System.currentTimeMillis()<end) {
            browser.getSource(html::set);
            Thread.sleep(600);
            String source=html.get();
            ready=source.matches("(?s).*type=[\"']file[\"'].*") && java.util.regex.Pattern.compile("<div\\b(?=[^>]*class=[\"'][^\"']*overlay)(?=[^>]*\\bhidden\\b)[^>]*>").matcher(source).find();
            try {frame=(JFrame)frameField.get(viewer);}catch(Exception ignored){}
            if(ready && frame!=null && frame.isVisible())break;
        }
        System.err.println("QA: pagina klaar="+ready+", frame="+frame);
        if(!ready)throw new AssertionError("Importformulier niet klaar: "+html.get().substring(Math.max(0,html.get().length()-2200)));
        final JFrame window=frame;
        if(window==null || !window.getTitle().equals("Bankbook"))throw new AssertionError("Frameworkvenster ontbreekt");
        SwingUtilities.invokeAndWait(() -> {
            try {
                Rectangle rect=window.getBounds();
                BufferedImage image=new Robot().createScreenCapture(rect);
                ImageIO.write(image,"png",new File(System.getProperty("bankbook.qa.screenshot")));
            }catch(Exception ex){throw new RuntimeException(ex);}
        });
        System.out.println("PASS gedeelde Viewers.viewer/CEFViewer laadt Bankbook-importformulier zonder blijvend laadscherm");
        Runtime.getRuntime().halt(0);
    }
}
