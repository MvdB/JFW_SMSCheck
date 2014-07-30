package de.edvdb.android.jfireware;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// —get the SMS message passed in—
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String messages = "";
		if (bundle != null) {
			// —retrieve the SMS message received—
			Object[] smsExtra = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[smsExtra.length];

			for (int i = 0; i < msgs.length; i++) {
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
				// take out content from sms
				String body = sms.getMessageBody().toString();
				String address = sms.getOriginatingAddress();

				messages += "SMS from " + address + " :\n";
				messages += body + "\n";

				checkForAlert(sms, context);
			}
			// —display the new SMS message—
			Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
		}
	}

	private void checkForAlert(SmsMessage sms, Context context) {
		// Adresse extrahieren
		String message = sms.getMessageBody();
		if (!(message.contains(Constants.START_PATTERN) && message.contains(Constants.END_PATTERN))) {
			return;
		} else {
			MainActivity.setContext(context);
			int beginIndex = message.indexOf(Constants.START_PATTERN) + Constants.START_PATTERN.length();
			int endIndex = message.indexOf(Constants.END_PATTERN);
			String address = message.substring(beginIndex, endIndex).trim();
			endIndex = message.indexOf(Constants.END_PATTERN) + Constants.END_PATTERN.length();
			String cause = message.substring(endIndex).trim();

			DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
			SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
			String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
			// Create SMS row
			ContentValues values = new ContentValues();

			values.put("address", address);
//			values.put("cause", cause);
			values.put("date", mydate);
			values.put("body", sms.getMessageBody().toString());

			db.insert(Constants.DB_TABLE_NAME, null, values);

			db.close();
			
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}
}
