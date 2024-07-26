package gui;

import certificate_autority.CertificateAuthority;
import dns.DomainNameSystem;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ServerList_panel {
    private static JPanel panel = null;

    private static GList server_list = new GList();
    private static JButton add_server = new JButton();
    private static JButton start_dns = new JButton();
    private static JButton stop_dns = new JButton();

    private static Map<String, ServerInfo> server_info = new LinkedHashMap<>(); //<server name> -> <server info>

    public static JPanel init() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            panel.setPreferredSize(new Dimension());
            panel.setBackground(new Color(58, 61, 63));

            //inizializza la lista
            GScrollPane scrollPane = new GScrollPane(server_list);
            server_list.set_popup(Popup_menu.class);

            // init_list(); //inizializza gli elementi della lista

            //inizializza i bottoni
            add_server.setIcon(get_icon("/images/add_server.png"));
            add_server.setPressedIcon(get_icon("/images/add_server_pres.png"));
            add_server.setRolloverIcon(get_icon("/images/add_server_sel.png"));
            start_dns.setIcon(get_icon("/images/power_on.png"));
            start_dns.setPressedIcon(get_icon("/images/power_on_pres.png"));
            start_dns.setRolloverIcon(get_icon("/images/power_on_sel.png"));
            start_dns.setDisabledIcon(get_icon("/images/power_dis.png"));
            stop_dns.setIcon(get_icon("/images/power_off.png"));
            stop_dns.setPressedIcon(get_icon("/images/power_off_pres.png"));
            stop_dns.setRolloverIcon(get_icon("/images/power_off_sel.png"));
            stop_dns.setDisabledIcon(get_icon("/images/power_dis.png"));

            add_server.setBorder(null);
            start_dns.setBorder(null);
            stop_dns.setBorder(null);

            add_server.addActionListener(add_server_l);
            start_dns.addActionListener(start_l);
            stop_dns.addActionListener(stop_l);

            stop_dns.setEnabled(false);

            //aggiunge lista e bottone al pannello
            GridBagConstraints c = new GridBagConstraints();

            c.insets = new Insets(0, 0, 10, 10);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.LAST_LINE_END;
            c.weighty = 0;
            c.weightx = 1;
            panel.add(add_server, c);

            c.insets.right = 0;
            c.gridx = 1;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.weightx = 0;
            panel.add(stop_dns, c);

            c.insets.left = 10;
            c.insets.right = 10;
            c.gridx = 0;
            panel.add(start_dns, c);

            c.fill = GridBagConstraints.BOTH;
            c.gridy = 1;
            c.weighty = 1;
            c.gridwidth = 3;
            panel.add(scrollPane, c);
        }

        return panel;
    }

    public static boolean add_server(ServerInfo info) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (server_info.putIfAbsent(info.name, info) == null) { //se non c'è già un valore associato a questo server aggiunge name -> info alla mappa server_info
            server_list.add(info.name);

            return true;
        }
        return false;
    }

    public static ServerInfo[] get_info() {
        return server_info.values().toArray(new ServerInfo[0]);
    }

    protected static void remove(String name) {
        server_info.remove(name);
    }

    protected static ServerInfo get(String name) {
        return server_info.get(name);
    }

    private static ImageIcon get_icon(String name) {
        return new ImageIcon(ServerList_panel.class.getResource(name));
    }

    public static void init_list() throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, IllegalBlockSizeException, BadPaddingException {
        if (new File(Main_frame.jar_path + "/database/server_info.dat").exists()) {
            byte[] file_byte = new FileInputStream(Main_frame.jar_path + "/database/server_info.dat").readAllBytes();
            String file_txt = new String(Main_frame.file_decoder.doFinal(file_byte));

            //divide file_txt nei vari segmenti con le informazioni relative ad ogni server
            Pattern server_sep = Pattern.compile("\n");
            String[] servers_info = server_sep.split(file_txt);

            //divide ogni segmento in servers_info nei vari valori delle info del server
            Pattern info_sep = Pattern.compile(";");
            for (String server_info : servers_info) {
                String[] info = info_sep.split(server_info);

                if (info.length == 6) { //se ci sono i 6 valori aspettati (nome, ip, link, ce, pub key, mail)
                    add_server(new ServerInfo(info[0], info[1], info[2], info[3], info[4], info[5])); //aggiunge il server alla lista
                }
            }
        }
        else { //se il file non esiste
            new File(Main_frame.jar_path + "/database").mkdir(); //crea la cartella database
            new File(Main_frame.jar_path + "/database/server_info.dat").createNewFile(); //crea il file vuoto
        }
    }

    private static ActionListener add_server_l = e -> {
        Main_frame.ask_info();
    };

    private static ActionListener start_l = e -> {
        if (CertificateAuthority.can_start()) {
            try {
                DomainNameSystem.start(); //fa partire il dns

                start_dns.setEnabled(false);
                stop_dns.setEnabled(true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    private static ActionListener stop_l = e -> {
        try {
            DomainNameSystem.stop(); //stoppa il dns

            stop_dns.setEnabled(false);
            start_dns.setEnabled(true);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    };
}

class Popup_menu extends JPopupMenu {
    private ServerInfo server_info;
    private String name;
    private GList list;

    public Popup_menu(String name, GList list) {
        server_info = ServerList_panel.get(name); //memorizza le info del server a cui è appaiato
        this.name = name;
        this.list = list;

        //aggiunge i due item del menu ed imposta la grafica
        JMenuItem menu_info = new JMenuItem("show info");
        JMenuItem menu_remove = new JMenuItem("remove");

        UIManager.put("MenuItem.selectionBackground", new Color(108, 111, 113));
        UIManager.put("MenuItem.selectionForeground", new Color(158, 161, 163));

        this.setBorder(BorderFactory.createLineBorder(new Color(28, 31, 33)));
        menu_info.setBorder(BorderFactory.createLineBorder(new Color(78, 81, 83)));
        menu_remove.setBorder(BorderFactory.createLineBorder(new Color(78, 81, 83)));

        menu_info.setBackground(new Color(88, 91, 93));
        menu_info.setForeground(new Color(158, 161, 163));
        menu_remove.setBackground(new Color(88, 91, 93));
        menu_remove.setForeground(new Color(158, 161, 163));

        menu_info.addActionListener(info_l);
        menu_remove.addActionListener(remove_l);

        this.add(menu_info);
        this.add(menu_remove);
    }

    private ActionListener info_l = e -> {
        Main_frame.show_info(this.server_info);
    };

    private ActionListener remove_l = e -> { //in realtà bisogna aggiungere un expiration date
        ServerList_panel.remove(name); //rimuove il server dalla mappa
        list.remove(name); //rimuove questo server dalla lista
    };
}

/*
 * utilizzando un estensione di JList viene più semplice ma aggiungere e rimuovere elementi dalla lista in modo dinamico può provocare problemi grafici
 * dove la lista viene mostrata vuota finché non le si dà un nuovo update, di conseguenza ho creato la mia versione di JList utilizzando varie JTextArea
 * e partendo da un JPanel.
 * Non so bene da che cosa sia dovuto il problema con JList ma sembra essere risolto utilizzando la mia versione
 */
class GList extends JPanel {
    private Map<String, ListCell> elements = new LinkedHashMap<>();
    private int selected_index = -1;

    private JPanel list_panel = new JPanel(); //pannello che contiene tutte le JTextArea della lista
    private JTextArea filler = new JTextArea(); //filler per rimepire lo spazio in basso

    private Constructor popupMenu = null;

    public GList() {
        super();
        this.setLayout(new GridBagLayout());

        this.setForeground(new Color(44, 46, 47));
        this.setBackground(new Color(98, 101, 103));
        this.setFont(new Font("custom_list", Font.BOLD, 11));

        filler.setBackground(this.getBackground());
        filler.setFocusable(false);
        filler.setEditable(false);

        list_panel.setLayout(new GridBagLayout());
        list_panel.setBackground(this.getBackground());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0;
        c.weightx = 1;
        this.add(list_panel, c);

        c.gridy = 1;
        c.weighty = 1;
        this.add(filler, c);
    }

    public void set_popup(Class PopupMenu) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this.popupMenu = PopupMenu.getDeclaredConstructor(String.class, GList.class);

        for (ListCell cell : elements.values()) {
            cell.setComponentPopupMenu((JPopupMenu) this.popupMenu.newInstance(cell.getText(), this));
        }
    }

    public void add(String name) throws InvocationTargetException, InstantiationException, IllegalAccessException { //aggiunge una nuova casella nell'ultima posizione
        ListCell cell = new ListCell(name, this, elements.size());
        elements.put(name, cell);
        if (popupMenu != null) {
            cell.setComponentPopupMenu((JPopupMenu) popupMenu.newInstance(name, this));
        }

        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridy = elements.size() - 1;
        c.gridx = 0;

        list_panel.add(cell, c);

        this.updateUI(); //aggiorna la gui
    }

    public void remove(String name) {
        ListCell cell = elements.get(name);

        elements.remove(name); //rimuove la cella dalla lista
        list_panel.remove(cell); //rimuove la casella dalla lista

        if (cell.my_index == selected_index) { //se questa casella era selezionata
            selected_index = -1;
        }

        this.updateUI(); //aggiorna la gui
    }

    public void rename_element(String old_name, String new_name) {
        for (ListCell cell : elements.values()) {
            if (cell.getText().equals(old_name)) {
                cell.setText(new_name);
                break;
            }
        }
    }

    public String getSelectedValue() {
        if (selected_index == -1) { //se non è selezionata nessuna casella
            return "";
        }
        else {
            return ((ListCell) list_panel.getComponent(selected_index)).getText();
        }
    }

    public void reset_list() {
        elements = new LinkedHashMap<>();
        list_panel.removeAll();
        this.repaint();

        selected_index = -1;
    }

    class ListCell extends JTextArea {
        private static final Color STD_BACKGROUND = new Color(98, 101, 103);
        private static final Color SEL_BACKGROUND = new Color(116, 121, 125);
        private static final Color SEL_BORDER = new Color(72, 74, 75);

        private final GList PARENT_LIST;
        private int my_index;

        public ListCell(String text, GList list, int index) {
            super(text);
            this.PARENT_LIST = list;
            this.my_index = index;

            //imposta tutti i colori
            this.setForeground(new Color(44, 46, 47));
            this.setBackground(STD_BACKGROUND);
            this.setFont(new Font("custom_list", Font.BOLD, 11));
            this.setBorder(null);

            this.setEditable(false);
            this.setCaretColor(STD_BACKGROUND);
            this.setCursor(null);

            this.addKeyListener(key_l);
            this.addMouseListener(mouse_l);
        }

        private KeyListener key_l = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case 40: //freccia in basso
                        try {
                            ListCell next_cell = (ListCell) PARENT_LIST.list_panel.getComponent(my_index + 1);

                            next_cell.set_selected();
                            next_cell.requestFocus();
                        } catch (Exception ex) {} //se non esiste un elemento ad index my_index + 1
                        break;

                    case 38: //freccia in alto
                        try {
                            ListCell prev_cell = (ListCell) PARENT_LIST.list_panel.getComponent(my_index - 1);

                            prev_cell.set_selected();
                            prev_cell.requestFocus();
                        } catch (Exception ex) {} //se non esiste un elemento ad index my_index - 1
                        break;

                    case 27: //esc
                        unselect();
                        break;
                }
            }
        };

        private MouseListener mouse_l = new MouseListener() {
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                set_selected();
            }
        };

        public void set_selected() {
            if (PARENT_LIST.selected_index != my_index) {
                //deseleziona la casella selezionata in precedenza, se ne era selezionata una
                if (PARENT_LIST.selected_index != -1) {
                    ((ListCell) PARENT_LIST.list_panel.getComponent(PARENT_LIST.selected_index)).unselect();
                }

                //imposta questa JTextArea come selezionata
                setBackground(SEL_BACKGROUND);
                setBorder(BorderFactory.createLineBorder(SEL_BORDER));
                setCaretColor(SEL_BACKGROUND);
                setSelectionColor(SEL_BACKGROUND);

                PARENT_LIST.selected_index = my_index;
            }
        }

        public void unselect() {
            setBackground(STD_BACKGROUND);
            setBorder(null);
            setCaretColor(STD_BACKGROUND);
            setSelectionColor(STD_BACKGROUND);

            PARENT_LIST.selected_index = -1;
        }
    }
}