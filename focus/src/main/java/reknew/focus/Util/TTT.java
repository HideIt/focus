package reknew.focus.Util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class TTT {
	public static void set(Context context, String key, String value) {
		/*****************************************/
		switch(0) {
			case 1001:
				JSONObject jsoObj;
				String date = null;
				boolean isclose = false;
				try {
					jsoObj = new JSONObject("");
					date =jsoObj.getString("data");
					isclose = jsoObj.getBoolean("isclose");
				} catch(JSONException e) {
					e.printStackTrace();
				}
				TTT.set(null, "", date);
				break;
		}
		/*****************************************/
	}
}