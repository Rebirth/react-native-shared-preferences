package in.sriraman.sharedpreferences;

import android.content.Context;
import android.os.Build;
import android.os.OperationCanceledException;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Encryptor {
    private final String AES_MODE = "AES/GCM/NoPadding";
    private String keystore_string = "keystore";
    private String key_alias = "key_alias";
    private String iv_key;
    private KeyStore keyStore;
    private Context context;
    private ApplicationHelper helper;
    private static Encryptor mEncryptor;

    public Encryptor(String keystore_string, String key_alias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            // can't store keys below api version 23
            throw new OperationCanceledException();
        }

        this.keystore_string = keystore_string;
        this.key_alias = key_alias;

        this.keyStore = KeyStore.getInstance(keystore_string);
        this.keyStore.load(null);
        generateKey();
    }

    public static Encryptor getInstance() {
      return mEncryptor;
    }

    public static Encryptor init(String keystore_string, String key_alias) {
      if (mEncryptor == null) {
        mEncryptor = new Encryptor(keystore_string, key_alias);
      }

      return mEncryptor;
    }

    public String encrypt(String toEncrypt) {


        try {
            // don't encrypt without a key
            if (iv_key == null || mEncryptor == null) {
                return null;
            }

            Cipher c = Cipher.getInstance(AES_MODE);
            c.init(Cipher.ENCRYPT_MODE, mEncryptor.getKey(), new GCMParameterSpec(128, mEncryptor.GenerateIV()));
            byte[] encodedBytes = c.doFinal(toEncrypt.getBytes());
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public String decryptString(String toDecrypt) {
        try {
            // can't decrypt without a key
            if (iv_key == null || mEncryptor == null) {
                return null;
            }

            Cipher c = Cipher.getInstance(AES_MODE);
            c.init(Cipher.DECRYPT_MODE, mEncryptor.getKey(), new GCMParameterSpec(128, mEncryptor.GenerateIV()));

            byte[] encrypted_bytes = Base64.decode(toDecrypt, Base64.DEFAULT);
            byte[] decodedBytes = c.doFinal(encrypted_bytes);

            // return the decoded string
            return new String(decodedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public String encrypt(int toEncrypt) {
        return encrypt(String.valueOf(toEncrypt));
    }

    public int decryptInt(String toDecrypt,int fallback) {
        String decrypted = decryptString(toDecrypt);

        if (decrypted != null) {
            return Integer.valueOf(decrypted);
        } else {
            return fallback;
        }
    }

    public String encrypt(float toEncrypt) {
        return encrypt(String.valueOf(toEncrypt));
    }

    public float decryptFloat(String toDecrypt,float fallback) {
        String decrypted = decryptString(toDecrypt);

        if (decrypted != null) {
            return Float.valueOf(decrypted);
        } else {
            return fallback;
        }
    }

    public String encrypt(long toEncrypt) {
        return encrypt(String.valueOf(toEncrypt));
    }

    public long decryptLong(String toDecrypt,long fallback) {
        String decrypted = decryptString(toDecrypt);

        if (decrypted != null) {
            return Long.valueOf(decrypted);
        } else {
            return fallback;
        }
    }



    private Key getKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return keyStore.getKey(key_alias, null);
    }

    private void setIVKey(String iv_key) {
        this.iv_key = iv_key
    }

    private void generateKey() throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            // can't store keys below api version 23
            throw new OperationCanceledException();
        }

        if (!keyStore.containsAlias(key_alias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keystore_string);
            keyGenerator.init(new KeyGenParameterSpec.Builder(key_alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(false).build());
            SecretKey secretKey = keyGenerator.generateKey();
        }
    }

    private byte[] GenerateIV() throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(iv_key.getBytes());

        return Arrays.copyOfRange(messageDigest.digest(), 0, 12);
    }
}
