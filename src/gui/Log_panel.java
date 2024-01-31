package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Log_panel {
    private static LogTextArea log = new LogTextArea();

    private static JPanel log_panel = null;
    private static GScrollPane log_scroller;

    private static final Color ERROR_BACKGROUND = Color.lightGray;
    private static final Color ERROR_FOREGROUND = Color.red.darker();

    protected static JPanel init() {
        if (log_panel == null) {
            log_panel = new JPanel();
            log_scroller = new GScrollPane(log);
            log_panel.setPreferredSize(new Dimension());

            log_scroller.setBackground(new Color(128, 131, 133));

            log_panel.setLayout(new GridLayout(1, 1));
            log_panel.add(log_scroller);
        }
        return log_panel;
    }

    public synchronized static void log_write(String txt, boolean error) {
        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

        if (error) {
            eventQueue.postEvent(new LogEvent(log, txt, ERROR_BACKGROUND, ERROR_FOREGROUND, true));
        }
        else {
            eventQueue.postEvent(new LogEvent(log, txt));
        }
    }

    public static String get_log() {
        return log.getText();
    }

    private static void show_last_line() {
        if (log_scroller.getVerticalScrollBar().isValid()) {
            log_scroller.getVerticalScrollBar().setValue(log_scroller.getVerticalScrollBar().getMaximum());
        }
    }

    private static class LogTextArea extends JTextPane {
        private Document doc;

        public LogTextArea() {
            super();

            this.doc = this.getStyledDocument();
            this.enableEvents(LogEvent.ID); //accetta gli eventi del log

            this.setBackground(Color.BLACK);
            this.setForeground(Color.lightGray);
            this.setCaretColor(Color.WHITE);
            this.setSelectionColor(new Color(180, 180, 180));
            this.setSelectedTextColor(new Color(30, 30, 30));

            this.setEditable(false);

            this.setText(" ================================== Server Starting " + get_data_time() + " ==================================\n");
        }

        private static String get_data_time() {
            String pattern = "dd.MM.yyyy - HH:mm.ss";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Calendar c = Calendar.getInstance();

            return sdf.format(c.getTime());
        }

        @Override
        protected void processEvent(AWTEvent e) {
            if (e instanceof LogEvent) {
                try {
                    LogEvent event = (LogEvent) e; //ricava l'oggetto LogEvent
                    SimpleAttributeSet attrib = new SimpleAttributeSet();

                    StyleConstants.setForeground(attrib, event.get_foreground()); //imposta la grafica del testo che vuole aggiungere al log
                    StyleConstants.setBackground(attrib, event.get_background());
                    StyleConstants.setBold(attrib, event.is_bold());

                    //aggiunge il testo al log e "va in giù" con la scrollbar
                    doc.insertString(doc.getLength(), event.get_txt(), attrib);
                    show_last_line();
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                super.processEvent(e);
            }
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getUI().getPreferredSize(this).width <= getParent().getSize().width;
        }
    }

}

class LogEvent extends AWTEvent {
    public static final int ID = AWTEvent.RESERVED_ID_MAX + 1;
    private static int counter = 0;

    private String txt;
    private Color background = Color.BLACK;
    private Color foreground = Color.lightGray.brighter();
    private boolean bold = false;

    public LogEvent(Object source, String txt) {
        super(source, ID);
        this.txt = txt;
    }

    public LogEvent(Object source, String txt, Color background, Color foreground, boolean bold) {
        super(source, ID);

        this.txt = txt;
        this.background = background;
        this.foreground = foreground;
        this.bold = bold;
    }

    public String get_txt() {
        return this.txt;
    }
    public Color get_background() { return background; }
    public Color get_foreground() { return foreground; }
    public boolean is_bold() { return bold; }
}