package com.mantz_it.androiddsplibbenchmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Android DSP library - Main Activity (Benchmark App)
 *
 * Module:      MainActivity.java
 * Description: This is the main activity for the benchmark app
 *
 * @author Dennis Mantz
 *
 * Copyright (C) 2014 Dennis Mantz
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

public class MainActivity extends Activity implements Benchmark.BenchmarkCallback {
	private Button bt_start;
	private Button bt_stop;
	private Button bt_submit;
	private TextView tv_output;
	private Benchmark benchmark;
	private File logfile;
	private Process logcat;
	private String version = "unknown";
	private String manufacturer = "unknown";
	private String model = "unknown";
	private String uniqueID = "unknown";
	private String apiLevel = "unknown";
	private static final String LOGTAG = "ANDROID_DSP_BENCHMARK";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Start logging:
		try{
			logfile = new File(Environment.getExternalStorageDirectory(), "android_dsp_lib_benchmark_log.txt");
			logcat = Runtime.getRuntime().exec("logcat -f " + logfile.getAbsolutePath());
			Log.i(LOGTAG, "onCreate: log path: " + logfile.getAbsolutePath());
		} catch (Exception e) {
			Log.e(LOGTAG, "onCreate: Failed to start logging!");
		}

		bt_start = (Button) findViewById(R.id.bt_start);
		bt_stop = (Button) findViewById(R.id.bt_stop);
		bt_submit = (Button) findViewById(R.id.bt_submit);
		tv_output = (TextView) findViewById(R.id.tv_output);
		bt_start.setEnabled(true);
		bt_stop.setEnabled(false);
		bt_submit.setEnabled(false);
		tv_output.setMovementMethod(new ScrollingMovementMethod());	// make it scroll!

		// Get reference to the shared preferences to get unique id:
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		uniqueID = preferences.getString("UNIQUE-ID", "unknown");

		// if no unique id could be find, generate it:
		if(uniqueID.equals("unknown")) {
			uniqueID = new BigInteger(130, new SecureRandom()).toString(32);
			SharedPreferences.Editor edit = preferences.edit();
			edit.putString("UNIQUE-ID", uniqueID);
			edit.apply();
		}

		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		manufacturer = Build.MANUFACTURER;
		model = Build.MODEL;
		apiLevel = "" + android.os.Build.VERSION.SDK_INT;
		tv_output.setText("Benchmark for the Android DSP library version " + version +
				" by Dennis Mantz.\nDevice Manufacturer: " + manufacturer + "\n" +
				"Device Model: " + model + "\nAPI-Level: " + apiLevel + "\n\n");
		Log.i(LOGTAG, tv_output.getText().toString());
	}

	@Override
	protected void onDestroy() {
		if(logcat != null)
			logcat.destroy();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_showLog) {
			Uri uri = Uri.fromFile(logfile);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(uri, "text/plain");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
			return true;
		}

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		onBtStopClick(null);
	}

	public void onBtStartClick(View view) {
		benchmark = new Benchmark(this, this);
		bt_start.setEnabled(false);
		bt_stop.setEnabled(true);
		bt_submit.setEnabled(false);
		benchmark.startBenchmark();

		// Prevent the screen from turning off:
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void onBtStopClick(View view) {
		bt_start.setEnabled(true);
		bt_stop.setEnabled(false);
		bt_submit.setEnabled(false);
		if(benchmark != null)
			benchmark.stopBenchmark();

		// allow screen to turn off again:
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		});
	}

	public void onBtSubmitClick(View view) {
		String filename = "1_" + manufacturer + "_" + model + "_" + apiLevel + "_" + uniqueID + ".csv";
		File csvFile = new File(Environment.getExternalStorageDirectory(), filename);
		String csvValues = "1, " + manufacturer + ", " + model + ", " + apiLevel + ", " + uniqueID + ", " + benchmark.getCsvValues();

		// Create csv file:
		try {
			FileWriter fileWriter = new FileWriter(csvFile);
			fileWriter.write(csvValues);
			fileWriter.close();
		} catch (IOException e) {
			outputErr(e.getMessage());
			e.printStackTrace();
		}

		// Invoke email app:
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "dennis.mantz@googlemail.com", null));
		intent.putExtra(Intent.EXTRA_SUBJECT, "ANDROID DSP LIB BENCHMARK V1");
		intent.putExtra(Intent.EXTRA_TEXT, tv_output.getText().toString());
		File root = Environment.getExternalStorageDirectory();
		Uri uri = Uri.fromFile(csvFile);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intent, "Send email (careful: not all clients will correctly attach the csv file: "+csvFile.getAbsolutePath()+") ..."));
	}

	@Override
	public void print(final String msg) {
		Log.i(LOGTAG, msg);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_output.append(msg);
			}
		});
	}

	@Override
	public void println(final String msg) {
		Log.i(LOGTAG, msg);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_output.append(msg + "\n");
			}
		});
	}

	@Override
	public void outputErr(final String msg) {
		Log.e(LOGTAG, msg);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_output.append("ERROR: " + msg + "\n");
			}
		});
	}

	@Override
	public void onFinish(final boolean success) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bt_start.setEnabled(true);
				bt_stop.setEnabled(false);
				bt_submit.setEnabled(success);

				// allow screen to turn off again:
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		});
	}
}
