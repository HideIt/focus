package reknew.focus.Util;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UseageDBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;

	private static final String CREATE_TABLE_USEAGE = "create table useage ( id integer primary key autoincrement, " +
			"date text, screenOn text, screenOff text )";

	//TODO
	//全部保存 数据太多
	//更改：只保留五天的记录
	public UseageDBHelper() {
		super(Utils.getContext(), "useage.db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_USEAGE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}