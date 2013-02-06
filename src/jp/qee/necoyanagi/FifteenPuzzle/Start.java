package jp.qee.necoyanagi.FifteenPuzzle;

import jp.qee.necoyanagi.FifteenPuzzle.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Start extends Activity {

	//private static final float GESTURE_THRESHOLD_DIP = 16.0f;
	private float scale;

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
        scale = getResources().getDisplayMetrics().density;

        // レイアウト
    	setContentView(new StartView(this));
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        Intent intent = new Intent(this, Puzzle.class);
        startActivity(intent);

    	return true;
    }

    public class StartView extends View {

    	private int deviceWidth = 0;
    	private int deviceHeight = 0;
    	private int centerX = 0;
    	private int centerY = 0;

    	/**
    	 * コンストラクタ
    	 *
    	 */
		public StartView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            deviceWidth = display.getWidth();
            deviceHeight = display.getHeight();
            centerX = deviceWidth / 2;
            centerY = deviceHeight / 2;
		}

    	/**
    	 * 描画イベント
    	 *
    	 */
    	protected void onDraw(Canvas canvas) {
    		Paint paint = new Paint();
    		Paint layer = new Paint();
    		Paint title = new Paint();

    		layer.setAntiAlias(true);
    		paint.setAntiAlias(true);
    		title.setAntiAlias(true);

            layer.setColor(Color.BLACK);
            layer.setAlpha(800);
            layer.setStrokeWidth(50 * scale);
            layer.setStrokeCap(Paint.Cap.ROUND);

            paint.setColor(Color.DKGRAY);
            paint.setTextSize(30 * scale);
            title.setColor(Color.LTGRAY);
            title.setTextSize(40 * scale);

    		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.backgruond4);
			Matrix matrix = new Matrix();
			int orgWidth = bitmap.getWidth();
			int orgHeight = bitmap.getHeight();

			matrix.postScale((float)deviceWidth / orgWidth, (float)deviceHeight / orgHeight);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, orgWidth, orgHeight, matrix, true);

			canvas.drawBitmap(bitmap, 0, 0, paint);
			canvas.drawText("Animal", centerX + (15 * scale), centerY - (60 * scale), title);
			canvas.drawText("Puzzle", centerX + (25 * scale), centerY - 0, title);

			//canvas.drawLine(centerX - 50, deviceHeight - 40, centerX + 50, deviceHeight - 40, layer);
			//canvas.drawText("START", centerX - 40, deviceHeight - 30, paint);
    	}
    }

}
