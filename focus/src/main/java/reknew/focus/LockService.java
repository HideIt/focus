package reknew.focus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reknew.focus.Util.AppInfo;
import reknew.focus.Util.BetterToast;
import reknew.focus.Util.Utils;

public class LockService extends Service implements OnClickListener {

	private final int MAX_CLICK_TIMES = 99;

	private static LockService instance = null;

	private static AppInfo appInfo = null;

	private WindowManager manager;
	private WindowManager.LayoutParams params;
	private View view;

	private AlertDialog appListDialog;

	private int counts = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("InflateParams")
	@Override
	public void onCreate() {
		Utils.d("LockService created");
		super.onCreate();

		instance = this;

		appInfo = AppInfo.getWhiteList();
		appInfo.packageList.add("com.android.incallui");
		appInfo.packageList.add("android");

		manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		params = new WindowManager.LayoutParams();
		params.type = LayoutParams.TYPE_SYSTEM_ALERT;
		params.flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM | LayoutParams.FLAG_NOT_FOCUSABLE;
		params.format = PixelFormat.TRANSPARENT;
		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.CENTER;

		view = LayoutInflater.from(Utils.getContext()).inflate(R.layout.float_view, null);
		view.findViewById(R.id.float_view_text).setOnClickListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils.d("LockService started");
		if (intent.getAction().equals("reknew.focus.START_LOCK_SERVICE")) {
			Utils.d("********** float view START", "float_view");
			startFloatView();
			startForeground(startId, new Notification());
		} else {
			stopSelf();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Utils.d("LockService destroyed");
		Utils.d("********** float view STOP", "float_view");
		stopFloatView();
		stopForeground(true);
		instance = null;
		appInfo = null;
		super.onDestroy();
	}

	private void startFloatView() {
		if (view.getDisplay() == null) {
			Utils.d("addView()");
			manager.addView(view, params);
		}
	}

	private void stopFloatView() {
		if (view.getDisplay() != null) {
			Utils.d("removeView()");
			manager.removeView(view);
		}
	}

	//TODO
	//后期不再使用System AlertDialog，因为这会将reknew.focus变成topActivity 可能导致应用被杀
	//可以通过更改view的方法显示应用列表以及解锁按钮
	//另: 性能问题
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		List<Map<String, Object>> gridList = new ArrayList<>();
		for (int i = 0; i < appInfo.nameList.size(); i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("image", appInfo.imageList.get(i));
			map.put("text", appInfo.nameList.get(i));
			gridList.add(map);
		}
		String[] from = new String[]{"image", "text"};
		int[] to = new int[]{R.id.item_image, R.id.item_text};

		SimpleAdapter adapter = new SimpleAdapter(this, gridList, R.layout.grid_view_item, from, to);
		//TODO how ? & why?
		adapter.setViewBinder((view, data, textRepresentation) -> {
			if (view instanceof ImageView && data instanceof Drawable) {
				((ImageView) view).setImageDrawable((Drawable) data);
				return true;
			} else {
				return false;
			}
		});

		GridView gridView = new GridView(this);
		gridView.setAdapter(adapter);
		gridView.setNumColumns(4);
		gridView.setOnItemClickListener((parent, view, position, id) -> {
			appListDialog.dismiss();
			try {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(appInfo.packageList.get(position), appInfo.classList.get
						(position)));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} catch (Exception e) {
				Utils.e("start Activity from GridView exception");
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		builder.setView(gridView);
		builder.setPositiveButton("UNLOCK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (canUnlock()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(LockService.this, AlertDialog
							.THEME_DEVICE_DEFAULT_LIGHT);
					builder.setMessage("ARE YOU SURE YOU WANT UNLOCK THE PHONE ?");
					builder.setPositiveButton("unlock", (dialog1, which1) -> {
						stopSelf();
						//正常途径退出，则改写trigger防止意外启动
						Utils.save("trigger", "0", Context.MODE_PRIVATE);
					});
					Dialog ensureDialog = builder.create();
					ensureDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					ensureDialog.show();
				} else {
					Utils.d("Click another 0X" + Integer.toHexString(MAX_CLICK_TIMES - counts).toUpperCase() +
							" times, and you can unlock  : )");
					BetterToast.showToast("Click another 0X" + Integer.toHexString(MAX_CLICK_TIMES - counts)
							.toUpperCase() + " times, and you can unlock  : )");
				}
			}

			//TODO 每天只能解锁 _ 次
			private boolean canUnlock() {
				counts++;
				if (counts >= MAX_CLICK_TIMES) {
					counts = 0;
					return true;
				} else {
					return false;
				}
			}
		});
		appListDialog = builder.create();
		appListDialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ALERT);
		appListDialog.show();
		appListDialog.getWindow().setLayout(800, LayoutParams.WRAP_CONTENT);
	}

	public static void setTopPackage(String topPackage) {
		Utils.d("setTopPackage");
		if (instance != null && appInfo != null) {
			instance.monitor(topPackage);
		}
	}

	private void monitor(String topPackage) {
		if (appInfo.packageList.contains(topPackage)) {
			Utils.v("___yes___  " + topPackage, "top_package");
			stopFloatView();
		} else {
			Utils.v("___not___  : " + topPackage, "top_package");
			startFloatView();
		}
	}
}