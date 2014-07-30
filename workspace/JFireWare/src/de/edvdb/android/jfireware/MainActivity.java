package de.edvdb.android.jfireware;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {
	static String address = "";
	private GoogleMap map;
	private TextToSpeech mTts;
	private boolean ttsEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
			DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
			SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
			db.close();
		}

		mTts = new TextToSpeech(this, this);

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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	private static Context mContext;

	public static Context getContext() {
		return mContext;
	}

	public static void setContext(Context mContext) {
		MainActivity.mContext = mContext;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mContext == null) {
			mContext = this;
		}

		zoomToPos(Constants.BASE, Constants.MAP_ZOOM, "FWGH",
				BitmapDescriptorFactory.HUE_GREEN);

		final Button button_start = (Button) findViewById(R.id.button_start);
		button_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				try {
					String encodedurl = URLEncoder
							.encode(address, "ISO-8859-1");
					Intent i = new Intent(Intent.ACTION_VIEW, Uri
							.parse("google.navigation:q=" + encodedurl));
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getApplicationContext().startActivity(i);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext);
		SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

		Cursor cursor = db
				.query("jfwtable", null, null, null, null, null, null);
		if (cursor.moveToLast()) {
			button_start.setEnabled(true);
			int colAdress = cursor.getColumnIndex("address");
			address = cursor.getString(colAdress);
			int colBody = cursor.getColumnIndex("body");
			String smsBody = cursor.getString(colBody);
			EditText text = (EditText) findViewById(R.id.editText1);
			text.setText(smsBody);
			text.setEnabled(false);

			Geocoder geocoder = new Geocoder(this);
			try {
				List<Address> addresses = geocoder.getFromLocationName(address,
						1);
				if (addresses != null && addresses.size() > 0) {
					Address target = addresses.get(0);
					LatLng latLng = new LatLng(target.getLatitude(),
							target.getLongitude());
					zoomToPos(latLng, Constants.MAP_ZOOM, address,
							BitmapDescriptorFactory.HUE_RED);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (ttsEnabled) {
				mTts.speak(smsBody, TextToSpeech.QUEUE_FLUSH, null);
			} else {
				Toast.makeText(this, "TTS not available", Toast.LENGTH_LONG)
						.show();
			}
			cursor.close();
		} else {
			button_start.setEnabled(false);
		}
		db.close();
	}

	private void zoomToPos(LatLng latLng, int mapZoom, String title, float hue) {
		MapFragment mapFragment = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.map));

		if (mapFragment != null) {
			map = mapFragment.getMap();
			Marker marker = map.addMarker(new MarkerOptions().position(latLng)
					.title(title)
					.icon(BitmapDescriptorFactory.defaultMarker(hue)));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoom));
			map.animateCamera(CameraUpdateFactory
					.newLatLngZoom(latLng, mapZoom));
		}
	}

	private void SpeakSomething(String toSpeak) {

	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		super.onDestroy();
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = mTts.setLanguage(Locale.GERMANY);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("jfwtts", "Language is not available.");
				ttsEnabled = false;
			} else {
				ttsEnabled = true;
			}
		} else {
			Log.e("jfwtts", "Could not initialize TextToSpeech.");
			ttsEnabled = false;
		}
	}
}
