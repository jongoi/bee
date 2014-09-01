package com.evilDiscounts;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/*
 * This service runs in the background and waits for a call to end.
 * Once a call ends, it sets an alarm to send an sms to the person the user
 * just spoke with in 1 minute.
 */
public class CallService extends Service {
  int lastState = -1;
  PhoneStateListener listener;
  TelephonyManager tm;
  
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("CALL_SERVICE_STARTED", "started the call service");
    listener = new MyPhoneStateListener();
    tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
  }
  
  public class MyPhoneStateListener extends PhoneStateListener {
    // Listen for a call to end.
    public void onCallStateChanged(int state,String incomingNumber){
      if (CallService.this.lastState == TelephonyManager.CALL_STATE_OFFHOOK
              && state == TelephonyManager.CALL_STATE_IDLE){
        // A call just ended. Set the sendSmsAlarm.
        setSendSmsAlarm();
      }
      CallService.this.lastState = state;
    }
  }

  // Send a message to the person the user just talked to in 1 minute.
  private void setSendSmsAlarm() {
    Intent i = new Intent(this, SendSMSReceiver.class);
    GregorianCalendar cal = new GregorianCalendar();
    int _id = (int) System.currentTimeMillis();
    PendingIntent appIntent =
        PendingIntent.getBroadcast(this, _id, i, PendingIntent.FLAG_ONE_SHOT);

    cal.add(Calendar.MINUTE, 1);
    Log.i("SEND_SMS_ALARM_SET", "Set send sms alarm for " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE));
    AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            appIntent);
  }
  
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
}
