package in.sriraman.sharedpreferences;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.*;

import android.bluetooth.BluetoothAdapter;
import com.facebook.react.bridge.Callback;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class RNSharedPreferencesModule extends ReactContextBaseJavaModule {


	final int BT_ACTION_REQUEST_ENABLE = 1;

	private BluetoothAdapter bt_adapter = null;
	private ListView bt_list_view;
	private ArrayList<BluetoothDevice> bt_device_list = null;
	private boolean bt_scanning = false;
	private boolean is_watch = false;
	private String shared_name = "react_native_shared_preferences";

	private String keystore;
	private String key_alias;
	private String key;
	private boolean encrypted = false;

	private BroadcastReceiver bt_info_receiver = null;




	public void onCreate(Bundle savedInstanceState) {


	}


	private void initSharedHandler() {
		SharedHandler.init(getReactApplicationContext(), shared_name);
	}

	private void initEncryptedHandler() {
		EncryptedHandler.init(getReactApplicationContext(), shared_name, keystore, key_alias, key);
	}

	public RNSharedPreferencesModule(ReactApplicationContext reactContext) {
		super(reactContext);
	}

	@Override
	public String getName() {
		return "SharedPreferences";
	}

	@ReactMethod
	public void setName(String name) {
		shared_name = name;
	}

	@ReactMethod
	public void enableEncryption(String keystore, String key_alias, String key) {
		this.encrypted = true;
		
		this.keystore = keystore;
		this.key_alias = key_alias;
		this.key = key;
	}

	@ReactMethod
	public void disableEncryption() {
		this.encrypted = false;

		this.keystore = null;
		this.key_alias = null;
		this.key = null;
	}


	@ReactMethod
	public void setItem(String key, String value) {

		if (encrypted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			initEncryptedHandler();
			EncryptedHandler.getInstance().putString(key, value);
		} else {
			initSharedHandler();
			SharedDataProvider.putSharedValue(key,value);
		}

	}

	@ReactMethod
	public void getItem(String key, Callback successCallback){

		if (encrypted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			initEncryptedHandler();
			String value = EncryptedHandler.getInstance().getString(key);
			successCallback.invoke(value);
		} else {
			initSharedHandler();
			String value = SharedDataProvider.getSharedValue(key);
			successCallback.invoke(value);
		}
	}


	/***
	 * getItems(): returns Native Array of Preferences for the given values
	 * */
	@ReactMethod
  public void getItems(ReadableArray keys, Callback successCallback){
    initSharedHandler();
    String[] keysArray= new String[keys.size()];
    for (int i=0;i<keys.size();i++){
	    keysArray[i]=keys.getString(i);
    }
    String[] [] values = SharedDataProvider.getMultiSharedValues(keysArray);
    WritableNativeArray data = new WritableNativeArray();
    for(int i=0;i<keys.size();i++){
	    data.pushString(values[i][1]);
    }
    successCallback.invoke(data);
  }

	@ReactMethod
	public void getAll(Callback successCallback){
		initSharedHandler();
		String[][] values = SharedDataProvider.getAllSharedValues();
		WritableNativeArray data = new WritableNativeArray();
		for(int i=0; i<values.length; i++){
			WritableArray arr = new WritableNativeArray();
			arr.pushString(values[i][0]);
			arr.pushString(values[i][1]);
			data.pushArray(arr);
		}
		successCallback.invoke(data);
	}

	/*
	   @ReactMethod
	   public void multiGet(String[] keys, Callback successCallback){

	   SharedHandler.init(getReactApplicationContext());
	   String[][] value = SharedDataProvider.getMultiSharedValues(keys);
	   successCallback.invoke(value);

	   }

	 */

	@ReactMethod
	public void getAllKeys(Callback successCallback){
		initSharedHandler();
		String[] keys = SharedDataProvider.getAllKeys();
		WritableNativeArray data = new WritableNativeArray();
		for(int i=0; i<keys.length; i++){
			data.pushString(keys[i]);
		}
		successCallback.invoke(data);
	}



	@ReactMethod
	public void clear(){
		initSharedHandler();
		SharedDataProvider.clear();
	}


	@ReactMethod
	public void removeItem(String key) {
		initSharedHandler();
		SharedDataProvider.deleteSharedValue(key);
	}


}
