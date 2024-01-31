package gui;

import com.sun.tools.javac.Main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Pattern;

public abstract class Main_frame {
    public static String jar_path;

    private static final Dimension MIN_DIMENSION = new Dimension(900, 500);
    private static final double INFO_WIDTH_PERC = 0.8;

    private static JFrame frame = null;
    private static JPanel info_panel,
                          log_panel,
                          serverList_panel;
    private static FullscreenLayeredPane layeredPane;

    public static JFrame init(String jar_path) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        if (frame == null) {
            Main_frame.jar_path = jar_path;

            //inizializza il frame
            frame = new JFrame("Godzilla DNS, CA");
            frame.setLayout(new GridBagLayout());
            frame.setMinimumSize(MIN_DIMENSION);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(new Color(58, 61, 63));

            layeredPane = new FullscreenLayeredPane();

            //inizializza tutti i pannelli
            info_panel = Info_panel.init();
            log_panel = Log_panel.init();
            serverList_panel = ServerList_panel.init();

            layeredPane.add_fullscreen(log_panel, JLayeredPane.DEFAULT_LAYER);
            layeredPane.add(info_panel, JLayeredPane.POPUP_LAYER);
            info_panel.setVisible(false);

            //aggiunge i pannelli al frame
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new Insets(10, 10, 10, 10);
            c.weighty = 1;
            c.weightx = 0.3;
            frame.add(serverList_panel, c);

            c.gridx = 1;
            c.insets = new Insets(10, 0, 10, 10);
            c.weightx = 1;
            frame.add(layeredPane, c);

            //rende il frame visibile
            frame.setVisible(true);
        }

        return frame;
    }

    public static void show_info(ServerInfo server_info) {
        if (info_panel.isVisible()) { //se sta già visualizzando qualcosa su info_panel, lo resetta
            Info_panel.reset();
        }
        else {
            info_panel.setVisible(true);
        }

        Info_panel.show_info(server_info.name, server_info.link, server_info.ip, server_info.ce, server_info.mail); //mostra le informazioni relative al server
        recenter_info_panel();
    }

    public static void ask_info() {
        if (info_panel.isVisible()) { //se sta già visualizzando qualcosa su info_panel, lo resetta
            Info_panel.reset();
        }
        else {
            info_panel.setVisible(true);
        }

        Info_panel.ask_info();
    }

    public static void recenter_info_panel() {
        info_panel.setBounds(
                (int) (layeredPane.getWidth() * (1 - INFO_WIDTH_PERC) / 2),
                layeredPane.getHeight() / 2 - info_panel.getPreferredSize().height / 2,
                (int) (layeredPane.getWidth() * INFO_WIDTH_PERC),
                info_panel.getPreferredSize().height
        );
    }

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
            recenter_info_panel();
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

