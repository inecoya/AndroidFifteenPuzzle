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

	// �X���b�h
	private Handler mHandler = new Handler();
	private Runnable mUpdateImage;
	private int time = 0;

	// �r���[
	private MainView MainView;
	private FrameLayout frameLayout;

	// �ϐ�
	private boolean viewHint = false;
	private int imageType = 0;
	private float scale;

	// DB�p�I�u�W�F�N�g
	SQLite sqliteHelper;
	SQLiteDatabase puzzleDB;
	Cursor markerCursor;


    /** Called when the activity is first created. */
	/**
	 * �R���X�g���N�^
	 *
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ��ʂ̏�����
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // �X���[�v�����Ȃ�
        requestWindowFeature(Window.FEATURE_NO_TITLE); // �A�v������\��
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        scale = getResources().getDisplayMetrics().density;


        // DB�ڑ�
		sqliteHelper = new SQLite(getApplicationContext());
		puzzleDB = sqliteHelper.getWritableDatabase();


		// �f�t�H���g�l�擾
		Cursor cursor = puzzleDB.query("config", new String[] {"value"}, "code=?", new String[] {"image_type"}, null, null, null);
		if (cursor.moveToFirst()) {
			imageType = Integer.parseInt(cursor.getString(0));
		}


        // �r���[
		frameLayout = new FrameLayout(this.getApplicationContext());

		// �p�Y��
		MainView = new MainView(this, imageType);

		// �L��
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




        // �^�C�}�[�X���b�h
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
     * ���j���[�\��
     *
     */
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	//���j���[�C���t���[�^�[���擾
    	MenuInflater inflater = getMenuInflater();
    	//xml�̃��\�[�X�t�@�C�����g�p���ă��j���[�ɃA�C�e����ǉ�
    	inflater.inflate(R.menu.main, menu);
    	//�ł�����true��Ԃ�
    	return true;
    }

    /**
     * ���j���[�N���b�N
     *
     */
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

	        case R.id.reset: // �Q�[��������
	        	MainView.InitGame();
	        	break;

	        case R.id.debug: // �f�o�b�O
	        	MainView.Debug();
	        	break;

	        case R.id.ranking: // �����L���O
	            Intent ranking = new Intent(this, Ranking.class);
	            startActivity(ranking);
	            break;

	        case R.id.gallery: // �M�������[
	            Intent gallery = new Intent(this, ImageGallery.class);
	            gallery.putExtra("RES_IDS", MainView.getImageResourceIds()); // �Ώۉ摜�̃��\�[�XID��n��
	            startActivity(gallery);
	        	break;

	        case R.id.config: // �ݒ�
	            Intent config = new Intent(this, Config.class);
	            startActivity(config);
	            break;

	        case R.id.hint: // �q���g
	        	viewHint = true;
	        	MainView.invalidate();
	        	break;

	        case R.id.preview: // �v���r���[
	        	if (imageType == 1) {
		        	ImageView view = new ImageView(this);
		        	view.setImageBitmap(MainView.getOrgImage());

	        		new AlertDialog.Builder(this).
	        		setNegativeButton("����", null).
	        		setView(view).
	        		create().
	        		show();
	        	}
	        	break;

	        case R.id.help: // �w���v
	        	WebView webView = new WebView(this);
	        	webView.loadDataWithBaseURL("file:///android_asset/", getTemplateHtml(), "text/html", "utf-8", null);

        		new AlertDialog.Builder(this).
        		setNegativeButton("����", null).
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
     * �߂�{�^�����ŃA�v�����ĊJ�����Ƃ��̃C�x���g
     *
     */
    protected void onRestart() {
    	super.onRestart();

    	// �w��摜���ύX����Ă�����ĕ\��
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
     * �^�b�`�C�x���g
     *
     */
    public boolean onTouchEvent(MotionEvent event) {

    	// View�𕡐����C���[�ɂ����ꍇ�A
    	// View�ł̓^�b�`�C�x���g���Ƃ�Ȃ����߁AActivity������s
    	MainView.onTouch(event);

    	return true;
    }

	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

				//Toast.makeText(this, "Back�{�^�����L�����Z�����܂���", Toast.LENGTH_LONG).show();

				Intent i = new Intent(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_HOME);
				startActivity(i);

				return true;

			}

		}

		return super.dispatchKeyEvent(event);

	}


    /**
     * �r���[�N���X
     *
     * @author ishida
     *
     */
    public class MainView extends View {

    	private Context thisContext;

    	// �摜
    	private Bitmap[] numbers;
    	private imagesGrid imagesGrid;
    	private Bitmap orgImage;
    	private int imageType = 0;
    	private String packageName = "";

    	// ���
    	private boolean initGame = true;
    	private boolean finGame = false;
    	private boolean nextGame = false;
    	private boolean viewStatus = true;
    	//private boolean isComplete = false;
    	private boolean viewComplete = false;

    	// ��ʃ��C�A�E�g
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

    	// �y�[�W���O
    	private int maxPage = 0;
    	private int currentPage = 1;
    	private int clearPage = 0;

    	// �萔
    	public static final int MOVABLE_UP = 1;
    	public static final int MOVABLE_DOWN = 2;
    	public static final int MOVABLE_LEFT = 3;
    	public static final int MOVABLE_RIGHT = 4;

    	/**
    	 * �R���X�g���N�^
    	 *
    	 * @param context
    	 */
    	public MainView(Context context, int type) {
    		super(context);
    		// TODO Auto-generated constructor stub

            // �E�C���h�E�̃T�C�Y
    		//setClickable(true);
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            deviceWidth = display.getWidth();
            deviceHeight = display.getHeight();
            centerX = deviceWidth / 2;
            centerY = deviceHeight / 2;



            // ������
            imagesGrid = new imagesGrid(4, 4, deviceWidth, deviceHeight);
            maxPage = Integer.parseInt(getResources().getString(R.string.image_count));
            packageName = context.getPackageName();
            numbers = new Bitmap[15];
            imageType = type;
            thisContext = context;


            // ���C�A�E�g
            prevButton = BitmapFactory.decodeResource(getResources(), R.drawable.sub_blue_prev);
            nextButton = BitmapFactory.decodeResource(getResources(), R.drawable.sub_blue_next);


            // �O��܂ł̏��ŕ\��
    		Cursor cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"stage"}, null, null, null);
    		if (cursor.moveToFirst()) { // �N���A�X�e�[�W
    			clearPage = Integer.parseInt(cursor.getString(0));
    		}
    		cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"current"}, null, null, null);
    		if (cursor.moveToFirst()) { // ���݂̃X�e�[�W
    			currentPage = Integer.parseInt(cursor.getString(0));
    		}
    		cursor = puzzleDB.query("status", new String[] {"value"}, "code=?", new String[] {"clear"}, null, null, null);
    		if (cursor.moveToFirst()) { // �N���A
    			//isComplete = cursor.getString(0).equals("1") ? true : false;
    		}
    		cursor.close();


            if (type == 0) { // �i���o�[�̉摜
            	setNumberImages();
            } else { // �ʏ�̉摜�𕪊����ĕ\��
            	int resId = getResources().getIdentifier(getResources().getString(R.string.file_name) + currentPage, "raw", context.getPackageName());
           		setImage(resId);
            }

    	}

		/**
		 * �i���o�[�摜�\��
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
    	 * �摜�\��
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

        	// �܂����摜����ʂ����ς��̃T�C�Y�ɂ���
        	Matrix matrix = new Matrix();
        	matrix.postScale((float) imagesGrid.getFrame(0, 0).width()*4 / orgWidth, (float) imagesGrid.getFrame(0, 0).height()*4 / orgHeight);


        	bitmap = Bitmap.createBitmap(bitmap, 0, 0, orgWidth, orgHeight, matrix, true);


        	// ��ӂ̒���
        	int frameWidth = bitmap.getWidth() / 4;
        	int frameHeight = bitmap.getHeight() / 4;

        	//if (deviceWidth < orgWidth) {
            	//frameWidth = imagesGrid.getFrame(0, 0).width();
            	//frameHeight = imagesGrid.getFrame(0, 0).height();
        	//}


        	// ��ʂɍ��킹�ă��T�C�Y�����摜��ۑ�
        	//matrix = new Matrix();
        	//matrix.postScale(resize / orgWidth, resize / orgHeight);

            orgImage = bitmap;


        	// �������ĕ\��
        	int index = 0;
        	for (int y=0; y<4; y++) {
            	for (int x=0; x<4; x++) {

            		if (index < numbers.length) {
            			Rect orgRect = new Rect(x*frameWidth, y*frameHeight, (x+1)*frameWidth, (y+1)*frameHeight);
            			Rect rect = imagesGrid.getFrameByIndex(index);

            			// �g����
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
    	 * �`��C�x���g
    	 *
    	 */
    	protected void onDraw(Canvas canvas) {
    		Paint paint = new Paint();
            Paint text = new Paint();
            Paint layer = new Paint();
            Paint title = new Paint();

            // �A���`�G�C���A�X
            text.setAntiAlias(true);
            paint.setAntiAlias(true);
            layer.setAntiAlias(true);
            title.setAntiAlias(true);

            // �J���[�̃Z�b�g
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
            	// ���������A���邢�͐����̉摜��\��
                for (int i=1; i<=numbers.length; i++) {
                	Rect frame = imagesGrid.getFrameByIndex(i);
                	if (frame != null) {
                		canvas.drawBitmap(numbers[i-1], frame.left, frame.top, paint);
                	}
                }

                // �q���g�\���i�����Ă��Ȃ������͈Â��Ȃ�j
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
        		// �����n�̉摜��\��
        		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
        		canvas.drawLine(centerX - (100 * scale), centerY + (17 * scale), centerX + (100 * scale), centerY + (17 * scale), title);
        		canvas.drawText("ALL STAGE CLEAR!!", centerX - (145 * scale), centerY + (30 * scale), paint);
            } else {
                // �X�^�[�g�O
                if (initGame) {
                	if (imageType == 1) {
                		// �����n�̉摜��\��
                		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
                		canvas.drawLine(centerX - (80 * scale), centerY + (17 * scale), centerX + (80 * scale), centerY + (17 * scale), title);
                		canvas.drawText("STAGE " + currentPage, centerX - (70 * scale), centerY + (10 * scale), paint);
                    	canvas.drawText("GAME START!!", centerX - (110 * scale), centerY + (50 * scale), paint);
                	} else {
                    	canvas.drawText("GAME START!!", centerX - (110 * scale), centerY + (10 * scale), paint);
                	}

                	time = 0;
                	mHandler.postDelayed(mUpdateImage, 100); // �^�C�}�[�Z�b�g
                }

                // �Q�[���N���A
                if (finGame) {
                	if (imageType == 1) {
                		// �����n�̉摜��\��
                		canvas.drawBitmap(orgImage, 0, centerY - (imagesGrid.getFrame(0, 0).height() * 2), paint);
                	} else {
                    	canvas.drawText("GAME CLEAR", centerX - (100 * scale), centerY + (12 * scale), paint);
                	}
                	mHandler.removeCallbacks(mUpdateImage); // �^�C�}�[�I��
                }
            }


            // �J�E���^�̕\���^��\��
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


            // �X�e�[�W
        	canvas.drawText("STAGE : " + currentPage + " / " + maxPage, centerX - (50 * scale), deviceHeight - (42 * scale), text);
            canvas.drawText("MOVE : " + String.valueOf(imagesGrid.MoveCount()), centerX - (35 * scale), deviceHeight - (17 * scale), text);


            // �{�^��
			canvas.drawBitmap(prevButton, 0, deviceHeight - (65 * scale), paint);
			canvas.drawBitmap(nextButton, deviceWidth - (70 * scale), deviceHeight - (65 * scale), paint);
			prevButtonArea = new Rect(0, (int) (deviceHeight - (55 * scale)), prevButton.getWidth(), deviceHeight);
			nextButtonArea = new Rect((int) (deviceWidth - (65 * scale)), (int) (deviceHeight - (65 * scale)), deviceWidth, deviceHeight);

    	}

    	/**
    	 * �[���^�b�`�C�x���g
    	 *
    	 * @param e
    	 * @return
    	 */
    	public boolean onTouch(MotionEvent e) {

    		if (viewComplete) {
		        switch( e.getAction() & MotionEvent.ACTION_MASK){
		        	case MotionEvent.ACTION_UP:
		        		new AlertDialog.Builder(thisContext).
		        		setNegativeButton("������", null).
		        		setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
			        			// ������
								currentPage = 1;
								viewComplete = false;
								initGame = true;
								setStage(currentPage);
							}
		        		}).
		        		setMessage("���߂łƂ��������܂��I\n�S�ẴX�e�[�W���N���A���܂����B\n�N���A�����X�e�[�W�̉摜�̓M�������[����_�E�����[�h�o���܂��B\n\n������x�Q�[�����n�߂܂����H").
		        		create().
		        		show();
		        		break;
		        }

    		} else {

	    		// �y�[�W���O
		        switch( e.getAction() & MotionEvent.ACTION_MASK){

		        	case MotionEvent.ACTION_UP:

		    			if ((prevButtonArea.left < e.getX() && prevButtonArea.right > e.getX()) && (prevButtonArea.bottom > e.getY() && prevButtonArea.top < e.getY())) {
		    				if (currentPage > 1) {

				        		if (!initGame&&!finGame) {
					        		new AlertDialog.Builder(thisContext).
					        		setNegativeButton("������", null).
					        		setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
						        			// ������
					    					currentPage--;
					    					setStage(currentPage);
										}
					        		}).
					        		setMessage("�Q�[���̓r���ł����A�O�̃X�e�[�W�ɖ߂�܂����H").
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
						        		setNegativeButton("������", null).
						        		setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int whichButton) {
							        			// ������
					    						currentPage++;
					    						setStage(currentPage);
											}
						        		}).
						        		setMessage("�Q�[���̓r���ł����A���̃X�e�[�W�ɐi�݂܂����H").
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


	    		// �摜���^�b�v�����Ƃ��̏���
	        	if (initGame || finGame) {
	    	        switch( e.getAction() & MotionEvent.ACTION_MASK){
			        	case MotionEvent.ACTION_UP:

			        		// �^�b�v�����ꏊ�̃t���[��
			        		Point frame = imagesGrid.onFramePoint(e.getX(), e.getY());

			        		if (frame != null) {

				        		if (finGame && nextGame){
				        			// ���̃X�e�[�W��

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
				        			// ������
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

			        		// �^�b�v�����ꏊ�̃t���[��
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

				        		// DB�o�^
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
					        		setPositiveButton("���̃X�e�[�W", new DialogInterface.OnClickListener() {
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
    	 * �X�e�[�W�J��
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

			// ���݂̃X�e�[�W��DB�ɋL�^
			ContentValues values;
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			values = new ContentValues();
			values.put("update_time", simpleDateFormat.format(date));
			values.put("value", stage);
            puzzleDB.update("status", values, "code=?", new String[] {"current"});
    	}

    	/**
    	 * ���ʂ��L�^
    	 *
    	 */
    	public void recordData() {
			ContentValues values;
			Date date = new Date(System.currentTimeMillis());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			// �����N��o�^
			values = new ContentValues();
			values.put("move_count", imagesGrid.MoveCount());
			values.put("elapsed_time", time);
			values.put("stage", currentPage);
			values.put("update_time", simpleDateFormat.format(date));
			puzzleDB.insert("result", null, values);

			// �N���A�X�e�[�W��o�^
			values = new ContentValues();
			values.put("update_time", simpleDateFormat.format(date));
			values.put("value", clearPage);
            puzzleDB.update("status", values, "code=?", new String[] {"stage"});

			// �N���A�t���O��o�^
            if (clearPage == maxPage) {
    			values = new ContentValues();
    			values.put("update_time", simpleDateFormat.format(date));
    			values.put("value", 1);
                puzzleDB.update("status", values, "code=?", new String[] {"clear"});
            }
    	}

    	/**
    	 * �o�ߎ��Ԃ��Z�b�g
    	 *
    	 * @param value
    	 */
        public void setTime(int value) {
        	this.time = value;
        }

        /**
         * �Q�[����������(�͂��߂̏�Ԃɖ߂�)
         *
         */
        public void InitGame() {
        	initGame = true;
        	imagesGrid.InitGame();
        	invalidate();
        }

        /**
         * �f�o�b�O�p�V���b�t��
         *
         */
        public void Debug() {
        	imagesGrid.Debug();
        	invalidate();
        }

        /**
         * �\����ԁi�J�E���g�j��ݒ�
         *
         * @param value
         */
        public void setViewStatus(boolean value) {
        	this.viewStatus = value;
        	invalidate();
        }

        /**
         * �\����ԁi�J�E���g�j��Ԃ�
         *
         * @return
         */
        public boolean getViewStatus() {
        	return this.viewStatus;
        }

        /**
         * �I���W�i���i�����O�j�̉摜��Ԃ�
         *
         * @return
         */
        public Bitmap getOrgImage() {
        	return this.orgImage;
        }

        /**
         * �N���A�����p�Y���̉摜�̃��\�[�XID��Ԃ�
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
         * �O���b�h�N���X
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
        	 * �R���X�g���N�^
        	 *
        	 * @param x ���̃}�X��
        	 * @param y �c�̃}�X��
        	 * @param w ��ʂ̉���
        	 * @param h ��ʂ̏c��
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
    		 * �O���b�h��������
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
    		 * �f�o�b�O�p�V���b�t��
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
    		 * �V���b�t��
    		 *
    		 */
    		public void Shuffle(int fitCount) {
    			int shuffleCount = 0;

    			for(;;) {
    				// ���}�X�܂ő����Ă��Ă�OK���̔���A�܂��������������V���b�t��
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

    				// �X���C�h
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
    		 * �����_���l��Ԃ�
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
    		 * �w��C���f�b�N�X�̃t���[����Ԃ�
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
    		 * �w��C���f�b�N�X�̃O���b�h�̈ʒu��Ԃ�
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
    		 * �w��O���b�h�̃C���f�b�N�X��Ԃ�
    		 *
    		 * @param x
    		 * @param y
    		 * @return
    		 */
    		public int getIndexByPoint(int x, int y) {
    			return frameIndex[x][y];
    		}

    		/**
    		 * �Q�[���̏I�����(�t���O)��Ԃ�
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
    		 * 1�}�X������̕���Ԃ�
    		 *
    		 * @return
    		 */
    		public int imageWidth() {
    			return frames[0][0].width();
    		}

    		/**
    		 * 1�}�X������̍�����Ԃ�
    		 *
    		 * @return
    		 */
    		public int imageHeight() {
    			return frames[0][0].height();
    		}

    		/**
    		 * �w��O���b�h�̃t���[����Ԃ�
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
    		 * �h���b�O�o����������擾
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

        			// ������ɓ������邩
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

        			// �������ɓ������邩
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

        			// �������ɓ������邩
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

        			// �E�����ɓ������邩
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
    		 * �h���b�O���Ă����Ԃ��X�V
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
    		 * �h���b�O���Ă�����(�t���O)��Ԃ�
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
    		 * �h���b�O���Ă��镝���擾
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
    		 * �}�X���ړ�
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
    		 * �J�E���g��Ԃ�
    		 *
    		 * @return
    		 */
    		public int MoveCount() {
    			return this.moveCount;
    		}

    		/**
    		 * �J�E���g�����Z�b�g
    		 */
    		public void initMoveCount() {
    			this.moveCount = 0;
    		}
    	}
    }
}

