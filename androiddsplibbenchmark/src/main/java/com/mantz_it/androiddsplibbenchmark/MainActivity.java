package com.mantz_it.androiddsplibbenchmark;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements Benchmark.BenchmarkCallback {
	private Button bt_start;
	private Button bt_stop;
	private Button bt_submit;
	private TextView tv_output;
	private Benchmark benchmark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_start = (Button) findViewById(R.id.bt_start);
		bt_stop = (Button) findViewById(R.id.bt_stop);
		bt_submit = (Button) findViewById(R.id.bt_submit);
		tv_output = (TextView) findViewById(R.id.tv_output);
		bt_start.setEnabled(true);
		bt_stop.setEnabled(false);
		bt_submit.setEnabled(false);
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

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onBtStartClick(View view) {
		benchmark = new Benchmark(this, this);
		bt_start.setEnabled(false);
		bt_stop.setEnabled(true);
		bt_submit.setEnabled(false);
		benchmark.startBenchmark();
	}

	public void onBtStopClick(View view) {
		bt_start.setEnabled(true);
		bt_stop.setEnabled(false);
		bt_submit.setEnabled(false);
		benchmark.stopBenchmark();
	}

	public void onBtSubmitClick(View view) {
		//todo
	}

	@Override
	public void print(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_output.append(msg);
			}
		});
	}

	@Override
	public void println(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_output.append(msg + "\n");
			}
		});
	}

	@Override
	public void outputErr(final String msg) {
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
			}
		});
	}
}
