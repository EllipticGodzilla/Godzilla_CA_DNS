import certificate_autority.CertificateAuthority;
import dns.Converter;
import gui.Main_frame;
import gui.ServerInfo;
import gui.ServerList_panel;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Vector;

public class Godzilla_CA_DNS {
    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, URISyntaxException {
        Vector<Image> icons = new Vector<>();

        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getClassLoader().getResourceAsStream("images/icon_16.png").readAllBytes()).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getClassLoader().getResourceAsStream("images/icon_32.png").readAllBytes()).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getClassLoader().getResourceAsStream("images/icon_64.png").readAllBytes()).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getClassLoader().getResourceAsStream("images/icon_128.png").readAllBytes()).getImage());

        Main_frame.init(new File(Godzilla_CA_DNS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()).setIconImages(icons); //inizializza la gui
        Converter.init(); //inizializza il converter
        CertificateAuthority.init(); //inizializza la ca

        Runtime.getRuntime().addShutdownHook(new Thread(shutdown_hook));
    }

    private static Runnable shutdown_hook = new Runnable() {
        @Override
        public void run() {
            try {
                save_server_info(); //salva tutte le informazioni sui server
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        private void save_server_info() throws IOException, URISyntaxException {
            String file_txt = "";

            //genera il testo da salvare nel file
            ServerInfo[] info_array = ServerList_panel.get_info();
            for (ServerInfo info : info_array) {
                file_txt += info.name + ";" + info.link + ";" + info.ip + ";" + info.ce + ";" + info.mail + "\n";
            }

            //salva il testo all'interno del file
            FileOutputStream writer = new FileOutputStream(Main_frame.jar_path + "/server_info.dat");
            writer.write(file_txt.getBytes());
            writer.close();
        }
    };
}
