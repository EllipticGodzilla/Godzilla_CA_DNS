package gui;

import certificate_autority.CertificateAuthority;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Info_panel {
    private static JPanel info_panel = null;

    private static JTextField[] info_area = new JTextField[5];
    private static JButton add_server = new JButton();
    private static JButton annulla = new JButton(); //ogni volta che il pulsante annulla viene mosso è una variazione sulle y, le x sono costanti a 10

    public static JPanel init() {
        if (info_panel == null) {
            info_panel = new JPanel(new GridBagLayout());
            info_panel.setBackground(new Color(58, 61, 63));
            info_panel.setSize(new Dimension(535, 190));

            info_area[0] = new NoeditTextField("Server Name:");
            info_area[1] = new NoeditTextField("Server link:");
            info_area[2] = new NoeditTextField("Server ip");
            info_area[3] = new NoeditTextField("Server public key:");
            info_area[4] = new NoeditTextField("Server e-mail:");

            for (JTextField area : info_area) {
                area.setFocusable(false);
            }

            add_server.setBorder(null);
            annulla.setBorder(null);

            add_server.setIcon(new ImageIcon(Info_panel.class.getResource("/images/ok.png")));
            add_server.setPressedIcon(new ImageIcon(Info_panel.class.getResource("/images/ok_pres.png")));
            add_server.setSelectedIcon(new ImageIcon(Info_panel.class.getResource("/images/ok_sel.png")));
            annulla.setIcon(new ImageIcon(Info_panel.class.getResource("/images/annulla.png")));
            annulla.setPressedIcon(new ImageIcon(Info_panel.class.getResource("/images/annulla_pres.png")));
            annulla.setSelectedIcon(new ImageIcon(Info_panel.class.getResource("/images/annulla_sel.png")));

            annulla.addActionListener(hide_l);

            //aggiungo i componenti nel pannello
            GridBagConstraints c = new GridBagConstraints();

            //aggiunge tutte le text area in info_area[]
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(10, 10, 10, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0;
            c.weighty = 0.1;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            info_panel.add(info_area[0], c);

            c.gridy = 1;
            c.insets.top = 0;
            info_panel.add(info_area[1], c);

            c.gridy = 2;
            info_panel.add(info_area[2], c);

            c.gridy = 3;
            info_panel.add(info_area[3], c);

            c.gridy = 4;
            info_panel.add(info_area[4], c);

            //aggiunge i pulsanti add_server e annulla
            c.gridy = 5;
            c.fill = GridBagConstraints.NONE;
            info_panel.add(annulla, c);

            c.gridx = 1;
            c.anchor = GridBagConstraints.FIRST_LINE_END;
            c.insets.right = 10;
            info_panel.add(add_server, c);
        }
        return info_panel;
    }

    public static void show_info(String name, String link, String ip, String ce, String mail) {
        add_rigth_area(0, name);
        add_rigth_area(1, link);
        add_rigth_area(2, ip);
        add_rigth_area(3, ce);
        add_rigth_area(4, mail);

        annulla.setVisible(false); //nasconde il pulsante annulla
        info_area[3].setText("Server certificate:");

        if (add_server.getActionListeners().length != 0 && !add_server.getActionListeners()[0].equals(hide_l)) { //se ha come actionListener generate_ce
            add_server.removeActionListener(generate_ce_l); //rimuove il listener per aggiungere un nuovo server
            add_server.addActionListener(hide_l); //premento il pulsante "ok" nasconde il pannello
        }
        else if (add_server.getActionListeners().length == 0) { //se non c'è nessun actionListener
            add_server.addActionListener(hide_l); //premento il pulsante "ok" nasconde il pannello
        }

        info_panel.updateUI();
    }

    public static void ask_info() {
        for (int i = 0; i < 5; i++) {
            add_rigth_area(i, null);
        }

        annulla.setVisible(true);
        info_area[3].setText("Server public key:");

        if (add_server.getActionListeners().length != 0 && !add_server.getActionListeners()[0].equals(generate_ce_l)) { //se non ha come actionListener generate_ce
            add_server.removeActionListener(hide_l); //rimuove il listener per nascondere il pannello una volta premuto il pulsante
            add_server.addActionListener(generate_ce_l); //premuto il pulsante genera il certificato del nuovo server
        }
        else if (add_server.getActionListeners().length == 0) { //se non c'è nessun actionListener
            add_server.addActionListener(generate_ce_l); //premento il pulsante "ok" genera un nuovo certificato per il server
        }

        info_panel.updateUI();
    }

    public static void reset() { //rimuove gli ultimi 5 componenti aggiunti a info_panel
        for (int i = 0; i < 5; i++) {
            info_panel.remove(info_panel.getComponent(info_panel.getComponentCount() - 1)); //rimuove l'ultimo componente nella lista
        }

        info_panel.updateUI();
    }

    private static ActionListener generate_ce_l = e -> {
        try {
            String nome = ((JTextField) info_panel.getComponent(info_panel.getComponentCount() - 5)).getText(),
                   link = ((JTextField) info_panel.getComponent(info_panel.getComponentCount() - 4)).getText(),
                   ip = ((JTextField) info_panel.getComponent(info_panel.getComponentCount() - 3)).getText(),
                   pub_key = ((JTextField) info_panel.getComponent(info_panel.getComponentCount() - 2)).getText(),
                   mail = ((JTextField) info_panel.getComponent(info_panel.getComponentCount() - 1)).getText();

            String ce = CertificateAuthority.calculate_ce(nome, link, ip, pub_key, mail);
            ServerInfo info = new ServerInfo(nome, link, ip, ce, mail);

            ServerList_panel.add_server(info);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        //resetta e nasconde il pannello
        reset();
        info_panel.setVisible(false);
    };

    private static ActionListener hide_l = e -> {
        reset();
        info_panel.setVisible(false);
    };

    //se txt = null l'area sarà editable, altrimenti viene impostato come testo txt e sarà resa non editable
    private static void add_rigth_area(int index, String txt) {
        JTextField field = (txt == null)? new EditTextField() : new NoeditTextField(txt);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 1;
        c.gridy = index;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets((index == 0)? 10 : 0, 10, 10, 10);

        info_panel.add(field, c);
    }

    private static class NoeditTextField extends JTextField {
        private static final int WIDTH  = 150;
        private static final int HEIGHT = 20;

        public NoeditTextField(String txt) {
            super(txt);

            this.setBackground(new Color(58, 61, 63));
            this.setBorder(null);
            this.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
            this.setForeground(new Color(188, 191,  193));

            this.setPreferredSize(new Dimension(
                    Math.min(WIDTH, this.getPreferredSize().width), //se this.getPreferredSize().width è troppo grande questa casella nasconde quelle in info_area
                    HEIGHT
            ));

            this.setEditable(false);
        }
    }

    private static class EditTextField extends JTextField {
        private static final int WIDTH  = 150;
        private static final int HEIGHT = 20;

        public EditTextField() {
            super();

            this.setBackground(new Color(108, 111, 113));
            this.setBorder(BorderFactory.createLineBorder(new Color(68, 71, 73)));
            this.setFont(new Font("Arial", Font.BOLD, 14));
            this.setForeground(new Color(218, 221, 223));

            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }
    }
}
