package com.example.phonenumberverification;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Verify extends Activity {

	private EditText et_code;
	private Button verify,resend;
	private String sms;
	ProgressBar mProgressBar;
	CountDownTimer mCountDownTimer;
	SharedPreferences prefs;
	TextView waiting,time;
	private SmsListener reciever;
	String phone_no=null,message = null;
	int i = 60;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify);

		prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		time = (TextView)findViewById(R.id.time);
		waiting = (TextView)findViewById(R.id.waiting);
		resend = (Button)findViewById(R.id.resend);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mProgressBar.setProgress(i);
		mCountDownTimer = new CountDownTimer(60000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				i--;
				mProgressBar.setProgress(i);
				time.setText(i+"s");

			}

			@Override
			public void onFinish() {
				// Do what you want
				i--;
				mProgressBar.setProgress(i);
				et_code.setVisibility(View.VISIBLE);
				verify.setVisibility(View.VISIBLE);
				resend.setVisibility(View.VISIBLE);
				waiting.setVisibility(View.INVISIBLE);
				time.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
			}
		};
		mCountDownTimer.start();

		et_code = (EditText) findViewById(R.id.et_code);
		verify = (Button) findViewById(R.id.verify);

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(2147483647);
		reciever = new SmsListener();
		registerReceiver(reciever, filter,
				"android.permission.RECEIVE_SMS", null);

		verify.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sms = "Your verification code is " + et_code.getText();
				verify(sms);
			}
		});
		
		resend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				phone_no=prefs.getString("phone_no", null);
				message = prefs.getString("sms", null);
				MainActivity.sendSMS(getApplicationContext(),phone_no, message);
			}
		});
	}

	public void verify(String message) {
		int code;
		String sms, phone_no;
		code = prefs.getInt("code", 0);
		sms = prefs.getString("sms", null);
		phone_no = prefs.getString("phone_no", null);
		if (message.equals(sms)) {
			Toast.makeText(getApplicationContext(), "Verified",
					Toast.LENGTH_LONG).show();
			Intent i = new Intent(getApplicationContext(), Success.class);
			startActivity(i);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Invalid Code",
					Toast.LENGTH_LONG).show();
		}

	}

	public class SmsListener extends BroadcastReceiver {

		public SmsListener() {
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					"android.provider.Telephony.SMS_RECEIVED")) {
				Bundle bundle = intent.getExtras(); // ---get the SMS message
													// passed in---
				SmsMessage[] msgs = null;
				String msg_from;
				if (bundle != null) {
					// ---retrieve the SMS message received---
					try {
						Object[] pdus = (Object[]) bundle.get("pdus");
						msgs = new SmsMessage[pdus.length];
						for (int i = 0; i < msgs.length; i++) {
							msgs[i] = SmsMessage
									.createFromPdu((byte[]) pdus[i]);
							msg_from = msgs[i].getOriginatingAddress();
							String msgBody = msgs[i].getMessageBody();
							verify(msgBody);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
	  super.onDestroy();
	  unregisterReceiver(reciever);
	}
}
