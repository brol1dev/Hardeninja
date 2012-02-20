package mx.cinvestav.android.hardeninja.service;

import mx.cinvestav.android.hardeninja.beans.MessageBean;
import mx.cinvestav.android.hardeninja.beans.PreferencesData;
import mx.cinvestav.android.hardeninja.config.CommunicationConfig;
import mx.cinvestav.android.hardeninja.config.PreferencesConfig;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class ControllerService extends Service {

	private static final String CLASS_TAG = "ControllerService";
	private PreferencesData preferences;
	private static int nThreads;
	private boolean isForwardMode;
	
	
	
	/**
	 * When this is created, we want to read the preferences of the
	 * application. This is helpful to store in which mode we are even
	 * if the device is starting again.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(CLASS_TAG, "Service created");
		nThreads = 0;
		
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(this);
		
		// Load the mode or create it if there's no key for the mode.
		try {
			String modeStr = prefs.getString(PreferencesConfig.MODE_KEY, null);
			if (TextUtils.isEmpty(modeStr)) {
				SharedPreferences.Editor prefsEdit = prefs.edit();
				prefsEdit.putBoolean(PreferencesConfig.MODE_KEY, 
						PreferencesConfig.MODE_DEF);
				prefsEdit.apply();
				Log.d(CLASS_TAG, "Init service to no forward mode");
			}
			isForwardMode = PreferencesConfig.MODE_DEF;
		} catch (ClassCastException e) {
			isForwardMode = prefs.getBoolean(PreferencesConfig.MODE_KEY, 
					PreferencesConfig.MODE_DEF);
		}
		
		preferences = new PreferencesData();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	
		Log.d(CLASS_TAG, "Service started in " 
				+ (isForwardMode ? "forward": "no forward") + " mode");
		Log.d(CLASS_TAG, "intent action received: " + intent.getAction());
		
		if (intent.getAction().equals(CommunicationConfig.COMMUNICATE_SMS)) {
			
			SharedPreferences prefs = PreferenceManager.
					getDefaultSharedPreferences(this);
			
			String smsPhone = prefs.getString(PreferencesConfig.PHONE_KEY, 
					PreferencesConfig.PHONE_DEF);
			String smsOnCode = prefs.getString(PreferencesConfig.ONCODE_KEY, 
					PreferencesConfig.ONCODE_DEF);
			String smsOffCode = prefs.getString(PreferencesConfig.OFFCODE_KEY, 
					PreferencesConfig.OFFCODE_DEF);
			preferences.setSmsPhone(smsPhone);
			preferences.setSmsOnCode(smsOnCode);
			preferences.setSmsOffCode(smsOffCode);
			
			HandlerThread thread = new HandlerThread("SMSThread" 
					+ ControllerService.nThreads++, 
					Process.THREAD_PRIORITY_BACKGROUND);
			thread.start();
			
			Looper smsLooper = thread.getLooper();
			SMSHandler handler = new SMSHandler(smsLooper);
			
			Bundle bundle = intent.getExtras();
			Message msg = handler.obtainMessage();
//			msg.arg1 = startId;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d(CLASS_TAG, "Service destroyed");
	}
	
	/**
	 * Class used to threat sms functionality
	 *
	 */
	private final class SMSHandler extends Handler {
		
		private static final String SMS_TAG = CLASS_TAG + "$SMSHandler";
		public SMSHandler(Looper looper) {
			super(looper);
		}
		
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String smsCode = null;
			if (bundle != null) {
				Log.d(SMS_TAG, "sms received");
				SharedPreferences prefs = PreferenceManager.
						getDefaultSharedPreferences(getBaseContext());
				
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];
				// populate messages
				for (int i = 0; i < pdus.length; ++i)
					messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				
				String[] codes = { preferences.getSmsOnCode(), 
						preferences.getSmsOffCode() };
				isForwardMode = prefs.getBoolean(PreferencesConfig.MODE_KEY, 
						PreferencesConfig.MODE_DEF);
				
				if (isForwardMode) {
					MessageBean message = MessageProcess.readSMS(messages, codes);
					
					if (!TextUtils.isEmpty(message.getCode()) 
							&& message.getCode().equals(
							preferences.getSmsOffCode())) {
						isForwardMode = false;
						SharedPreferences.Editor prefsEdit = prefs.edit();
						prefsEdit.putBoolean(PreferencesConfig.MODE_KEY, 
								isForwardMode);
						prefsEdit.apply();
						Log.d(SMS_TAG, "Service changed to no forward mode");
					} else {
						MessageProcess.sendSMS(message, 
								preferences.getSmsPhone());
						Log.d(SMS_TAG, "SMS sent to " 
								+ preferences.getSmsPhone());
					}
					
				} else {
					smsCode = MessageProcess.getCode(messages, codes);
					if (TextUtils.isEmpty(smsCode))
						Log.d(SMS_TAG, "found no code in sms");
					else {
						if (smsCode.equals(preferences.getSmsOnCode())) {
							isForwardMode = true;
							SharedPreferences.Editor prefsEdit = prefs.edit();
							prefsEdit.putBoolean(PreferencesConfig.MODE_KEY, 
									isForwardMode);
							prefsEdit.apply();
							Log.d(SMS_TAG, "Service changed to forward mode");
						}
					}
					
				}
			}
		}
	}
	
	/**
	 * Never necessary for this service. We just want it to run 
	 * in background.
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
