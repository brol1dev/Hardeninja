package mx.cinvestav.android.hardeninja.receiver;

import mx.cinvestav.android.hardeninja.config.CommunicationConfig;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Listens for system events. They are:
 * - SMS_RECEIVED
 * - PHONE_STATE (to know about an incoming phone call)
 * - BOOT_COMPLETED
 * 
 * @author Eric Vargas
 *
 */
public class EventReceiver extends BroadcastReceiver {

	private static final String CLASS_TAG = "EventReceiver";
	
	private static final String SMS_RECEIVED = 
			"android.provider.Telephony.SMS_RECEIVED";
	private static final String PHONE_STATE =
			"android.intent.action.PHONE_STATE";
	
	/**
	 * When the intent has a BOOT_COMPLETED action, this will inicialize
	 * the service running on background in the device.
	 * When the intent has a SMS_RECEIVED action, it will pass the SMS
	 * to the service.
	 * When the intent has a PHONE_STATE action, it will pass the call
	 * to the service.
	 * 
	 * @param context
	 * @param intent  
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d(CLASS_TAG, "catch action: " + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent sIntent = new Intent(CommunicationConfig.BOOT_COMPLETE);
			context.startService(sIntent);
		
		} else if (intent.getAction().equals(SMS_RECEIVED)) {
			Bundle bundle = intent.getExtras();
			this.processSMS(context, bundle);
		
		} else if (intent.getAction().equals(PHONE_STATE)) {
			Bundle bundle = intent.getExtras();
			this.processCall(context, bundle);
		}
		
	}

	private void processSMS(Context context, Bundle bundle) {
		Intent intent = new Intent(CommunicationConfig.COMMUNICATE_SMS);
		intent.putExtras(bundle);
		context.startService(intent);
	}
	
	private void processCall(Context context, Bundle bundle) {
		Intent intent = new Intent(CommunicationConfig.COMMUNICATE_CALL);
		intent.putExtras(bundle);
		context.startService(intent);
	}
}
