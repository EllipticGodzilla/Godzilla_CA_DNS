package network;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket sck;
    private BufferedOutputStream output;
    private BufferedInputStream input;

    public Connection(Socket sck) throws IOException {
        this.sck = sck;
        this.output = new BufferedOutputStream(sck.getOutputStream());
        this.input = new BufferedInputStream(sck.getInputStream());
    }

    public void write(String msg) throws IllegalBlockSizeException, IOException, BadPaddingException {
        write(msg.getBytes());
    }

    public void write(byte[] msg) throws IOException {
        output.write(new byte[] {(byte) (msg.length & 0xff), (byte) ((msg.length >> 8) & 0xff)});
        output.write(msg);

        output.flush();
    }

    public String wait_for_string() throws IllegalBlockSizeException, IOException, BadPaddingException {
        return new String(wait_for_bytes());
    }

    public byte[] wait_for_bytes() throws IOException, IllegalBlockSizeException, BadPaddingException {
        byte[] msg_size_byte = input.readNBytes(2); //legge la dimensione del messaggio che sta arrivando
        int msg_len = (msg_size_byte[0] & 0Xff) | (msg_size_byte[1] << 8); //trasforma i due byte appena letti in un intero

        byte[] msg = input.readNBytes(msg_len); //legge il messaggio appena arrivato

        return msg;
    }

    public void close() throws IOException {
        sck.close();
        output.close();
        input.close();
    }
}
