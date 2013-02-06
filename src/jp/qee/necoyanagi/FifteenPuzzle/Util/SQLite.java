package jp.qee.necoyanagi.FifteenPuzzle.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite extends SQLiteOpenHelper {

    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "puzzle.db";

	public SQLite(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		// 成績
		db.execSQL("CREATE TABLE IF NOT EXISTS result"
                + " (id integer primary key,"
				+ " move_count integer,"
				+ " elapsed_time text,"
				+ " stage int,"
                + " update_time text);");

        // アプリ設定
		db.execSQL("CREATE TABLE IF NOT EXISTS config"
                + " (id integer primary key,"
				+ " code text,"
				+ " value text,"
                + " update_time text);");

		// 状態
		db.execSQL("CREATE TABLE IF NOT EXISTS status"
                + " (id integer primary key,"
				+ " code text,"
				+ " value text,"
                + " update_time text);");


		// 初期値


		// 画像種別
		Cursor cursor = db.query("config", new String[] {"value"}, "code=?", new String[] {"image_type"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "image_type");
			values.put("value", 1);
			values.put("update_time", System.currentTimeMillis());
			db.insert("config", null, values);
		}

		// グリッド数
		cursor = db.query("config", new String[] {"value"}, "code=?", new String[] {"grid_count"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "grid_count");
			values.put("value", 4);
			values.put("update_time", System.currentTimeMillis());
			db.insert("config", null, values);
		}

		// クリアステージ
		cursor = db.query("status", new String[] {"value"}, "code=?", new String[] {"stage"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "stage");
			values.put("value", 0);
			values.put("update_time", System.currentTimeMillis());
			db.insert("status", null, values);
		}

		// クリアフラグ
		cursor = db.query("status", new String[] {"value"}, "code=?", new String[] {"clear"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "clear");
			values.put("value", 0);
			values.put("update_time", System.currentTimeMillis());
			db.insert("status", null, values);
		}

		// 現在のステージ
		cursor = db.query("status", new String[] {"value"}, "code=?", new String[] {"current"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "current");
			values.put("value", 1);
			values.put("update_time", System.currentTimeMillis());
			db.insert("status", null, values);
		}

		cursor.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

		//定数の DB_VERSION すると、ここに来るのでアップデート処理
	}

}
