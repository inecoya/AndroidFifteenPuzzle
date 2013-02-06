package jp.qee.necoyanagi.FifteenPuzzle;

import jp.qee.necoyanagi.FifteenPuzzle.Util.SQLite;

import jp.qee.necoyanagi.FifteenPuzzle.R;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Config extends Activity {

	// DB用オブジェクト
	SQLite sqliteHelper;
	SQLiteDatabase puzzleDB;

	// 変数
	private int imageType = 0;


	/**
	 * コンストラクタ
	 *
	 */
    @Override
    public void onCreate(Bundle context) {
        super.onCreate(context);

        // 画面の初期化
        requestWindowFeature(Window.FEATURE_NO_TITLE); // アプリ名非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // レイアウト
		setContentView(R.layout.config);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);


		// DB接続
		sqliteHelper = new SQLite(getApplicationContext());
		puzzleDB = sqliteHelper.getWritableDatabase();
		Cursor cursor = puzzleDB.query("config", new String[] { "value" }, "code=?", new String[] {"image_type"}, null, null, null);



		// ボタンイベント定義
		ImageButton button = (ImageButton) findViewById(R.id.backButton2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時に呼び出されます
            	finish();
            }
        });


        // ラジオボタン
        if (cursor.moveToFirst()) {
        	int resId = getResources().getIdentifier("radiobutton" + String.valueOf(cursor.getInt(0)), "id", getPackageName());
            radioGroup.check(resId);
        }

        // ラジオボタン チェンジイベント定義
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            	RadioButton radioButton = (RadioButton) findViewById(checkedId);
            	if (radioButton.getText().equals("アンドロイド")) {
                	imageType = 1;
            	} else {
            		imageType = 0;
            	}

            	// 設定を保存
    			ContentValues values = new ContentValues();
    			values.put("value", imageType);
                puzzleDB.update("config", values, "code=?", new String[] {"image_type"});
            }
        });


        cursor.close();

    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

    	puzzleDB.close();
    }

    /**
     * メニュー表示
     *
     */
    /*
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	//メニューインフレーターを取得
    	MenuInflater inflater = getMenuInflater();
    	//xmlのリソースファイルを使用してメニューにアイテムを追加
    	inflater.inflate(R.menu.config, menu);
    	//できたらtrueを返す
    	return true;
    }
    */

    /**
     * メニュークリック
     *
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.save: // 保存
    			ContentValues values = new ContentValues();
    			values.put("value", imageType);
                puzzleDB.update("config", values, "code=?", new String[] {"image_type"});
	            return true;
	        default:
	            break;
        }
        return super.onOptionsItemSelected(item);
    }

}
