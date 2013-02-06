package jp.qee.necoyanagi.FifteenPuzzle.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

public class Common {

	public static final String DIALOG_BUTTON_TYPE_CLOSE = "•Â‚¶‚é";
	public static final String DIALOG_BUTTON_TYPE_OK = "OK";
	public static final String DIALOG_BUTTON_TYPE_CANCEL = "CANCEL";
	public static final String DIALOG_BUTTON_TYPE_YES = "‚Í‚¢";
	public static final String DIALOG_BUTTON_TYPE_NO = "‚¢‚¢‚¦";

	public void openSimpleDialog(Context context, String message) {
		new AlertDialog.Builder(context).
			setNegativeButton("•Â‚¶‚é", null).
			setMessage(message).
			create().
			show();
	}

	public void openViewDialog(Context context, View view) {
		new AlertDialog.Builder(context).
			setNegativeButton("•Â‚¶‚é", null).
			setView(view).
			create().
			show();
	}

}
