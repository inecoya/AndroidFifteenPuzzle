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

		// ����
		db.execSQL("CREATE TABLE IF NOT EXISTS result"
                + " (id integer primary key,"
				+ " move_count integer,"
				+ " elapsed_time text,"
				+ " stage int,"
                + " update_time text);");

        // �A�v���ݒ�
		db.execSQL("CREATE TABLE IF NOT EXISTS config"
                + " (id integer primary key,"
				+ " code text,"
				+ " value text,"
                + " update_time text);");

		// ���
		db.execSQL("CREATE TABLE IF NOT EXISTS status"
                + " (id integer primary key,"
				+ " code text,"
				+ " value text,"
                + " update_time text);");


		// �����l


		// �摜���
		Cursor cursor = db.query("config", new String[] {"value"}, "code=?", new String[] {"image_type"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "image_type");
			values.put("value", 1);
			values.put("update_time", System.currentTimeMillis());
			db.insert("config", null, values);
		}

		// �O���b�h��
		cursor = db.query("config", new String[] {"value"}, "code=?", new String[] {"grid_count"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "grid_count");
			values.put("value", 4);
			values.put("update_time", System.currentTimeMillis());
			db.insert("config", null, values);
		}

		// �N���A�X�e�[�W
		cursor = db.query("status", new String[] {"value"}, "code=?", new String[] {"stage"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "stage");
			values.put("value", 0);
			values.put("update_time", System.currentTimeMillis());
			db.insert("status", null, values);
		}

		// �N���A�t���O
		cursor = db.query("status", new String[] {"value"}, "code=?", new String[] {"clear"}, null, null, null);
		if (cursor.getCount() == 0) {
			ContentValues values = new ContentValues();
			values.put("code", "clear");
			values.put("value", 0);
			values.put("update_time", System.currentTimeMillis());
			db.insert("status", null, values);
		}

		// ���݂̃X�e�[�W
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

		//�萔�� DB_VERSION ����ƁA�����ɗ���̂ŃA�b�v�f�[�g����
	}

}
