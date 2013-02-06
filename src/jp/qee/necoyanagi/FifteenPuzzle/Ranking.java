package jp.qee.necoyanagi.FifteenPuzzle;

import java.util.ArrayList;
import java.util.List;

import jp.qee.necoyanagi.FifteenPuzzle.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import jp.qee.necoyanagi.FifteenPuzzle.Util.SQLite;

public class Ranking extends Activity {

	SQLite sqliteHelper;
	SQLiteDatabase puzzleDB;

	private ListView list;

    /** Called when the activity is first created. */

	/**
	 * コンストラクタ
	 *
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 画面の初期化
        requestWindowFeature(Window.FEATURE_NO_TITLE); // アプリ名非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // 画面レイアウト
		setContentView(R.layout.ranking);
		list = (ListView)this.findViewById(R.id.list);


		// ボタンイベント定義
		ImageButton button = (ImageButton) findViewById(R.id.backButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ボタンがクリックされた時に呼び出されます
            	finish();
            }
        });



        // DB接続
		sqliteHelper = new SQLite(getApplicationContext());
		puzzleDB = sqliteHelper.getWritableDatabase();
		Cursor cursor = puzzleDB.query("result", new String[] { "move_count", "stage", "update_time" }, null, null, null, null, "move_count", "10");


		// リスト表示
		List<ListItem> items = initList();

		if (cursor.moveToFirst()) {
			for (int row=1; row<=cursor.getCount(); row++) {
				ListItem item = items.get(row);

				item.setCount(String.valueOf(cursor.getInt(0)));
				item.setStage(String.valueOf(cursor.getInt(1)));
				item.setDate(cursor.getString(2));

				items.set(row, item);
				cursor.moveToNext();
			}
		}

		ListAdapter adapter = new ListAdapter(getApplicationContext(), items);
		list.setAdapter(adapter);

		cursor.close();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();

    	puzzleDB.close();
    }

    /**
     * リスト初期化
     *
     * @return List<ListItem>
     */
    private List<ListItem> initList() {

		List<ListItem> items = new ArrayList<ListItem>();

		for (int i=0; i<11; i++) {
			items.add(new ListItem());

			if (i == 0) {
				ListItem header = new ListItem();

				header.setRank("順位");
				header.setCount("手数");
				header.setStage("ステージ");
				header.setDate("プレイ時刻");

				items.set(0, header);
			} else {
				ListItem item = new ListItem();

				item.setRank(String.valueOf(i));
				item.setCount("");
				item.setStage("");
				item.setDate("");

				items.set(i, item);
			}
		}

		return items;

    }

    /**
     * メニュー表示
     *
     */
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	//メニューインフレーターを取得
    	MenuInflater inflater = getMenuInflater();
    	//xmlのリソースファイルを使用してメニューにアイテムを追加
    	inflater.inflate(R.menu.ranking, menu);
    	//できたらtrueを返す
    	return true;
    }

    /**
     * メニュークリック
     *
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

	        case R.id.delete: // 記録削除
	    		new AlertDialog.Builder(this).
				setPositiveButton("はい", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						puzzleDB.delete("result", null, null);

						List<ListItem> items = initList();
						ListAdapter adapter = new ListAdapter(getApplicationContext(), items);
						list.setAdapter(adapter);
					}
				}).
				setNegativeButton("いいえ", null).
				setMessage("ゲームの成績をクリアしますか？ (※一度クリアしたデータは元に戻せません)").show();
	            return true;

	        default:
	            break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * リスト表示用アダプタクラス
     *
     * @author ishida
     *
     */
	class ListAdapter extends ArrayAdapter<ListItem>{

		private LayoutInflater mInflater;
		private TextView mRank;
		private TextView mCount;
		private TextView mStage;
		private TextView mDate;

		/**
		 * コンストラクタ
		 *
		 * @param context
		 * @param objects
		 */
		public ListAdapter(Context context, List<ListItem> objects) {
			super(context, 0, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list, null);
			}
			final ListItem item = this.getItem(position);
			if(item != null){
				mRank = (TextView)convertView.findViewById(R.id.rank);
				mRank.setText(item.getRank());

				mCount = (TextView)convertView.findViewById(R.id.count);
				mCount.setText(item.getCount());

				mStage = (TextView)convertView.findViewById(R.id.stage);
				mStage.setText(item.getStage());

				mDate = (TextView)convertView.findViewById(R.id.date);
				mDate.setText(item.getDate());
			}
			return convertView;

		}
	}


	/**
	 * リスト用Bean
	 *
	 * @author ishida
	 *
	 */
	public class ListItem {
		private String rank = "";
		private String count = "";
		private String stage = "";
		private String date = "";

		/**
		 *
		 * @param value
		 */
		public void setRank(String value) {
			this.rank = value;
		}

		/**
		 *
		 * @return
		 */
		public String getRank() {
			return this.rank;
		}

		/**
		 *
		 * @param value
		 */
		public void setCount(String value) {
			this.count = value;
		}

		/**
		 *
		 * @return
		 */
		public String getCount() {
			return this.count;
		}

		/**
		 *
		 * @param stage
		 */
		public void setStage(String stage) {
			this.stage = stage;
		}

		/**
		 *
		 * @return
		 */
		public String getStage() {
			return stage;
		}

		/**
		 *
		 * @param value
		 */
		public void setDate(String value) {
			this.date = value;
		}

		/**
		 *
		 * @return
		 */
		public String getDate() {
			return this.date;
		}
	}
}
