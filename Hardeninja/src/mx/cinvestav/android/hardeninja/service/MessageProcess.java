package mx.cinvestav.android.hardeninja.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import mx.cinvestav.android.hardeninja.beans.MessageBean;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class MessageProcess {

	private static final String CLASS_TAG 	= "MessageProcess";
	private static final String DATE_FORMAT = "dd MMMM yyyy 'at' HH:mm:ss";
	
	/**
	 * Search for an activation o deactivation code in the message provided
	 * @param messages
	 * @param codes
	 * @return found code or null if nothing found.
	 */
	public static String getCode(SmsMessage[] messages, String[] codes) {
		
		for (SmsMessage sms : messages) {
			String smsBody = sms.getMessageBody();
			if (TextUtils.isEmpty(smsBody)) {
				Log.e(CLASS_TAG, "there's no message body to read");
				continue;
			}
			for (String code : codes) {
				if (smsBody.contains(code))
					return code;
			}
		}
		
		return null;
	}
	
	/**
	 * Read a message in messages, and return the content in the resulting 
	 * MessageBean.
	 * @param messages
	 * @param codes
	 * @return MessageBean with the information of the sms
	 */
	public static MessageBean readSMS(SmsMessage[] messages, String[] codes) {
		String smsBody, codeInSms, smsDate, smsSrc;
		
		smsBody = ""; 
		smsSrc = smsDate = codeInSms = null;
		for (SmsMessage sms : messages) {
			smsBody = smsBody.concat(sms.getMessageBody());
			if (TextUtils.isEmpty(smsBody)) {
				Log.e(CLASS_TAG, "there's no message body to read");
				continue;
			}
			
			for (String code : codes) { 
				if (smsBody.contains(code)) {
					codeInSms = code;
					break;
				}
			}
			
			Date date = new Date(sms.getTimestampMillis());
			smsDate = MessageProcess.getFormatDate(date);
			
			smsSrc = sms.getDisplayOriginatingAddress();
		}
		
		return new MessageBean(smsSrc, smsBody, smsDate, codeInSms);
	}
	
	/**
	 * Send a SMS to the destination number with the contents in message
	 * @param message
	 * @param destNumber
	 */
	public static void sendSMS(MessageBean message, String destNumber) {
		SmsManager manager = SmsManager.getDefault();
		
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("SMS from: " + message.getSrcNumber());
		bodyBuilder.append(". Date: " + message.getDate());
		bodyBuilder.append(". Content: " + message.getMsgBody());
		
		ArrayList<String> msgParts = manager.divideMessage(
				bodyBuilder.toString());
		for (String part : msgParts)
			manager.sendTextMessage(destNumber, null, part, null, null);
	}
	
	/**
	 * Send a SMS to the destination number with the calling number in src
	 * @param srcNumber
	 * @param destNumber
	 */
	public static void sendSMS(String srcNumber, String destNumber) {
		SmsManager manager = SmsManager.getDefault();
		
		String date = MessageProcess.getFormatDate(new Date());
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("Call from: " + srcNumber);
		bodyBuilder.append(". Date: " + date);
		
		manager.sendTextMessage(destNumber, null, bodyBuilder.toString(),
				null, null);
	}
	
	private static String getFormatDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		return dateFormat.format(date);
	}
}
