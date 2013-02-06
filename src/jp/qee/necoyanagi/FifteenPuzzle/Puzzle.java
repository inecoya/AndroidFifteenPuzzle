package jp.qee.necoyanagi.FifteenPuzzle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.qee.necoyanagi.FifteenPuzzle.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import jp.qee.necoyanagi.FifteenPuzzle.Util.SQLite;

public class Puzzle extends Activity {

	// スレッド
	private Handler mHandler = new Handler();
	private Runnable mUpdateImage;
	private int time = 0;

	// ビュー
	private MainView MainView;
	private FrameLayout frameLayout;

	// 変数
	private boolean viewHint = false;
	private int imageType = 0;
	private float scale;

	// DB用オブジェクト
	SQLite sqliteHelper;
	SQLiteDatabase puzzleDB;
	Cursor markerCursor;


    /** Called when the activity is first created. */
	/**
	 * コンストラクタ
	 *
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 画面の初期化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // スリープさせない
        requestWindowFeature(Window.FEATURE_NO_TITLE); // アプリ名非表示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        scale = getResources().getDisplayMetrics().density;


        // DB接続
		sqliteHelper = new SQLite(getApplicationContext());
		puzzleDB = sqliteHelper.getWritableDatabase();


		// デフォルト値取得
		Cursor cursor = puzzleDB.query("config", new String[] {"value"}, "code=?", new String[] {"image_type"}, null, null, null);
		if (cursor.moveToFirst()) {
			imageType = Integer.parseInt(cursor.getString(0));
		}


        // ビュー
		frameLayout = new FrameLayout(this.getApplicationContext());

		// パズル
		MainView = new MainView(this, imageType);

		// 広告
		View adView = this.getLayoutInflater().inflate(R.layout.main, null);


		/*
		libAdMaker AdMaker;// = new libAdMaker(this);
		AdMaker = (libAdMaker)adView.findViewById(R.id.admakerview);
		if (AdMaker != null) {
			AdMaker.setActivity(this);
			AdMaker.siteId = "855";
			AdMaker.zoneId = "2532";
			AdMaker.setUrl("http://images.ad-maker.info/apps/liklkae6q8dq.html");
			AdMaker.start();
		}
		*/



		frameLayout.addView(adView);
        frameLayout.addView(MainView);
        setContentView(frameLayout);




