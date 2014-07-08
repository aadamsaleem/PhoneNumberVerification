package com.example.phonenumberverification;

import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText et_phone;
	private Button send;
	public SharedPreferences prefs;
	SharedPreferences.Editor editor;
	private String phone_no;
	private String message;
	private int code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		et_phone = (EditText) findViewById(R.id.et_phone);
		send = (Button) findViewById(R.id.send);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (et_phone.getText().toString().length() >= 10) {
					phone_no = et_phone.getText().toString();
					code = generateCode();
					message = generateSMS(code);

					editor.putString("sms", message);
					editor.putInt("code", code);
					editor.putString("phone_no", phone_no);
					editor.commit();
					sendSMS(getApplicationContext(), phone_no, message);
					Intent i = new Intent(MainActivity.this, Verify.class);
					startActivity(i);
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Enter Valid Phone Number", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public String generateSMS(int code) {
		String sms;
		sms = "Your verification code is " + code;
		return sms;
	}

	public int generateCode() {
		int code;
		Random ran = new Random();
		code = 100000 + ran.nextInt(900000);
		return code;
	}

	public static void sendSMS(final Context context, final String phoneNumber,
			final String message) {

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber, null, message, null, null);
		Handler handler = new Handler();
		Runnable r = new Runnable() {
			public void run() {
				deletemsg(context, phoneNumber, message);
			}
		};
		handler.postDelayed(r, 5000);

	}

	public static void deletemsg(Context context, String phoneNumber,
			String message) {
		try {
			Uri uriSms = Uri.parse("content://sms/");
			Cursor c = context.getContentResolver().query(
					uriSms,
					new String[] { "_id", "thread_id", "address", "person",
							"date", "body" }, null, null, null);
			Log.e("Cursor has items?", "" + c.moveToFirst());
			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					long threadId = c.getLong(1);
					String address = c.getString(2);
					String person = c.getString(3);
					String date = c.getString(4);
					String body = c.getString(5);

					Log.e("Cursor Values",
							"\n0-->"+c.getColumnName(0) +"-->"+ c.getString(0)+
							"\n1-->"+c.getColumnName(1) +"-->"+ c.getString(1)+
							"\n2-->"+c.getColumnName(2) +"-->"+ c.getString(2)+
							"\n3-->"+c.getColumnName(3) +"-->"+ c.getString(3)+
							"\n4-->"+c.getColumnName(4) +"-->"+ c.getString(4)+
							"\n5-->"+c.getColumnName(5) +"-->"+ c.getString(5));
					
					Log.e(message, body);
					if (message.equals(body)) {
						context.getContentResolver().delete(
								Uri.parse("content://sms/"), "_id=" + id, null);
						Log.e("Log", "Deleteing.....");
						break;
					}
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
