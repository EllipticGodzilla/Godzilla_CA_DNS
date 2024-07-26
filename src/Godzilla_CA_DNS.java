import certificate_autority.CertificateAuthority;
import gui.Main_frame;
import gui.ServerInfo;
import gui.ServerList_panel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.security.*;
import java.util.Vector;

public class Godzilla_CA_DNS {
    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException, URISyntaxException {
        Vector<Image> icons = new Vector<>();

        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getResource("/images/icon_16.png")).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getResource("/images/icon_32.png")).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getResource("/images/icon_64.png")).getImage());
        icons.add(new ImageIcon(Godzilla_CA_DNS.class.getResource("/images/icon_128.png")).getImage());

        Main_frame.init(new File(Godzilla_CA_DNS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()).setIconImages(icons); //inizializza la gui
        CertificateAuthority.init(); //inizializza la ca

        Runtime.getRuntime().addShutdownHook(new Thread(shutdown_hook));
    }

    private static Runnable shutdown_hook = () -> {
        try {
            //salva tutte le informazioni sui server
            String file_txt = "";

            //genera il testo da salvare nel file
            ServerInfo[] info_array = ServerList_panel.get_info();
            for (ServerInfo info : info_array) {
                file_txt += info.name + ";" + info.link + ";" + info.ip + ";" + info.ce + ";" + info.pubKey + ";" + info.mail + "\n";
            }

            //cifra il testo
            byte[] encodet_txt = Main_frame.file_encoder.doFinal(file_txt.getBytes());

            //salva il testo all'interno del file
            FileOutputStream writer = new FileOutputStream(Main_frame.jar_path + "/database/server_info.dat");
            writer.write(encodet_txt);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };
}
