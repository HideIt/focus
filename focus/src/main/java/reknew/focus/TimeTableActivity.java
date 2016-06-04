package reknew.focus;

import android.app.Activity;
import android.os.Bundle;

import reknew.focus.Util.ActivityCollector;

public class TimeTableActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityCollector.addActivity(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_time_table);
	}

	@Override
	protected void onDestroy() {
		ActivityCollector.removeActivity(this);
		super.onDestroy();
	}
}