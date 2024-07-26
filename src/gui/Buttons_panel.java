package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class Buttons_panel {
    private static JPanel panel = null;

    public static JPanel init() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            panel.setBorder(null);
            panel.setBackground(new Color(58, 61, 63));

            JButton pubKey = new JButton();

            pubKey.setIcon(new ImageIcon(Buttons_panel.class.getResource("/images/key.png")));
            pubKey.setPressedIcon(new ImageIcon(Buttons_panel.class.getResource("/images/key_pres.png")));
            pubKey.setRolloverIcon(new ImageIcon(Buttons_panel.class.getResource("/images/key_sel.png")));

            pubKey.addActionListener(pubKey_listener);
            pubKey.setPreferredSize(new Dimension(30, 30));
            pubKey.setBorder(null);

            panel.add(pubKey);
        }

        return panel;
    }

    private static ActionListener pubKey_listener = e -> {
        Main_frame.show_publicKey();
    };
}
