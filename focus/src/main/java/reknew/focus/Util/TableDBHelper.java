package reknew.focus.Util;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TableDBHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;

	private static final String CREATE_TABLE_APPLICATION =
			"create table application ("
			+ "id integer primary key autoincrement, "
			+ "name text, "
			+ "run integer, "
			+ "package text, "
			+ "class text )";

	private static final String CREATE_TABLE_LOCKTIME =
			"create table locktime ("
			+ "id integer primary key autoincrement,"
			+ "start integer, "
			+ "stop integer, "
			+ "inUse integer, "
			+ "day1 integer, "
			+ "day2 integer, "
			+ "day3 integer, "
			+ "day4 integer, "
			+ "day5 integer, "
			+ "day6 integer, "
			+ "day7 integer )";

	public TableDBHelper() {
		super(Utils.getContext(), "table.db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_APPLICATION);
		db.execSQL(CREATE_TABLE_LOCKTIME);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}