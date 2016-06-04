package reknew.focus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import reknew.focus.Util.ActivityCollector;
import reknew.focus.Util.AppInfo;
import reknew.focus.Util.BetterToast;
import reknew.focus.Util.Utils;


public class LaunchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
		Formatter, OnValueChangeListener, OnScrollListener, OnClickListener {

	private int hour = 0;
	private int minute = 0;
	private NumberPicker hourPicker;
	private NumberPicker minutePicker;

	private boolean showToast = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityCollector.addActivity(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		Utils.startService(GuardService.class);
		init();
	}

	private void init() {
		new Thread(() -> {
			if (Utils.isServiceRunning("reknew.focus.LockService")) {
				finish();
			}
		}).start();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		//noinspection ConstantConditions
		fab.setOnClickListener(this);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
				.navigation_drawer_open, R.string.navigation_drawer_close);
		//noinspection ConstantConditions
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		//noinspection ConstantConditions
		navigationView.setNavigationItemSelectedListener(this);

		hourPicker = (NumberPicker) findViewById(R.id.hour_picker);
		//noinspection ConstantConditions
		hourPicker.setFormatter(this);
		hourPicker.setOnValueChangedListener(this);
		hourPicker.setOnScrollListener(this);
		hourPicker.setMaxValue(24);
		hourPicker.setMinValue(0);
		hourPicker.setValue(0);

		minutePicker = (NumberPicker) findViewById(R.id.minute_picker);
		//noinspection ConstantConditions
		minutePicker.setFormatter(this);
		minutePicker.setOnValueChangedListener(this);
		minutePicker.setOnScrollListener(this);
		//String[] times = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};
		//minutePicker.setDisplayedValues(times);
		//minutePicker.setMaxValue(times.length - 1);
		minutePicker.setMaxValue(59);
		minutePicker.setMinValue(0);
		minutePicker.setValue(0);

		TextView text = (TextView) findViewById(R.id.spec_text);
		//noinspection ConstantConditions
		text.setOnClickListener(this);
		Button showToast = (Button) findViewById(R.id.showToast);
		//noinspection ConstantConditions
		showToast.setOnClickListener(this);
		Button getWhiteList = (Button) findViewById(R.id.get_white_list);
		//noinspection ConstantConditions
		getWhiteList.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("InlinedApi")
	@Override
	protected void onStart() {
		super.onStart();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!Settings.canDrawOverlays(Utils.getContext())) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
				builder.setTitle("Caution");
				builder.setMessage("Please permit drawing over other apps, or this application cannot work.");
				builder.setCancelable(false);
//				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//						intent.setData(Uri.parse("package:reknew.focus"));
//						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					}
//				});
				builder.setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(Settings
						.ACTION_MANAGE_OVERLAY_PERMISSION).setData(Uri.parse("package:reknew.focus")).setFlags(Intent
						.FLAG_ACTIVITY_NEW_TASK)));
				builder.setNegativeButton("Cancel", (dialog, which) -> ActivityCollector.finishAll());
				builder.show();
			}
		}
		if (!MyAccessibilityService.isAccessibilityEnabled()) {
			//noinspection ConstantConditions
			Snackbar.make(findViewById(R.id.launch_content), "Please Enable Accessibility", Snackbar.LENGTH_LONG)
					.setAction("GOTO", v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
					.setCallback(new Snackbar.Callback() {
				//TODO SnackBar自行消失时 fab不回弹
				@Override
				public void onDismissed(Snackbar snackbar, int event) {
					super.onDismissed(snackbar, Snackbar.Callback.DISMISS_EVENT_SWIPE);
				}
			}).show();
		}
	}

	@Override
	protected void onDestroy() {
		ActivityCollector.removeActivity(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.spec_text:
				Snackbar.make(v, "Version: 0.0.2 alpha", Snackbar.LENGTH_SHORT).show();
				break;
			case R.id.fab:
				if ((hour + minute) != 0) {
					long trigger = System.currentTimeMillis() + (hour * 60 + minute) * 60 * 1000;
					Utils.save("trigger", String.valueOf(trigger), Context.MODE_PRIVATE);
					Utils.startService(GuardService.class);
					ActivityCollector.finishAll();
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					startActivity(intent);
				} else {
					BetterToast.showToast("Please Set Lock Time");
				}
				break;
			case R.id.showToast:
				showToast = !showToast;
				MyAccessibilityService.setShowToast(showToast);
				break;
			case R.id.get_white_list:
				long start = System.currentTimeMillis();
				AppInfo appInfo = AppInfo.getWhiteList();
				StringBuilder builder = new StringBuilder();
				for (String str : appInfo.nameList) {
					builder.append(str).append("\n");
				}
				long stop = System.currentTimeMillis();
				BetterToast.showToast(builder + "use time: " + (stop - start));
				break;
		}

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		//noinspection ConstantConditions
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.launch, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				BetterToast.showToast("Clicked settings");
				break;
		}
		return true;
		//return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		switch (item.getItemId()) {
			case R.id.nav_white_list:
				startActivity(new Intent(LaunchActivity.this, AppListActivity.class));
				break;
			case R.id.nav_time_table:
				startActivity(new Intent(LaunchActivity.this, TimeTableActivity.class));
				break;
		}
		return true;
	}

	@Override
	public String format(int value) {
		String str = String.valueOf(value);
		str = value < 10 ? "0" + str : str;
		return str;
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		if (picker == hourPicker) {
			hour = newVal;
		} else if (picker == minutePicker) {
			//minute = newVal * 5;
			minute = newVal;
		}
	}

	@Override
	public void onScrollStateChange(NumberPicker view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:
				//...
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				//...
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				//...
				break;
		}
	}
}