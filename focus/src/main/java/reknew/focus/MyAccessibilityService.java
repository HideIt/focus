package reknew.focus;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils.SimpleStringSplitter;
import android.view.accessibility.AccessibilityEvent;

import reknew.focus.Util.BetterToast;
import reknew.focus.Util.Utils;

public class MyAccessibilityService extends AccessibilityService {


	private static boolean showToast = false;

	public static void setShowToast(boolean showToast) {
		MyAccessibilityService.showToast = showToast;
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			String packageName = event.getPackageName().toString();
			LockService.setTopPackage(packageName);
			if (showToast) {
				String className = event.getClassName().toString();
				BetterToast.showToast(packageName + "\n" + className);
			}
		}
	}

	//检查辅助服务是否开启
	public static boolean isAccessibilityEnabled() {
		int enabled = 0;
		ContentResolver resolver = Utils.getContext().getContentResolver();
		try {
			enabled = Secure.getInt(resolver, Secure.ACCESSIBILITY_ENABLED);
		} catch (Settings.SettingNotFoundException e) {
			Utils.e("Secure.getInt() exception");
		}
		if (enabled == 1) {
			String settingValue = Secure.getString(resolver, Secure.ENABLED_ACCESSIBILITY_SERVICES);
			SimpleStringSplitter splitter = new SimpleStringSplitter(':');
			splitter.setString(settingValue);
			String serviceName = Utils.getContext().getPackageName() + "/" + MyAccessibilityService.class.getName();
			while (splitter.hasNext()) {
				String service = splitter.next();
				if (service.equalsIgnoreCase(serviceName)) {
					Utils.d("Accessibility enabled");
					return true;
				}
			}
		}
		return false;
	}
/*
	public static boolean isAccessibilityEnabled(String id) {
		AccessibilityManager manager = (AccessibilityManager) Utils.getContext().getSystemService(Context
				.ACCESSIBILITY_SERVICE);
		List<AccessibilityServiceInfo> accessibilityList = manager.getEnabledAccessibilityServiceList(AccessibilityEvent
				.TYPES_ALL_MASK);
		for (AccessibilityServiceInfo service : accessibilityList) {
			if (id.equals(service.getId())) {
				return true;
			}
		}
		return false;
	}
*/
	@Override
	public void onServiceConnected() {
		Utils.d("onServiceConnected()", "MyAccessibilityService");
		Utils.startService(GuardService.class);
	}

	@Override
	public void onInterrupt() {
		Utils.d("onInterrupt()", "MyAccessibilityService");
		Utils.startService(GuardService.class);
	}
}