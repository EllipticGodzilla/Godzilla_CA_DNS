package certificate_autority;

import dns.Converter;
import gui.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public abstract class CertificateAuthority {
    public static String pub_key;
    public static byte[] key_test_bytes;

    private static Cipher prv_encoder;
    private static MessageDigest sha3_256;

    public static void init() throws NoSuchAlgorithmException, IOException {
        //memorizza i 32 byte per testare la password
        key_test_bytes = CertificateAuthority.class.getClassLoader().getResourceAsStream("files/FileKey.dat").readAllBytes();

        //inizializza sha3_256
        sha3_256 = MessageDigest.getInstance("SHA3-256");

        request_psw.success(); //richiede la password
    }

    private static TempPanel_action request_psw = new TempPanel_action() {
        @Override
        public void success() {
            TempPanel.show(new TempPanel_info( //richiede la password per decifrare i file
                    TempPanel_info.INPUT_REQ,
                    false,
                    "inserisci la password:"
            ).set_psw_indices(0), read_psw);
        }

        @Override
        public void fail() {} //non può essere premuto annulla
    };

    private static TempPanel_action read_psw = new TempPanel_action() {
        @Override
        public void success() {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA3-512");

                String psw = input.elementAt(0); //legge la password
                byte[] psw_hash = md.digest(psw.getBytes());

                if (Arrays.compare(Arrays.copyOfRange(psw_hash, 32, 64), key_test_bytes) == 0) { //se la password è corretta
                    //utilizza i primi 32byte dell'hash come key ed iv per inizializzare AES
                    byte[] key_bytes = Arrays.copyOf(psw_hash, 16);
                    byte[] iv_bytes = Arrays.copyOfRange(psw_hash, 16, 32);

                    SecretKey key = new SecretKeySpec(key_bytes, "AES");
                    IvParameterSpec iv = new IvParameterSpec(iv_bytes);

                    //inzializza encrypter e decrypter con key ed iv appena calcolati
                    Main_frame.file_decoder = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    Main_frame.file_encoder = Cipher.getInstance("AES/CBC/PKCS5Padding");

                    Main_frame.file_encoder.init(Cipher.ENCRYPT_MODE, key, iv);
                    Main_frame.file_decoder.init(Cipher.DECRYPT_MODE, key, iv);

                    init_prv_encoder();
                    ServerList_panel.init_list();
                    Converter.init();
                }
                else {
                    fail();
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void fail() { //password sbagliata
            TempPanel.show(new TempPanel_info(
                    TempPanel_info.SINGLE_MSG,
                    false,
                    "la password inserita è sbagliata"
            ), request_psw);
        }
    };

    public static void init_prv_encoder() throws IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        //legge la chiave pubblica della ca
        pub_key = new String(Main_frame.file_decoder.doFinal(CertificateAuthority.class.getClassLoader().getResourceAsStream("files/CAPublicKey.dat").readAllBytes()));

        //legge la chiave privata della ca
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(Main_frame.file_decoder.doFinal(CertificateAuthority.class.getClassLoader().getResourceAsStream("files/CAPrivateKey.dat").readAllBytes())));

        //inizializza l'encoder con la chiave privata
        prv_encoder = Cipher.getInstance("RSA");
        prv_encoder.init(Cipher.ENCRYPT_MODE, key);
    }

    public static String calculate_ce(String name, String link, String ip, String pub_key, String mail) throws IllegalBlockSizeException, BadPaddingException {
        byte[] hash = sha3_256.digest((name + ";" + link + ";" + ip + ";" + pub_key + ";" + mail).getBytes()); //calcola l'hash delle server info
        byte[] ce = prv_encoder.doFinal(hash); //cifra l'hash con la chiave privata

        return Base64.getEncoder().encodeToString(ce); //ritorna il certificato codificato con Base64
    }

    public static boolean can_start() {
        return pub_key != null;
    }
}
