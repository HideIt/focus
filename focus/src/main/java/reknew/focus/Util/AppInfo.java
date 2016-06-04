package reknew.focus.Util;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 存： 从1开始
 * 取： 从0开始
 * <p>
 * view的position以及map的key从0开始，sqlite的id从1开始!!!
 */
public class AppInfo {
	public List<String> nameList = new ArrayList<>();
	public List<String> packageList = new ArrayList<>();
	public List<String> classList = new ArrayList<>();
	public List<Drawable> imageList = new ArrayList<>();
	public List<Boolean> selectedList = new ArrayList<>();

	public static AppInfo getFromDatabase() {
		AppInfo appInfo = new AppInfo();
		TableDBHelper dbHelper = new TableDBHelper();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("application", null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex("id"));
			int run = cursor.getInt(cursor.getColumnIndex("run"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			Drawable icon = Utils.loadIcon(String.valueOf(id) + ".png");

			appInfo.nameList.add(name);
			appInfo.imageList.add(icon);
			appInfo.selectedList.add(run == 1);
		}
		cursor.close();
		db.close();
		if (appInfo.nameList.size() == 0) {
			BetterToast.showToast("Please refresh...");
		}
		Utils.d("appInfo.nameList.size() = " + appInfo.nameList.size());
		return appInfo;
	}

	public static AppInfo getFromPackageManager() {
		PackageManager manager = Utils.getContext().getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveList = manager.queryIntentActivities(intent, 0);
		Collections.sort(resolveList, new ResolveInfo.DisplayNameComparator(manager));//sort resolveList
		TableDBHelper dbHelper = new TableDBHelper();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("application", new String[]{"run", "package"}, "run = ?", new String[]{"1"}, null,
				null, null);
		List<String> whiteList = new ArrayList<>();
		while (cursor.moveToNext()) {
			String packageName = cursor.getString(cursor.getColumnIndex("package"));
			whiteList.add(packageName);
		}
		cursor.close();
		db.execSQL("delete from application");//清空数据
		db.execSQL("update sqlite_sequence SET seq = 0 where name ='application'");//自增长ID置0
		Utils.d("whiteList count: " + whiteList.size());

		AppInfo appInfo = new AppInfo();
		int skip = 0;
		for (int i = 0; i < resolveList.size(); i++) {
			ResolveInfo info = resolveList.get(i);
			Drawable icon = info.loadIcon(manager);
			String name = info.loadLabel(manager).toString();
			String packageName = info.activityInfo.packageName;
			String className = info.activityInfo.name;
			if (packageName.equals("reknew.focus") || packageName.equals("com.android.settings")) {
				skip++;
			} else {
				ContentValues values = new ContentValues();
				values.put("name", name);
				values.put("package", packageName);
				values.put("class", className);
				if (whiteList.contains(packageName)) {
					values.put("run", 1);
					appInfo.selectedList.add(true);
				} else {
					values.put("run", 0);
					appInfo.selectedList.add(false);
				}
				db.insert("application", null, values);

				Utils.saveIcon(icon, String.valueOf(i + 1 - skip) + ".png");
				appInfo.nameList.add(name);
				appInfo.imageList.add(icon);
				appInfo.packageList.add(packageName);
				appInfo.classList.add(className);
			}
		}
		db.close();
		Utils.d("appInfo.nameList.size() = " + appInfo.nameList.size());
		return appInfo;
	}

	public static AppInfo getWhiteList() {
		AppInfo appInfo = new AppInfo();
		TableDBHelper dbHelper = new TableDBHelper();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("application", null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex("run")) == 1) {
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				String name = cursor.getString(cursor.getColumnIndex("name"));
				String packageName = cursor.getString(cursor.getColumnIndex("package"));
				String className = cursor.getString(cursor.getColumnIndex("class"));
				Drawable icon = Utils.loadIcon(String.valueOf(id) + ".png");

				appInfo.nameList.add(name);
				appInfo.packageList.add(packageName);
				appInfo.classList.add(className);
				appInfo.imageList.add(icon);
			}
		}
		cursor.close();
		db.close();
		Utils.d("appInfo.nameList.size() = " + appInfo.nameList.size());
		return appInfo;
	}

	//TODO learn AsyncTask
	//后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型
	private class MyTask extends AsyncTask<Integer, Integer, AppInfo> {

		/* 第一个执行的方法
		 * 执行时机：在执行实际的后台操作前，被UI 线程调用
		 * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
		 */
		@Override
		protected void onPreExecute() {
			Utils.d("onPreExecute()");
			super.onPreExecute();
		}

		/* 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中
		 * 作用：主要负责执行那些很耗时的后台处理工作。可以调用 publishProgress 方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
		 */
		@Override
		protected AppInfo doInBackground(Integer... params) {
			publishProgress(0);
			Utils.d("doInBackground()");
			return AppInfo.getFromPackageManager();
		}

		/* 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI 线程将调用这个方法.
		 * 虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
		 * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		/* 执行时机：在doInBackground 执行完成后，将被UI 线程调用
		 * 作用：后台的计算结果将通过该方法传递到UI 线程，并且在界面上展示给用户
		 * result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
		 */
		@Override
		protected void onPostExecute(AppInfo appInfo) {
			super.onPostExecute(appInfo);
		}
	}
}