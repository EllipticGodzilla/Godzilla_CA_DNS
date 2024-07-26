package gui;

import certificate_autority.CertificateAuthority;
import dns.Converter;

import javax.crypto.Cipher;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

public abstract class Main_frame {
    public static Cipher file_encoder,
                         file_decoder;

    public static String jar_path;

    private static final Dimension MIN_DIMENSION = new Dimension(900, 500);

    private static JFrame frame = null;
    private static JPanel temp_panel,
                          log_panel,
                          serverList_panel,
                          buttons_panel;
    private static FullscreenLayeredPane layeredPane;

    public static JFrame init(String jar_path) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        if (frame == null) {
            Main_frame.jar_path = jar_path;

            //inizializza il frame
            frame = new JFrame("Godzilla DNS, CA");
            frame.getContentPane().setLayout(new GridBagLayout());
            frame.setMinimumSize(MIN_DIMENSION);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(new Color(58, 61, 63));

            //inizializza tutti i pannelli
            layeredPane = new FullscreenLayeredPane();

            JPanel component_panel = new JPanel();
            component_panel.setLayout(new GridBagLayout());
            component_panel.setBackground(new Color(58, 61, 63));

            temp_panel = TempPanel.init();
            log_panel = Log_panel.init();
            serverList_panel = ServerList_panel.init();
            buttons_panel = Buttons_panel.init();

            temp_panel.setVisible(false);

            layeredPane.add_fullscreen(component_panel, JLayeredPane.DEFAULT_LAYER);
            layeredPane.add(temp_panel, JLayeredPane.POPUP_LAYER);

            //aggiunge i pannelli al frame
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(10, 10, 10, 10);
            c.weighty = 1;
            c.weightx = 0.3;
            c.gridheight = 2;
            component_panel.add(serverList_panel, c);

            c.gridheight = 1;
            c.gridx = 1;
            c.insets = new Insets(10, 0, 10, 10);
            c.weightx = 1;
            c.weighty = 0;
            component_panel.add(buttons_panel, c);

            c.insets.top = 0;
            c.gridy = 1;
            c.weighty = 1;
            component_panel.add(log_panel, c);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;
            c.insets = new Insets(0, 0, 0, 0);
            frame.getContentPane().add(layeredPane, c);

            //rende il frame visibile
            frame.setVisible(true);
        }

        return frame;
    }

    public static void show_publicKey() {
        TempPanel.show(new TempPanel_info(
                TempPanel_info.DOUBLE_COL_MSG,
                false,
                "CA public key: ", CertificateAuthority.pub_key
        ), null);
    }

    public static void show_info(ServerInfo server_info) {
        TempPanel.show(new TempPanel_info( //mostra le informazioni relative al server
                TempPanel_info.DOUBLE_COL_MSG,
                false,
                "server name: ", server_info.name,
                "server link: ", server_info.link,
                "server ip: ", server_info.ip,
                "certificato: ", server_info.ce,
                "chiave pubblica: ", server_info.pubKey,
                "mail: ", server_info.mail
        ), null);
    }

    public static void ask_info() {
        TempPanel.show(new TempPanel_info(
                TempPanel_info.INPUT_REQ,
                true,
                "server name: ",
                "server link: ",
                "server ip: ",
                "server public key: ",
                "server e-mail: "
        ), register_server);
    }

    public static void recenter_temp_panel() {
        temp_panel.setLocation(
                frame.getWidth() / 2 - temp_panel.getWidth() / 2,
                frame.getHeight() / 2 - temp_panel.getHeight() / 2
        );
    }

    private static TempPanel_action register_server = new TempPanel_action() {
        @Override
        public void success() {
            try {
                String nome = input.elementAt(0);
                String link = input.elementAt(1);
                String ip = input.elementAt(2);
                String pub_key = input.elementAt(3);
                String mail = input.elementAt(4);

                String ce = CertificateAuthority.calculate_ce(nome, link, ip, pub_key, mail); //calcola il certificato

                ServerInfo info = new ServerInfo(nome, link, ip, ce, pub_key, mail);
                ServerList_panel.add_server(info);
                Converter.add_map(link, ip);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void fail() {} //ha scelto di non aggiungere più il server
    };

    private static class FullscreenLayeredPane extends JLayeredPane { //permette di ridimensionare componenti in modo che abbiamo sempre la sua stessa dimensione
        Vector<Component> resizable = new Vector<>();
        public FullscreenLayeredPane() {
            super();
            this.setBackground(new Color(58, 61, 63));
        }

        @Override
        public void setBounds(int x, int y, int width, int height) { //in questo modo si elimina il delay che si avrebbe utilizzando un component listener
            super.setBounds(x, y, width, height);

            for (Component cmp : resizable) {
                cmp.setBounds(0, 0, width, height);
            }
            recenter_temp_panel();
        }

        public void add_fullscreen(Component comp, int index) {
            super.add(comp, index);
            resizable.add(comp);
        }
    }
}

class GScrollPane extends JScrollPane {
    public GScrollPane(Component c) {
        super(c);

        this.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));
    }

    @Override
    public JScrollBar createVerticalScrollBar() {
        JScrollBar scrollBar = super.createVerticalScrollBar();

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(78, 81, 83);
                this.thumbDarkShadowColor = new Color(58, 61, 63);
                this.thumbHighlightColor = new Color(108, 111, 113);
            }

            class null_button extends JButton {
                public null_button() {
                    super();
                    this.setPreferredSize(new Dimension(0, 0));
                }
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new null_button();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new null_button();
            }
        });

        scrollBar.setBackground(new Color(128, 131, 133));
        scrollBar.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));

        return scrollBar;
    }

    @Override
    public JScrollBar createHorizontalScrollBar() {
        JScrollBar scrollBar = super.createHorizontalScrollBar();

        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(78, 81, 83);
                this.thumbDarkShadowColor = new Color(58, 61, 63);
                this.thumbHighlightColor = new Color(108, 111, 113);
            }

            class null_button extends JButton {
                public null_button() {
                    super();
                    this.setPreferredSize(new Dimension(0, 0));
                }
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return new null_button(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return new null_button(); }

        });

        scrollBar.setBackground(new Color(128, 131, 133));
        scrollBar.setBorder(BorderFactory.createLineBorder(new Color(72, 74, 75)));

        return scrollBar;
    }
}

