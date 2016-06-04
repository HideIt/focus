package reknew.focus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.widget.ListView;

import reknew.focus.Util.ActivityCollector;
import reknew.focus.Util.AppInfo;
import reknew.focus.Util.ListViewAdapter;
import reknew.focus.Util.TableDBHelper;

public class AppListActivity extends Activity {

	private ListViewAdapter adapter;
	private SQLiteDatabase db;
	private ListView listView;

	Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActivityCollector.addActivity(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list);
		init();
	}

	@Override
	protected void onDestroy() {
		ActivityCollector.removeActivity(this);
		db.close();
		super.onDestroy();
	}

	private void init() {
		TableDBHelper dbHelper = new TableDBHelper();
		db = dbHelper.getWritableDatabase();

		adapter = new ListViewAdapter(AppInfo.getFromDatabase(), this);

		listView = (ListView) findViewById(R.id.app_list_view);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的CheckBox实例的步骤
			ListViewAdapter.ViewHolder holder = (ListViewAdapter.ViewHolder) view.getTag();
			// 改变CheckBox的状态
			holder.checkBox.toggle();
			// 将CheckBox的选中状况记录下来
			adapter.getSelectedList().set(position, holder.checkBox.isChecked());
			ContentValues values = new ContentValues();
			values.put("run", holder.checkBox.isChecked() ? 1 : 0);
			db.update("application", values, "id = ?", new String[]{String.valueOf(position + 1)});
		});

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(v -> {
			ProgressDialog progress = new ProgressDialog(this);
			progress.setCancelable(false);
			progress.setMessage("Be patient...");
			progress.show();
			new Thread(() -> {
				AppInfo appInfo = AppInfo.getFromPackageManager();
				adapter = new ListViewAdapter(appInfo, AppListActivity.this);
				handler.post(() -> listView.setAdapter(adapter));
				progress.dismiss();
			}).start();
		});
	}
}