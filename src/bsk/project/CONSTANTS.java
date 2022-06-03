package bsk.project;

public class CONSTANTS {
    public static int port = 25565;
    public static String ipAddr = "153.19.219.156";       // w aka

    //encryption - sessionKey
    public static String AesAlgName = "AES";                        //AES
    public static String AesAlgECBMode = "AES";                     //AES tryb ECB
    public static String AesAlgCBCMode = "AES/CBC/PKCS5Padding";    //AES tryb CBC
    public static int sessionKeySize = 256;                         //128, 192 or 256
    public static int ivSize = 16;

    //encryption - public, private keys
    public static String RsaAlgName = "RSA";                        //RSA
    public static int keyPairSize = 2048;
    public static String keyPath = "keys/";

    //local key
    public static int shaKeyLength = 256;
    public static String saltAlg = "SHA1PRNG";
    public static String ShaAlg = "PBKDF2WithHmacSHA256";

    //files
    public static double partFileMaxLength = 500 * 1024 * 1024;
}
