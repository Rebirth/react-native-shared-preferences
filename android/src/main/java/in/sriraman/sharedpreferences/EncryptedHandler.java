package in.sriraman.sharedpreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.Set;

public class EncryptedHandler {

    private SharedPreferences mEncryptedPreferences;

    private static EncryptedHandler mEncryptedHandler;
    private static String sName = "Preferences";
    private static String mIVKey = "";
    private Encryptor encryptor;

    public EncryptedHandler(Context context, String name, String keystore, String key_alias, String iv_key) {
        mEncryptedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        this.encryptor = Encryptor.init(keystore, key_alias).setIVKey(iv_key);
    }

    public static EncryptedHandler getInstance() {
        return mEncryptedHandler;
    }

    public static void init(Context context, String name, String keystore, String key_alias, String iv_key) {
        if (mEncryptedHandler == null || !name.equals(sName) || !iv_key.equals(mIVKey)) {
            mEncryptedHandler = new EncryptedHandler(context, name, keystore, key_alias, iv_key);
            sName = name;
            mIVKey = iv_key;
        }
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mEncryptedPreferences.edit();
        String encryptedValue = encryptor.encrypt(value);
        if (encryptedValue != null) {
            editor.putString(key, encryptedValue).commit();
        }
    }

    public String getString(String key) {
      String encryptedVal = mEncryptedPreferences.getString(key, "");
      return encryptor.decryptString(encryptedVal);
    }

    public Float getFloat(String key) {
      String encryptedVal = mEncryptedPreferences.getString(key, null);
      return encryptor.decryptFloat(encryptedVal, 0);
    }

    public Long getLong(String key) {
      String encryptedVal = mEncryptedPreferences.getString(key, null);
      return encryptor.decryptLong(encryptedVal, 0);
    }

    public Boolean getBoolean(String key) {
        return mEncryptedPreferences.getBoolean(key, false);
    }

    public Integer getInt(String key) {
      String encryptedVal = mEncryptedPreferences.getString(key, null);
      return encryptor.decryptInt(encryptedVal, 0);
    }

    public void clear() {
        mEncryptedPreferences.edit().clear().commit();
    }

    public Map<String, ?> getAllSharedData(){
        return mEncryptedPreferences.getAll();
    }

    public void deleteKey(String key) {
        SharedPreferences.Editor editor = mEncryptedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

}
