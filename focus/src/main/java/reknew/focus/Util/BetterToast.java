package reknew.focus.Util;

import android.widget.Toast;

public class BetterToast {

	private static Toast toast = null;

	public static void showToast(CharSequence text, boolean... showLongTime) {
		int duration = Toast.LENGTH_LONG;
		if (showLongTime.length != 0 && !showLongTime[0]) {
			duration = Toast.LENGTH_SHORT;
		}
		if (toast == null) {
			toast = Toast.makeText(Utils.getContext(), text, duration);
		} else {
			toast.setText(text);
			toast.setDuration(duration);
		}
		toast.show();
	}
}