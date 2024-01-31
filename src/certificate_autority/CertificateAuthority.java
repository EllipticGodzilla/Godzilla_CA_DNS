package certificate_autority;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public abstract class CertificateAuthority {
    private static Cipher prv_encoder;
    private static MessageDigest sha3_256;

    public static void init() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeyException, InvalidKeySpecException {
        //legge la chiave privata della ca
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(CertificateAuthority.class.getClassLoader().getResourceAsStream("file/CAPrivateKey.dat").readAllBytes()));

        //inizializza l'encoder con la chiave privata
        prv_encoder = Cipher.getInstance("RSA");
        prv_encoder.init(Cipher.ENCRYPT_MODE, key);

        //inizializza sha3_256
        sha3_256 = MessageDigest.getInstance("SHA3-256");
    }

    public static String calculate_ce(String name, String link, String ip, String pub_key, String mail) throws IllegalBlockSizeException, BadPaddingException {
        byte[] hash = sha3_256.digest((name + ";" + link + ";" + ip + ";" + pub_key + ";" + mail).getBytes()); //calcola l'hash delle server info
        byte[] ce = prv_encoder.doFinal(hash); //cifra l'hash con la chiave privata
        return Base64.getEncoder().encodeToString(ce); //ritorna il certificato codificato con Base64
    }
}
