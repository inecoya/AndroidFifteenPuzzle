package jp.qee.necoyanagi.FifteenPuzzle.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

public class Common {

	public static final String DIALOG_BUTTON_TYPE_CLOSE = "閉じる";
	public static final String DIALOG_BUTTON_TYPE_OK = "OK";
	public static final String DIALOG_BUTTON_TYPE_CANCEL = "CANCEL";
	public static final String DIALOG_BUTTON_TYPE_YES = "はい";
	public static final String DIALOG_BUTTON_TYPE_NO = "いいえ";

	public void openSimpleDialog(Context context, String message) {
		new AlertDialog.Builder(context).
			setNegativeButton("閉じる", null).
			setMessage(message).
			create().
			show();
	}

	public void openViewDialog(Context context, View view) {
		new AlertDialog.Builder(context).
			setNegativeButton("閉じる", null).
			setView(view).
			create().
			show();
	}

}
