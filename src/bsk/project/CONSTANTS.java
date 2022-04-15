package bsk.project;

public class CONSTANTS {
    public static int port = 25565;
    //public static String ipAddr = "153.19.219.156";       // w aka
    public static String ipAddr = "192.168.1.40";       // w domu

    //encryption - sessionKey
    public static String AesAlgName = "AES";                        //AES
    public static String AesAlgECBMode = "AES";                     //AES tryb ECB
    public static String AesAlgCBCMode = "AES/CBC/PKCS5Padding";    //AES tryb CBC
    public static int sessionKeySize = 256;                         //128, 192 or 256

    public static String RsaAlgName = "RSA";                        //RSA
    public static int keyPairSize = 2048;
    public static String keyPath = "keys/";
}
