package dns;

import gui.Log_panel;
import network.Connection;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.ServerSocket;

public abstract class DomainNameSystem {
    private static final int PORT = 9696;
    private static boolean running = false;

    private static ServerSocket s_socket;

    public static void start() throws IOException {
        if (!running) {
            running = true;
            s_socket = new ServerSocket(PORT);

            new Thread(() -> { //inizia ad aspettare client
                Log_panel.log_write("dns acceso\n", false);

                while (running) {
                    try {
                        Connection c = new Connection(s_socket.accept());
                        new Thread(new Client_DNS(c)).start(); //riceve la richiesta del client e invia la risposta
                    } catch (IOException e) {
                    } //il socket è stato chiuso
                }
            }).start();
        }
    }

    public static void stop() throws IOException {
        if (running) {
            running = false;
            s_socket.close();

            Log_panel.log_write("dns spento\n", false);
        }
    }

    private static class Client_DNS implements Runnable {
        private Connection client;
        public Client_DNS(Connection c) {
            this.client = c;
        }

        @Override
        public void run() {
            try {
                String server_link = client.wait_for_string();
                String request = "error";

                if (server_link.charAt(0) == 'd') { //se il client richiede l'ip conoscendo il link
                    request = Converter.get_ip_of(server_link.substring(1));

                    Log_panel.log_write("un client si è connesso al dns richiedendo l'ip di " + server_link.substring(1) + " = " + request + "\n", false);
                }
                else if (server_link.charAt(0) == 'r') { //se il client richiede il link conoscendo l'ip.
                    request = Converter.get_link_of(server_link.substring(1));

                    Log_panel.log_write("un client si è connesso al dns richiedendo il link di " + server_link.substring(1) + " = " + request + "\n", false);
                }
                else {
                    Log_panel.log_write("è stato ricevuto il messaggio (" + request + ") non riconosciuto\n", true);
                }

                client.write(request);
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
