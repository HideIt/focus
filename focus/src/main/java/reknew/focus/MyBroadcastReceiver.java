package reknew.focus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import reknew.focus.Util.Utils;

public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Utils.d("Broatcast received");
		if (intent.getAction().equals(Utils.ACTION_STOP_LOCK)) {
			Utils.d("will stop LockService --- by MyBroadcastReceiver");
			Utils.stopService(LockService.class);
		} else {
			Utils.e("intent.getAction() = " + intent.getAction(), "IntentAction");
			Utils.d("will start GuardService --- by MyBroadcastReceiver");
			Utils.startService(GuardService.class);
		}
	}
}