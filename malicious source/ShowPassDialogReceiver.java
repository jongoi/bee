package com.evilDiscounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShowPassDialogReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, Intent intent) {
    // Just trigger the dialog to appear.
    Intent i = new Intent(context, ShowPassDialog.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(i);
  }
}
