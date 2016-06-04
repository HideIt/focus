package reknew.focus.Util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import reknew.focus.MyApplication;
import reknew.focus.R;

public class Utils {

	public static final String ACTION_START_LOCK = "reknew.focus.START_LOCK_SERVICE";
	public static final String ACTION_STOP_LOCK = "reknew.focus.STOP_LOCK_SERVICE";

	private static Context context = MyApplication.getContext();

	public static Context getContext() {
		return context;
	}

	public static void v(Object obj, Object... tag) {
		if (tag.length != 0) {
			Log.v("" + String.valueOf(tag[0]), "" + String.valueOf(obj));
		} else {
			Log.v("MINE", "" + String.valueOf(obj));
		}
	}

	public static void d(Object obj, Object... tag) {
		if (tag.length != 0) {
			Log.d("" + String.valueOf(tag[0]), "" + String.valueOf(obj));
		} else {
			Log.d("MINE", "" + String.valueOf(obj));
		}
		saveLog(obj, tag);
	}

	public static void e(Object obj, Object... tag) {
		if (tag.length != 0) {
			Log.e("" + String.valueOf(tag[0]), "" + String.valueOf(obj));
		} else {
			Log.e("MINE", "" + String.valueOf(obj));
		}
		saveLog("**********ERROR**********");
		saveLog(obj, tag);
		saveLog("**********ERROR**********");
	}

	public static void startService(Class<?> cls, String... action) {
		Utils.d("startService(" + cls.getName() + ")   " + "action = " + Arrays.toString(action));
		Intent intent = new Intent(context, cls);
		if (action.length != 0) {
			intent.setAction(action[0]);
		}
		context.startService(intent);
	}

	public static void stopService(Class<?> cls) {
		Random random = new Random();
		int number = random.nextInt(10);
		Utils.d(number + " stopService(" + cls.getName() + ") -> ");
		if (isServiceRunning(cls.getName())) {
			Utils.d(number + " is onRunning");
			Intent intent = new Intent(context, cls);
			context.stopService(intent);
		} else {
			Utils.d(number + " isn't  onRunning");
		}
	}

	public static void save(String fileName, String content, int mode) {
		try {
			FileOutputStream out = context.openFileOutput(fileName, mode);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) {
			Utils.e("save(" + fileName + ") exception");
		}
	}

	public static String load(String fileName) {
		StringBuilder content = new StringBuilder();
		try {
			FileInputStream in = context.openFileInput(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
			reader.close();
		} catch (IOException e) {
			Utils.e("load(" + fileName + ") exception");
			return "0";
		}
		return content.toString();
	}

/*
	public static void saveTriggerTime(String content) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS", Locale.getDefault());
		String time = format.format(new Date());
		save("triggerTime", time + "　　" + content, Context.MODE_APPEND);
	}
*/

	public static void saveLog(Object obj, Object... tag) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS", Locale.getDefault());
		String time = format.format(new Date());
		if (tag.length != 0) {
			save("log", time + "    " + tag[0] + "    " + String.valueOf(obj), Context.MODE_APPEND);
		} else {
			save("log", time + "    " + String.valueOf(obj), Context.MODE_APPEND);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void saveIcon(Drawable drawable, String name) {
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		File dir = new File(context.getCacheDir() + "/icon");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(context.getCacheDir() + "/icon", name);//.getFilesDir()
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (IOException e) {
			Utils.e("Utils.saveIcon() exception");
		}
	}

	@SuppressWarnings("deprecation")
	public static Drawable loadIcon(String name) {
		Drawable drawable = context.getResources().getDrawable(R.mipmap.ic_launcher);
		try {
			String path = context.getCacheDir() + "/icon/" + name;
			FileInputStream in = new FileInputStream(path);
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			drawable = new BitmapDrawable(bitmap);
		} catch (FileNotFoundException e) {
			Utils.e("Utils.loadIcon() exception");
		}
		return drawable;
	}

	public static boolean isServiceRunning(String serviceName) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}