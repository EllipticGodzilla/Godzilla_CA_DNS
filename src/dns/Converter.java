package dns;

import gui.ServerInfo;
import gui.ServerList_panel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Converter {
    private static Map<String, String> link_ip = new LinkedHashMap<>(); //link -> ip
    public static void init() {
        ServerInfo[] servers_info = ServerList_panel.get_info(); //ricava tutte le informazioni dei server registrati
        for (ServerInfo info : servers_info) { //crea la mappa link -> ip
            link_ip.put(info.link, info.ip);
        }
    }

    public static void add_map(String link, String ip) {
        link_ip.put(link, ip);
    }

    public static String get_ip_of(String link) {
        String ip;
        if ((ip = link_ip.get(link)) == null) { //non è registrato nessun server con quell'indirizzo ip
            return "error, the ip is not registered";
        }
        else {
            return ip;
        }
    }

    public static String get_link_of(String ip) {
        ArrayList<String> val_list = new ArrayList(link_ip.values());
        ArrayList<String> key_list = new ArrayList(link_ip.keySet());
        int i = val_list.indexOf(ip);
        if (i == -1) { //non esiste nessun ip legato a questo link
            return "error, the link do not exist";
        }
        else {
            return key_list.get(i);
        }
    }
}
