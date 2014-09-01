package com.evilDiscounts;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

/*
 * This class includes some basic helper methods for sms messages.
 */
public class SmsHelpers {
	// This method returns an array of all sms messages not already scanned.
	public static Sms[] getSmsDetails(Context context) {
		String SORT_ORDER = "date DESC";
		int count = 0;
		
		// Get the lastTime pref to only scan messages we haven't scanned.
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    long lastTime = prefs.getLong("lastTime", -1);

		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms"),
				new String[] { "_id", "thread_id", "address", "person", "date",
						"body", "type" }, "date > " + lastTime, null, SORT_ORDER);

		if (cursor != null) {
			try {
				count = cursor.getCount();
				if (count > 0) {
					ArrayList<Sms> smsArray = new ArrayList<Sms>();
					cursor.moveToFirst();
					long newLastTime = cursor.getLong(4);
					do{
						long messageId = cursor.getLong(0);
						long threadId = cursor.getLong(1);
						String address = cursor.getString(2);
						String otherName = getContactName(context, formatNumber(address));
						long timestamp = cursor.getLong(4);
						String body = cursor.getString(5);
						int type = cursor.getInt(6);
						
						Sms smsMessage = new Sms(address, otherName, timestamp, messageId, threadId, body, type);
						smsArray.add(smsMessage);
					}while(cursor.moveToNext());
					
					// Update the newest timestamp scanned
					prefs.edit().putLong("lastTime", newLastTime).commit();
					return (Sms[])smsArray.toArray(new Sms[smsArray.size()]);

				}
			} finally {
				cursor.close();
			}
		}
		return null;
	}
	
	
	// This method attempts to get the contact name from a phone number.
	private static String getContactName(Context context, String otherNumber) {
	  String where = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + otherNumber + "'";
	  Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	          null, where, null, null);
	  String name = "Self";
	  if (cursor.getCount() > 0) {
	    cursor.moveToFirst();
        name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	  } else {
	    // Try without the dashes.
	    where = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + otherNumber.replaceAll("-", "").substring(1) + "'";
	    cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	              null, where, null, null);
	    if (cursor.getCount() > 0) {
	        cursor.moveToFirst();
	        name = cursor.getString(
	                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
	    }
	  }
	  return name;
	}
	
	// This method formats the contact number in an attempt to match how it was entered by the user.
	public static String formatNumber(String number){
	  String result = "";
	  if (number.startsWith("+")){
	    result = number.substring(0, 2) + "-" + number.substring(2,5) + "-" + 
	        number.substring(5,8) + "-" + number.substring(8);
	  } else if (number.startsWith("1")) {
	    result = number.substring(0, 1) + "-" + number.substring(1,4) + "-" + 
            number.substring(4,7) + "-" + number.substring(7);
	  } else {
	    result = number.substring(0, 3) + "-" + number.substring(3,6) + "-" + number.substring(6);
	  }
	  return result;
	}
}