        // タイマースレッド
    	mUpdateImage = new Runnable() {
    		public void run() {
    			time++;
    			mHandler.removeCallbacks(mUpdateImage);
    			mHandler.postDelayed(mUpdateImage, 1000);
    		}
    	};
    	//mHandler.postDelayed(mUpdateImage, 100);

    }

	@Override
    public void onDestroy() {
    	super.onDestroy();

    	//mHandler = null;
    	puzzleDB.close();
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
    	inflater.inflate(R.menu.main, menu);
    	//できたらtrueを返す
    	return true;
    }

    /**
     * メニュークリック
     *
     */
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

	        case R.id.reset: // ゲーム初期化
	        	MainView.InitGame();
	        	break;

	        case R.id.debug: // デバッグ
	        	MainView.Debug();
	        	break;

	        case R.id.ranking: // ランキング
	            Intent ranking = new Intent(this, Ranking.class);
	            startActivity(ranking);
	            break;

	        case R.id.gallery: // ギャラリー
	            Intent gallery = new Intent(this, ImageGallery.class);
	            gallery.putExtra("RES_IDS", MainView.getImageResourceIds()); // 対象画像のリソースIDを渡す
	            startActivity(gallery);
	        	break;

	        case R.id.config: // 設定
	            Intent config = new Intent(this, Config.class);
	            startActivity(config);
	            break;

	        case R.id.hint: // ヒント
	        	viewHint = true;
	        	MainView.invalidate();
	        	break;

	        case R.id.preview: // プレビュー
	        	if (imageType == 1) {
		        	ImageView view = new ImageView(this);
		        	view.setImageBitmap(MainView.getOrgImage());

	        		new AlertDialog.Builder(this).
	        		setNegativeButton("閉じる", null).
	        		setView(view).
	        		create().
	        		show();
	        	}
	        	break;

	        case R.id.help: // ヘルプ
	        	WebView webView = new WebView(this);
	        	webView.loadDataWithBaseURL("file:///android_asset/", getTemplateHtml(), "text/html", "utf-8", null);

        		new AlertDialog.Builder(this).
        		setNegativeButton("閉じる", null).
        		setView(webView).
        		create().
        		show();
	        	break;

	        default:
	            break;

        }

        return super.onOptionsItemSelected(item);

    }

    private String getTemplateHtml() {
		AssetManager as = getResources().getAssets();
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try{
            try {
                is = as.open("help.html");
                br = new BufferedReader(new InputStreamReader(is));

                String str;
                while((str = br.readLine()) != null){
                    sb.append(str +"\n");
                }
            } finally {
                if (br != null) br.close();
            }
        } catch (IOException e) {

        }

        return sb.toString();
    }

    /**
     * 戻るボタン等でアプリが再開したときのイベント
     *
     */
    protected void onRestart() {
    	super.onRestart();

    	// 指定画像が変更されていたら再表示
		Cursor cursor = puzzleDB.query("config", new String[] {"value"}, "code=?", new String[] {"image_type"}, null, null, null);
		if (cursor.moveToFirst()) {

			if (imageType != Integer.parseInt(cursor.getString(0))) {
				imageType = Integer.parseInt(cursor.getString(0));
		    	frameLayout.removeView(MainView);
		    	MainView = new MainView(this, imageType);
		    	frameLayout.addView(MainView);
			}
		}
		cursor.close();
    }

    /**
     * タッチイベント
     *
     */
    public boolean onTouchEvent(MotionEvent event) {

    	// Viewを複数レイヤーにした場合、
    	// Viewではタッチイベントがとれないため、Activityから実行
    	MainView.onTouch(event);

    	return true;
    }

	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

				//Toast.makeText(this, "Backボタンをキャンセルしました", Toast.LENGTH_LONG).show();

				Intent i = new Intent(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_HOME);
				startActivity(i);

				return true;

			}

		}

		return super.dispatchKeyEvent(event);

	}


    /**
     * ビュークラス
     *
     * @author ishida
     *
     */
    public class MainView extends View {

    	private Context thisContext;

    	// 画像
    	private Bitmap[] numbers;
    	private imagesGrid imagesGrid;
    	private Bitmap orgImage;
    	private int imageType = 0;
    	private String packageName = "";

    	// 状態
    	private boolean initGame = true;
    	private boolean finGame = false;
    	private boolean nextGame = false;
    	private boolean viewStatus = true;
    	//private boolean isComplete = false;
    	private boolean viewComplete = false;

    	// 画面レイアウト
    	private int deviceWidth = 0;
    	private int deviceHeight = 0;
    	private int centerX = 0;
    	private int centerY = 0;
    	private int time = 0;
    	//private String description = "";
    	//private String description2 = "";
    	private Bitmap prevButton;
    	private Bitmap nextButton;
    	private Rect prevButtonArea;
    	private Rect nextButtonArea;

    	// ページング
    	private int maxPage = 0;
    	private int currentPage = 1;
    	private int clearPage = 0;

    	// 定数
    	public static final int MOVABLE_UP = 1;
    	public static final int MOVABLE_DOWN = 2;
    	public static final int MOVABLE_LEFT = 3;
    	public static final int MOVABLE_RIGHT = 4;

    	/**
    	 * コンストラクタ
    	 *
    	 * @param context
    	 */
    	public MainView(Context context, int type) {
    		super(context);
    		// TODO Auto-generated constructor stub

            // ウインドウのサイズ
    		//setClickable(true);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            deviceWidth = display.getWidth();
            deviceHeight = display.getHeight();
            centerX = deviceWidth / 2;
            centerY = deviceHeight / 2;



            // 初期化
            imagesGrid = new imagesGrid(4, 4, deviceWidth, deviceHeight);
            maxPage = Integer.parseInt(getResources().getString(R.string.image_count));
            packageName = context.getPackageName();
            numbers = new Bitmap[15];
            imageType = type;
            thisContext = context;


            // レイアウト
            prevButton = BitmapFactory.decodeResource(getResources(), R.drawable.sub_blue_prev);
            nextButton = BitmapFactory.decodeResource(getResources(), R.drawable.sub_blue_next);


            // 前回までの情報で表示
    		Cursor cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"stage"}, null, null, null);
    		if (cursor.moveToFirst()) { // クリアステージ
    			clearPage = Integer.parseInt(cursor.getString(0));
    		}
    		cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"current"}, null, null, null);
    		if (cursor.moveToFirst()) { // 現在のステージ
    			currentPage = Integer.parseInt(cursor.getString(0));
    		}
    		cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"clear"}, null, null, null);
    		if (cursor.moveToFirst()) { // クリア
    			//isComplete = cursor.getString(0).equals("1") ? true : false;
    		}
    		cursor.close();


            if (type == 0) { // ナンバーの画像
            	setNumberImages();
            } else { // 通常の画像を分割して表示
            	int resId = getResources().getIdentifier(getResources().getString(R.string.file_name) + currentPage, "raw", context.getPackageName());
           		setImage(resId);
            }

    	}

		/**
		 * ナンバー画像表示
		 *
		 * @param packageName
		 */
    	private void setNumberImages() {
            for (int i=1; i<=numbers.length; i++) {
                int resId = getResources().getIdentifier("number" + String.valueOf(i), "drawable", packageName);
            	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            	int orgWidth = bitmap.getWidth();
            	int orgHeight = bitmap.getHeight();

            	float width = imagesGrid.imageWidth();
            	float height = imagesGrid.imageHeight();

            	Matrix matrix = new Matrix();
            	matrix.postScale(width / orgWidth, height / orgHeight);

            	numbers[i-1] = Bitmap.createBitmap(bitmap, 0, 0, orgWidth, orgHeight, matrix, true);
            }
    	}

    	/**
    	 * 画像表示
    	 *
    	 * @param resId
    	 */
    	private boolean setImage(int resId) {
        	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);

        	if (bitmap == null) return false;


        	int orgWidth = bitmap.getWidth();
        	int orgHeight = bitmap.getHeight();

        	//float resize = 0;


        	if (orgWidth <= orgHeight) {
        		//resize = deviceWidth;
        		orgHeight = orgWidth;
        	} else {
        		//resize = deviceHeight;
        		orgWidth = orgHeight;
        	}

        	// まず元画像を画面いっぱいのサイズにする
        	Matrix matrix = new Matrix();
        	matrix.postScale((float) imagesGrid.getFrame(0, 0).width()*4 / orgWidth, (float) imagesGrid.getFrame(0, 0).height()*4 / orgHeight);


        	bitmap = Bitmap.createBitmap(bitmap, 0, 0, orgWidth, orgHeight, matrix, true);


        	// 一辺の長さ
        	int frameWidth = bitmap.getWidth() / 4;
        	int frameHeight = bitmap.getHeight() / 4;

        	//if (deviceWidth < orgWidth) {
            	//frameWidth = imagesGrid.getFrame(0, 0).width();
            	//frameHeight = imagesGrid.getFrame(0, 0).height();
        	//}


        	// 画面に合わせてリサイズした画像を保存
        	//matrix = new Matrix();
        	//matrix.postScale(resize / orgWidth, resize / orgHeight);

            orgImage = bitmap;


        	// 分割して表示
        	int index = 0;
        	for (int y=0; y<4; y++) {
            	for (int x=0; x<4; x++) {

            		if (index < numbers.length) {
            			Rect orgRect = new Rect(x*frameWidth, y*frameHeight, (x+1)*frameWidth, (y+1)*frameHeight);
            			Rect rect = imagesGrid.getFrameByIndex(index);

            			// トリム
            			numbers[index] = Bitmap.createBitmap(bitmap, orgRect.left, orgRect.top, orgRect.width(), orgRect.height());

            			Matrix frameMatrix = new Matrix();
            			frameMatrix.postScale(rect.width() / orgRect.width(), rect.height() / orgRect.height());

            			numbers[index] = Bitmap.createBitmap(numbers[index], 0, 0, orgRect.width(), orgRect.height(), frameMatrix, true);
            		}

            		index++;
            	}
        	}


        	return true;
    	}

    	/**
    	 * 描画イベント
    	 *
    	 */
    	protected void onDraw(Canvas canvas) {
    		Paint paint = new Paint();
            Paint text = new Paint();
            Paint layer = new Paint();
            Paint title = new Paint();

            // アンチエイリアス
            text.setAntiAlias(true);
            paint.setAntiAlias(true);
            layer.setAntiAlias(true);
            title.setAntiAlias(true);

            // カラーのセット
            paint.setColor(Color.WHITE);
            paint.setTextSize(35 * scale);
            text.setColor(Color.WHITE);
            text.setTextSize(16 * scale);
            layer.setColor(Color.BLACK);
            layer.setAlpha(200);
            title.setColor(Color.BLACK);
            title.setAlpha(600);

            title.setStrokeWidth(100 * scale);
            title.setStrokeCap(Paint.Cap.ROUND);


            if (!finGame || imageType != 1) {
            	// 分割した、あるいは数字の画像を表示
                for (int i=1; i<=numbers.length; i++) {
                	Rect frame = imagesGrid.getFrameByIndex(i);
                	if (frame != null) {
                		canvas.drawBitmap(numbers[i-1], frame.left, frame.top, paint);
                	}
                }

                // ヒント表示（あっていない部分は暗くなる）
                if (viewHint) {
                	int index = 0;
	            	for (int y=0; y<4; y++) {
	                	for (int x=0; x<4; x++) {
	                		if (index < numbers.length) {
	                			Rect frame = imagesGrid.getFrameByIndex(index+1);
	                			if (frame != null) {
	                				Point point = imagesGrid.getFramePointByIndex(index+1);
	                    			if (point.x == x && point.y == y) {

	                    			} else {
	                    				canvas.drawRect(frame, layer);
	                    			}
	                			}
	                			index++;
	                		}
	                	}
	            	}
                }
            }

            if (viewComplete) {
        		// 完成系の画像を表示
        		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
        		canvas.drawLine(centerX - (100 * scale), centerY + (17 * scale), centerX + (100 * scale), centerY + (17 * scale), title);
        		canvas.drawText("ALL STAGE CLEAR!!", centerX - (145 * scale), centerY + (30 * scale), paint);
            } else {
                // スタート前
                if (initGame) {
                	if (imageType == 1) {
                		// 完成系の画像を表示
                		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
                		canvas.drawLine(centerX - (80 * scale), centerY + (17 * scale), centerX + (80 * scale), centerY + (17 * scale), title);
                		canvas.drawText("STAGE " + currentPage, centerX - (70 * scale), centerY + (10 * scale), paint);
                    	canvas.drawText("GAME START!!", centerX - (110 * scale), centerY + (50 * scale), paint);
                	} else {
                    	canvas.drawText("GAME START!!", centerX - (110 * scale), centerY + (10 * scale), paint);
                	}

                	time = 0;
                	mHandler.postDelayed(mUpdateImage, 100); // タイマーセット
                }

                // ゲームクリア
                if (finGame) {
                	if (imageType == 1) {
                		// 完成系の画像を表示
                		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
                	} else {
                    	canvas.drawText("GAME CLEAR", centerX - (100 * scale), centerY + (12 * scale), paint);
                	}
                	mHandler.removeCallbacks(mUpdateImage); // タイマー終了
                }
            }


            // カウンタの表示／非表示
            /*
            if (viewStatus) {
                if (this.time > 0) {
                	canvas.drawText(String.valueOf(time), centerX - 0, deviceHeight - 30, text);
                }

                if (imagesGrid.MoveCount() > 0) {
                	canvas.drawText(String.valueOf(imagesGrid.MoveCount()), centerX - 0, deviceHeight - 45, text);
                }
            }
            */


            // ステージ
        	canvas.drawText("STAGE : " + currentPage + " / " + maxPage, centerX - (50 * scale), deviceHeight - (42 * scale), text);
            canvas.drawText("MOVE : " + String.valueOf(imagesGrid.MoveCount()), centerX - (35 * scale), deviceHeight - (17 * scale), text);


            // ボタン
			canvas.drawBitmap(prevButton, 0, deviceHeight - (65 * scale), paint);
			canvas.drawBitmap(nextButton, deviceWidth - (70 * scale), deviceHeight - (65 * scale), paint);
			prevButtonArea = new Rect(0, (int) (deviceHeight - (55 * scale)), prevButton.getWidth(), deviceHeight);
			nextButtonArea = new Rect((int) (deviceWidth - (65 * scale)), (int) (deviceHeight - (65 * scale)), deviceWidth, deviceHeight);

    	}

    	/**
    	 * 擬似タッチイベント
    	 *
    	 * @param e
    	 * @return
    	 */
    	public boolean onTouch(MotionEvent e) {

    		if (viewComplete) {
		        switch( e.getAction() & MotionEvent.ACTION_MASK){
		        	case MotionEvent.ACTION_UP:
		        		new AlertDialog.Builder(thisContext).
		        		setNegativeButton("いいえ", null).
		        		setPositiveButton("はい", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
			        			// 初期化
								currentPage = 1;
								viewComplete = false;
								initGame = true;
								setStage(currentPage);
							}
		        		}).
		        		setMessage("おめでとうございます！\n全てのステージをクリアしました。\nクリアしたステージの画像はギャラリーからダウンロード出来ます。\n\nもう一度ゲームを始めますか？").
		        		create().
		        		show();
		        		break;
		        }

    		} else {

	    		// ページング
		        switch( e.getAction() & MotionEvent.ACTION_MASK){

		        	case MotionEvent.ACTION_UP:

		    			if ((prevButtonArea.left < e.getX() && prevButtonArea.right > e.getX()) && (prevButtonArea.bottom > e.getY() && prevButtonArea.top < e.getY())) {
		    				if (currentPage > 1) {

				        		if (!initGame&&!finGame) {
					        		new AlertDialog.Builder(thisContext).
					        		setNegativeButton("いいえ", null).
					        		setPositiveButton("はい", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
						        			// 初期化
					    					currentPage--;
					    					setStage(currentPage);
										}
					        		}).
					        		setMessage("ゲームの途中ですが、前のステージに戻りますか？").
					        		create().
					        		show();
					        		break;
				        		} else {
			    					currentPage--;
			    					setStage(currentPage);
				        		}

		    					return true;
		    				}
		    			}
		    			if ((nextButtonArea.left < e.getX() && nextButtonArea.right > e.getX()) && (nextButtonArea.bottom > e.getY() && nextButtonArea.top < e.getY())) {
		    				if (currentPage < maxPage) {
		    					if (currentPage <= clearPage) {

					        		if (!initGame&&!finGame) {
						        		new AlertDialog.Builder(thisContext).
						        		setNegativeButton("いいえ", null).
						        		setPositiveButton("はい", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int whichButton) {
							        			// 初期化
					    						currentPage++;
					    						setStage(currentPage);
											}
						        		}).
						        		setMessage("ゲームの途中ですが、次のステージに進みますか？").
						        		create().
						        		show();
						        		break;
					        		} else {
			    						currentPage++;
			    						setStage(currentPage);
					        		}

		    						return true;
		    					}
		    				}
		    			}

		        		break;
		        }


	    		// 画像をタップしたときの処理
	        	if (initGame || finGame) {
	    	        switch( e.getAction() & MotionEvent.ACTION_MASK){
			        	case MotionEvent.ACTION_UP:

			        		// タップした場所のフレーム
			        		Point frame = imagesGrid.onFramePoint(e.getX(), e.getY());

			        		if (frame != null) {

				        		if (finGame && nextGame){
				        			// 次のステージへ

				        			/*
				        			if (maxPage > currentPage) {
				        				currentPage++;
				        				clearPage++;
				        			} else {
				        				// currentPage = 1;
				        				clearPage = maxPage;
				        				isComplete = true;
				        			}


				                	int resId = getResources().getIdentifier(getResources().getString(R.string.file_name) + currentPage, "raw", packageName);
				               		setImage(resId);
				        			nextGame = false;
				        			finGame = false;

				        			this.InitGame();
				        			*/
				        		} else {
				        			// 初期化
				            		initGame = false;
				            		finGame = false;
				            		imagesGrid.Shuffle(1);
				            		imagesGrid.initMoveCount();
				            		invalidate();
				        		}
			        		}

			        		break;
	    	        }
	        	} else {
	    	        switch( e.getAction() & MotionEvent.ACTION_MASK){

			        	case MotionEvent.ACTION_DOWN:

			        		// タップした場所のフレーム
			        		Point frame = imagesGrid.onFramePoint(e.getX(), e.getY());

			        		if (frame != null) {
			        			imagesGrid.Dragging(0);
			        			imagesGrid.getMovableDirection(frame.x, frame.y);

				        		invalidate();
			        		}
			        		break;

			        	case MotionEvent.ACTION_UP:

			        		if (imagesGrid.Dragging()) {
			        			imagesGrid.moveFrame();

				        		//description = "";
				        		finGame = imagesGrid.isFinish();

				        		// DB登録
				        		if (finGame) {
				        			if (imageType == 1) {
				        				nextGame = true;
				        			}

	        	        			if (maxPage > currentPage) {
	        	        				if (currentPage > clearPage) {
	            	        				clearPage++;
	        	        				}
	        	        			} else {
	        	        				clearPage = maxPage;
	        	        				//isComplete = true;
	        	        			}

				        			recordData();

						        	ImageView view = new ImageView(thisContext);
						        	view.setImageBitmap(MainView.getOrgImage());


					        		new AlertDialog.Builder(thisContext).
					        		setPositiveButton("次のステージ", new DialogInterface.OnClickListener() {
		            					public void onClick(DialogInterface dialog, int whichButton) {
		            	        			if (maxPage > currentPage) {
		            	        				currentPage++;
		            	        			} else {
		            	        				viewComplete = true;
		            	        			}

		            	        			setStage(currentPage);
		            					}
		            				}).
		            				setTitle("STAGE CLEAR!!").
					        		setView(view).
					        		setIcon(null).
					        		create().
					        		show();

				        		}

				        		imagesGrid.Dragging(0);
				        		invalidate();

			        		} else {

			        		}

			        		break;

			        	case MotionEvent.ACTION_MOVE:

			        		if (imagesGrid.Dragging()) {
			        			imagesGrid.setDragLength(e.getX(), e.getY());
			        			invalidate();
			        		}

			        		break;
	    	        }
	        	}

    		}
        	viewHint = false;

        	//return false;
        	//return super.onTouchEvent(e);

    		return true;
    	}


    	/**
    	 * ステージ遷移
    	 *
    	 */
    	public void setStage(int stage) {
        	int resId = getResources().getIdentifier(getResources().getString(R.string.file_name) + stage, "raw", packageName);
       		setImage(resId);
			nextGame = false;
			finGame = false;

			imagesGrid.initMoveCount();
			InitGame();
			invalidate();

			// 現在のステージをDBに記録
			ContentValues values;
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			values = new ContentValues();
			values.put("update_time", simpleDateFormat.format(date));
			values.put("value", stage);
            puzzleDB.update("status", values, "code=?", new String[] {"current"});
    	}

    	/**
    	 * 結果を記録
    	 *
    	 */
    	public void recordData() {
			ContentValues values;
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			// ランクを登録
			values = new ContentValues();
			values.put("move_count", imagesGrid.MoveCount());
			values.put("elapsed_time", time);
			values.put("stage", currentPage);
			values.put("update_time", simpleDateFormat.format(date));
			puzzleDB.insert("result", null, values);

			// クリアステージを登録
			values = new ContentValues();
			values.put("update_time", simpleDateFormat.format(date));
			values.put("value", clearPage);
            puzzleDB.update("status", values, "code=?", new String[] {"stage"});

			// クリアフラグを登録
            if (clearPage == maxPage) {
    			values = new ContentValues();
    			values.put("update_time", simpleDateFormat.format(date));
    			values.put("value", 1);
                puzzleDB.update("status", values, "code=?", new String[] {"clear"});
            }
    	}

    	/**
    	 * 経過時間をセット
    	 *
    	 * @param value
    	 */
        public void setTime(int value) {
        	this.time = value;
        }

        /**
         * ゲームを初期化(はじめの状態に戻る)
         *
         */
        public void InitGame() {
        	initGame = true;
        	imagesGrid.InitGame();
        	invalidate();
        }

        /**
         * デバッグ用シャッフル
         *
         */
        public void Debug() {
        	imagesGrid.Debug();
        	invalidate();
        }

        /**
         * 表示状態（カウント）を設定
         *
         * @param value
         */
        public void setViewStatus(boolean value) {
        	this.viewStatus = value;
        	invalidate();
        }

        /**
         * 表示状態（カウント）を返す
         *
         * @return
         */
        public boolean getViewStatus() {
        	return this.viewStatus;
        }

        /**
         * オリジナル（分割前）の画像を返す
         *
         * @return
         */
        public Bitmap getOrgImage() {
        	return this.orgImage;
        }

        /**
         * クリアしたパズルの画像のリソースIDを返す
         *
         * @return
         */
        public int[] getImageResourceIds() {
        	int[] resIds = new int[clearPage];

        	for (int i=0; i<resIds.length; i++) {
        		resIds[i] = getResources().getIdentifier(getResources().getString(R.string.file_name) + (i+1), "raw", packageName);
        	}

        	return resIds;
        }

        /**
         * グリッドクラス
         *
         * @author ishida
         *
         */
    	private class imagesGrid {
    		private Rect[][] frames;
    		private int[][] frameIndex;
    		private int X;
    		private int Y;
        	private int deviceWidth = 0;
        	private int deviceHeight = 0;
        	//private int centerX = 0;
        	private int centerY = 0;
        	//private int frameCount = 0;
        	private boolean dragging;
        	private int dragDirection = 0;
        	private int[] dragIndex;
        	private int dragLength = 0;
        	private float dragBaseX = 0;
        	private float dragBaseY = 0;
        	private int frameWidth = 0;
        	private int moveCount = 0;

        	/**
        	 * コンストラクタ
        	 *
        	 * @param x 横のマス数
        	 * @param y 縦のマス数
        	 * @param w 画面の横幅
        	 * @param h 画面の縦幅
        	 */
    		public imagesGrid(int x, int y, int w, int h) {
    			X = x;
    			Y = y;
    			deviceWidth = w;
    			deviceHeight = h;
                centerX = deviceWidth / 2;
                centerY = deviceHeight / 2;

    			frames = new Rect[X][Y];
    			frameIndex = new int[X][Y];

    			//frameCount = X * Y;


                if (deviceWidth <= deviceHeight) {
                	frameWidth = deviceWidth / 4;
                } else {
                	frameWidth = deviceHeight / 4;
                }


                for (int i=0; i<frames.length; i++) {
                	frames[i][0] = new Rect(frameWidth * i, centerY - (frameWidth * 2), (frameWidth * i) + frameWidth, centerY - frameWidth);
                	frames[i][1] = new Rect(frameWidth * i, centerY - frameWidth      , (frameWidth * i) + frameWidth, centerY);
                	frames[i][2] = new Rect(frameWidth * i, centerY                   , (frameWidth * i) + frameWidth, centerY + frameWidth);
                	frames[i][3] = new Rect(frameWidth * i, centerY + frameWidth      , (frameWidth * i) + frameWidth, centerY + (frameWidth * 2));
                }


                InitGame();

    		}

    		/**
    		 * グリッドを初期化
    		 *
    		 */
    		public void InitGame() {
                int index=1;
   				for (int i=0; i<Y; i++) {
   	    			for (int n=0; n<X; n++) {
   	    				if (n == X-1 && i == Y-1) {
   	   	    				frameIndex[n][i] = 0;
   	    				} else {
   	   	    				frameIndex[n][i] = index;
   	    				}

   	    				index++;
    				}
    			}
    		}

    		/**
    		 * デバッグ用シャッフル
    		 *
    		 */
    		public void Debug() {
                int index=1;
   				for (int i=0; i<Y; i++) {
   	    			for (int n=0; n<X; n++) {
   	    				if (n == X-1 && i == Y-1) {
   	   	    				frameIndex[n][i] = index-1;
   	    				} else {
   	    					if (n == X-2 && i == Y-1) {
   	   	   	    				frameIndex[n][i] = 0;
   	    					} else {
   	   	   	    				frameIndex[n][i] = index;
   	    					}
   	    				}

   	    				index++;
    				}
    			}
    		}

    		/**
    		 * シャッフル
    		 *
    		 */
    		public void Shuffle(int fitCount) {
    			int shuffleCount = 0;

    			for(;;) {
    				// 何マスまで揃っていてもOKかの判定、また揃った後も数回シャッフル
    				if (getComplete() <= fitCount) {
    					shuffleCount++;
    					if (shuffleCount > 150) {
        					break;
    					}
    				} else {
    					shuffleCount = 0;
    				}

    				int x = getRandom(3, 0);
    				int y = getRandom(3, 0);
    				int direction = getMovableDirection(x, y);
    				Rect frame = getFrameByIndex(0);

    				// スライド
    				switch(direction) {
	    				case MOVABLE_UP:
	    					dragLength = frame.width();
	    					if (dragIndex.length < 2) {
		    					moveFrame();
	    					}
	    					Dragging(0);
	    					break;
	    				case MOVABLE_DOWN:
	    					dragLength = frame.width();
	    					if (dragIndex.length < 3) {
		    					moveFrame();
	    					}
	    					Dragging(0);
	    					break;
	    				case MOVABLE_LEFT:
	    					dragLength = frame.width();
	    					if (dragIndex.length < 3) {
		    					moveFrame();
	    					}
	    					Dragging(0);
	    					break;
	    				case MOVABLE_RIGHT:
	    					dragLength = frame.width();
	    					if (dragIndex.length < 3) {
		    					moveFrame();
	    					}
	    					Dragging(0);
	    					break;
	    				case 0:
	    					continue;
    				}

    				invalidate();
    			}

    			/*
    			String values[] = new String[frameCount];
    			for (int i=0; i<frameCount; i++) {
    				for(;;) {
        				int x = getRandom(3, 0);
        				int y = getRandom(3, 0);
        				boolean duplication = false;

        				for (String value : values) {
        					if (value != null && value.equals(x + ":" + y)) {
        						duplication = true;
        						break;
        					}
        				}

        				if (!duplication) {
            				frameIndex[x][y] = i;
            				values[i] = x + ":" + y;
            				break;
        				}
    				}
    			}
    			*/
    		}

    		/**
    		 *
    		 *
    		 * @return
    		 */
    		private int getComplete() {
    			int index=1;
    			int ret = 0;
   				for (int y=0; y<Y; y++) {
   	    			for (int x=0; x<X; x++) {
   	    				if (x == X-1 && y == Y-1) {
   	   	    				if (frameIndex[x][y] == 0) {
   	   	    					ret++;
   	   	    				}
   	    				} else {
   	   	    				if (frameIndex[x][y] == index) {
   	   	    					ret++;
   	   	    				}
   	    				}

   	    				index++;
    				}
    			}
   				return ret;
    		}

    		/**
    		 * ランダム値を返す
    		 *
    		 * @param max
    		 * @param min
    		 * @return
    		 */
    		private int getRandom(int max, int min) {
    			return (int)Math.floor(Math.random() * (max - min + 1)) + min;
    	    }

    		@SuppressWarnings({ "unused" })
			public Rect[][] getFrames() {
    			return this.frames;
    		}

    		public Rect getFrame(int x, int y) {
    			return this.frames[x][y];
    		}

    		/**
    		 * 指定インデックスのフレームを返す
    		 *
    		 * @param index
    		 * @return
    		 */
    		public Rect getFrameByIndex(int index) {
    			Point point = getFramePointByIndex(index);

    			if (point != null) {
    				Rect rect = new Rect();
    				rect.set(frames[point.x][point.y]);

    				if (dragIndex != null) {
            			for (int drag : dragIndex) {
            				if (drag == index) {
            	    			switch (dragDirection) {
    	        					case MOVABLE_UP:
    	        						rect.set(rect.left, rect.top-dragLength, rect.right, rect.bottom-dragLength);
    	        						break;
    	        					case MOVABLE_DOWN:
    	        						rect.set(rect.left, rect.top+dragLength, rect.right, rect.bottom+dragLength);
    	        						break;
    	        					case MOVABLE_LEFT:
    	        						rect.set(rect.left-dragLength, rect.top, rect.right-dragLength, rect.bottom);
    	        						break;
    	        					case MOVABLE_RIGHT:
    	        						rect.set(rect.left+dragLength, rect.top, rect.right+dragLength, rect.bottom);
    	        						break;
    	        					case 0:
    	        						break;
    	            			}
            					break;
            				}
            			}
    				}

    				return rect;
    			}

    			return null;
    		}

    		/**
    		 * 指定インデックスのグリッドの位置を返す
    		 *
    		 * @param index
    		 * @return
    		 */
    		public Point getFramePointByIndex(int index) {
   				for (int y=0; y<Y; y++) {
   	    			for (int x=0; x<X; x++) {
						if (frameIndex[x][y] == index) {
							return new Point(x, y);
						}
    				}
    			}
    			return null;
    		}

    		/**
    		 * 指定グリッドのインデックスを返す
    		 *
    		 * @param x
    		 * @param y
    		 * @return
    		 */
    		public int getIndexByPoint(int x, int y) {
    			return frameIndex[x][y];
    		}

    		/**
    		 * ゲームの終了状態(フラグ)を返す
    		 *
    		 * @return
    		 */
    		public boolean isFinish() {
    			int index=1;
   				for (int y=0; y<Y; y++) {
   	    			for (int x=0; x<X; x++) {
   	    				if (x == X-1 && y == Y-1) {
   	   	    				if (frameIndex[x][y] != 0) {
   	   	    					return false;
   	   	    				}
   	    				} else {
   	   	    				if (frameIndex[x][y] != index) {
   	   	    					return false;
   	   	    				}
   	    				}

   	    				index++;
    				}
    			}
   				return true;
    		}

    		/**
    		 * 1マスあたりの幅を返す
    		 *
    		 * @return
    		 */
    		public int imageWidth() {
    			return frames[0][0].width();
    		}

    		/**
    		 * 1マスあたりの高さを返す
    		 *
    		 * @return
    		 */
    		public int imageHeight() {
    			return frames[0][0].height();
    		}

    		/**
    		 * 指定グリッドのフレームを返す
    		 *
    		 * @param x
    		 * @param y
    		 * @return
    		 */
    		public Point onFramePoint(float x, float y) {
   				for (int i=0; i<Y; i++) {
   	    			for (int n=0; n<X; n++) {
   	    				if ((frames[n][i].left <= x && frames[n][i].right >= x) && (frames[n][i].bottom >= y && frames[n][i].top <= y)) {
   	    					dragBaseX = x;
   	    					dragBaseY = y;
   	    					return new Point(n, i);
   	    				}
   	    			}
   				}
    			return null;
    		}

    		/**
    		 * ドラッグ出来る方向を取得
    		 *
    		 * @param x
    		 * @param y
    		 * @return
    		 */
    		public int getMovableDirection(int x, int y) {
    			Point zeroFramePoint = getFramePointByIndex(0);

    			if (zeroFramePoint != null) {
    				//Log.i("frame", "zero " + zeroFramePoint.x + ":" + zeroFramePoint.y);

    				if (getIndexByPoint(x, y) == 0 ) {
    					return 0;
    				}

    				//Log.i("frame", x + ":" + y);

        			// 上方向に動かせるか
        			if (y > 0) {
        				if (zeroFramePoint.x == x) {
        					if (zeroFramePoint.y < y) {

        						dragIndex = new int[y-zeroFramePoint.y];
        						int i = 0;
        						for (int index=zeroFramePoint.y+1; index<=y; index++) {
        							dragIndex[i] = getIndexByPoint(x, index);
        							i++;
        						}

        						Dragging(MOVABLE_UP);
        						return MOVABLE_UP;
        					}
        				}
        			}

        			// 下方向に動かせるか
        			if (y < Y-1) {
        				if (zeroFramePoint.x == x) {
        					if (zeroFramePoint.y > y) {

        						dragIndex = new int[zeroFramePoint.y-y];
        						int i = 0;
        						for (int index=y; index<zeroFramePoint.y; index++) {
        							dragIndex[i] = getIndexByPoint(x, index);
        							i++;
        						}

        						Dragging(MOVABLE_DOWN);
        						return MOVABLE_DOWN;
        					}
        				}
        			}

        			// 左方向に動かせるか
        			if (x > 0) {
        				if (zeroFramePoint.y == y) {
        					if (zeroFramePoint.x < x) {

        						dragIndex = new int[x-zeroFramePoint.x];
        						int i = 0;
        						for (int index=zeroFramePoint.x+1; index<=x; index++) {
        							dragIndex[i] = getIndexByPoint(index, y);
        							i++;
        						}

        						Dragging(MOVABLE_LEFT);
        						return MOVABLE_LEFT;
        					}
        				}
        			}

        			// 右方向に動かせるか
        			if (x < X-1) {
        				if (zeroFramePoint.y == y) {
        					if (zeroFramePoint.x > x) {

        						dragIndex = new int[zeroFramePoint.x-x];
        						int i = 0;
        						for (int index=x; index<zeroFramePoint.x; index++) {
        							dragIndex[i] = getIndexByPoint(index, y);
        							i++;
        						}

        						Dragging(MOVABLE_RIGHT);
        						return MOVABLE_RIGHT;
        					}
        				}
        			}

    			}

    			return 0;
    		}

    		/**
    		 * ドラッグしている状態を更新
    		 *
    		 * @param value
    		 */
    		public void Dragging(int value) {
    			switch (value) {
    				case MOVABLE_UP:
    					dragDirection = MOVABLE_UP;
    					dragging = true;
    					break;
    				case MOVABLE_DOWN:
    					dragDirection = MOVABLE_DOWN;
    					dragging = true;
    					break;
    				case MOVABLE_LEFT:
    					dragDirection = MOVABLE_LEFT;
    					dragging = true;
    					break;
    				case MOVABLE_RIGHT:
    					dragDirection = MOVABLE_RIGHT;
    					dragging = true;
    					break;
    				case 0:
    					dragIndex = null;
    					dragDirection = 0;
    					dragLength = 0;
    					//dragBaseX = 0;
    					//dragBaseY = 0;
    					dragging = false;
    					break;
    			}
    		}

    		/**
    		 * ドラッグしている状態(フラグ)を返す
    		 *
    		 * @return
    		 */
    		public boolean Dragging() {
    			return this.dragging;
    		}

    		@SuppressWarnings("unused")
			public void setDragDirection(int value) {
    			this.dragDirection = value;
    		}

    		@SuppressWarnings("unused")
			public int getDragDirection() {
    			return this.dragDirection;
    		}

    		/**
    		 * ドラッグしている幅を取得
    		 *
    		 * @param x
    		 * @param y
    		 */
    		public void setDragLength(float x, float y) {
    			Rect frame = getFrameByIndex(0);

    			switch (dragDirection) {
					case MOVABLE_UP:
						dragLength = (int) (dragBaseY - y);

						if (dragLength < 0) {
							dragLength = 0;
						}

						if (frame.height() < dragLength) {
							dragLength = frame.height();
						}

						break;
					case MOVABLE_DOWN:
						dragLength = (int) (y - dragBaseY);

						if (dragLength < 0) {
							dragLength = 0;
						}

						if (frame.height() < dragLength) {
							dragLength = frame.height();
						}

						break;
					case MOVABLE_LEFT:
						dragLength = (int) (dragBaseX - x);

						if (dragLength < 0) {
							dragLength = 0;
						}

						if (frame.width() < dragLength) {
							dragLength = frame.width();
						}

						break;
					case MOVABLE_RIGHT:
						dragLength = (int) (x - dragBaseX);

						if (dragLength < 0) {
							dragLength = 0;
						}

						if (frame.width() < dragLength) {
							dragLength = frame.width();
						}

						break;
					case 0:
						dragLength = 0;
						break;
    			}

    		}

    		/**
    		 * マスを移動
    		 *
    		 */
    		public void moveFrame() {
				Rect zeroFrame = new Rect();
				Point zeroPoint = new Point(getFramePointByIndex(0).x, getFramePointByIndex(0).y);
				zeroFrame.set(getFrameByIndex(0));

    			switch (dragDirection) {
					case MOVABLE_UP:
						if (zeroFrame.height() / 2 < dragLength) {
							for (int index=0; index<dragIndex.length; index++) {
								Point point = new Point(getFramePointByIndex(dragIndex[index]).x, getFramePointByIndex(dragIndex[index]).y);
								Rect frame = new Rect();
								frame.set(getFrameByIndex(dragIndex[index]));
								frameIndex[point.x][point.y-1] = dragIndex[index];
							}

							frameIndex[zeroPoint.x][zeroPoint.y+dragIndex.length] = 0;

							moveCount++;
						}

						break;
					case MOVABLE_DOWN:
						if (zeroFrame.height() / 2 < dragLength) {
							for (int index=dragIndex.length-1; index>=0; index--) {
								Point point = new Point(getFramePointByIndex(dragIndex[index]).x, getFramePointByIndex(dragIndex[index]).y);
								Rect frame = new Rect();
								frame.set(getFrameByIndex(dragIndex[index]));
								frameIndex[point.x][point.y+1] = dragIndex[index];
							}

							frameIndex[zeroPoint.x][zeroPoint.y-dragIndex.length] = 0;

							moveCount++;
						}

						break;
					case MOVABLE_LEFT:
						if (zeroFrame.width() / 2 < dragLength) {
							for (int index=0; index<dragIndex.length; index++) {
								Point point = new Point(getFramePointByIndex(dragIndex[index]).x, getFramePointByIndex(dragIndex[index]).y);
								Rect frame = new Rect();
								frame.set(getFrameByIndex(dragIndex[index]));
								frameIndex[point.x-1][point.y] = dragIndex[index];
							}

							frameIndex[zeroPoint.x+dragIndex.length][zeroPoint.y] = 0;

							moveCount++;
						}

						break;
					case MOVABLE_RIGHT:
						if (zeroFrame.width() / 2 < dragLength) {
							for (int index=dragIndex.length-1; index>=0; index--) {
								Point point = new Point(getFramePointByIndex(dragIndex[index]).x, getFramePointByIndex(dragIndex[index]).y);
								Rect frame = new Rect();
								frame.set(getFrameByIndex(dragIndex[index]));
								frameIndex[point.x+1][point.y] = dragIndex[index];
							}

							frameIndex[zeroPoint.x-dragIndex.length][zeroPoint.y] = 0;

							moveCount++;
						}

						break;
					case 0:
						break;
    			}

    		}

    		/**
    		 * カウントを返す
    		 *
    		 * @return
    		 */
    		public int MoveCount() {
    			return this.moveCount;
    		}

    		/**
    		 * カウントをリセット
    		 */
    		public void initMoveCount() {
    			this.moveCount = 0;
    		}
    	}
    }
}

