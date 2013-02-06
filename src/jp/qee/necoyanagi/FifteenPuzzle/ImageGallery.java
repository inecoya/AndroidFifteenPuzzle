package jp.qee.necoyanagi.FifteenPuzzle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.qee.necoyanagi.FifteenPuzzle.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class ImageGallery extends Activity {

	private float scale;
	private ImageView selectedImage;
	private MapBitmapAdapter mapAdapter;
	private int[] resIds;
	private ImageManager imageManager;
	private ContentResolver contentResolver;
	private int imagePosition = -999;
	private Context thisContext;

	private static final String APPLICATION_NAME = "Animal Puzzle";

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
        thisContext = this;
        scale = getResources().getDisplayMetrics().density;


        // レイアウト
        setContentView(R.layout.gallery);


        // 画像保存用オブジェクトの準備
        imageManager = new ImageManager();
        contentResolver = this.getContentResolver();


        // 画像のリソースID
        Intent gallery = getIntent();
        resIds = gallery.getIntArrayExtra("RES_IDS");


        // 画像ギャラリーから選択された画像を表示するイメージビュー
        selectedImage = (ImageView) findViewById(R.id.selectedImage);


        // 画像ギャラリー
        Gallery imageMapGallery = (Gallery) findViewById(R.id.ImageGallery);


        // ギャラリーの画像リストアダプター作成
        mapAdapter = new MapBitmapAdapter(this);
        imageMapGallery.setAdapter(mapAdapter);
        imageMapGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> adapterView, View parent, int position, long id) {
        		// 選択された画像をイメージビューに表示
        		Bitmap selectedBitmap = (Bitmap) mapAdapter.getItem(position);
        		selectedImage.setImageBitmap(selectedBitmap);
        		imagePosition = position;
        	}
        });


        // アプリで保存した画像を画像リストアダプターにロードする
        loadMapImage(mapAdapter);


        // 画像ロードによりデータが変更されたことを通知する ※これをしないとギャラリーが表示されない
        mapAdapter.notifyDataSetChanged();


        // ゲームに戻る
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton3);
        backButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });


        // 画像を保存
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {

        		if (resIds.length > 0) {
        			if (imagePosition < 0) {
	               		new AlertDialog.Builder(thisContext).
	            		setNegativeButton("閉じる", null).
	            		setMessage("保存する画像を選んでください。").
	            		create().
	            		show();
        			} else {
                		imageManager.addImageAsApplication(contentResolver, BitmapFactory.decodeResource(getResources(), resIds[imagePosition]), getResources().getString(R.string.file_name) + (imagePosition+1));
                		Toast.makeText(thisContext, "ギャラリーの「"+ APPLICATION_NAME +"」に画像を保存しました。", Toast.LENGTH_SHORT).show();
        			}
        		} else {
               		new AlertDialog.Builder(thisContext).
            		setNegativeButton("閉じる", null).
            		setMessage("保存出来る画像がありません。").
            		create().
            		show();
        		}
        	}
        });

    }

    /**
     * 画像をスライドにロード
     *
     * @param mapAdapter
     */
    private void loadMapImage(MapBitmapAdapter mapAdapter) {
    	if (resIds.length > 0) {
    		Log.i("debug", resIds.length+"");
    		for (int resId : resIds) {
    			// 画像をロードする
    			try {
    				BitmapFactory.Options opt = new BitmapFactory.Options();
    				opt.inSampleSize=4;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, opt);
                    // イメージリストアダプターに追加
                    mapAdapter.addBitmap(bitmap);
    			} finally {

    			}
    		}
    	} else {
    		// ファイルリストが存在しない場合はトースト
            Toast.makeText(this, "画像がありません。クリアしたステージの画像をゲットしてください。", Toast.LENGTH_SHORT).show();
    	}
    }


    @Override
    public void onDestroy() {
    	super.onDestroy();

    	cleanupView(findViewById(R.id.galleryLayer));
    }

    /**
     * 指定したビュー階層内のドローワブルをクリアする。
     * （ドローワブルをのコールバックメソッドによるアクティビティのリークを防ぐため）
     * @param view
     */
    public static final void cleanupView(View view) {
        if(view instanceof ImageButton) {
            ImageButton ib = (ImageButton)view;
            ib.setImageDrawable(null);
        } else if(view instanceof ImageView) {
            ImageView iv = (ImageView)view;
            iv.setImageDrawable(null);
        } else if(view instanceof SeekBar) {
            SeekBar sb = (SeekBar)view;
            sb.setProgressDrawable(null);
            sb.setThumb(null);
        // } else if(view instanceof( xxxx )) {  -- 他にもDrawable を使用するUIコンポーネントがあれば追加
        }
        view.setBackgroundDrawable(null);
        if(view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            int size = vg.getChildCount();
            for(int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }

    /**
     * 画像保存用クラス
     *
     * @author ishida
     *
     */
    public class ImageManager {

    	private static final String TAG = "ImageManager";
    	private final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    	private final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + APPLICATION_NAME;

    	/**
    	 * 画像をカメラビューをして保存
    	 *
    	 * @param cr
    	 * @param bitmap
    	 * @return
    	 */
    	public Uri addImageAsCamera(ContentResolver cr, Bitmap bitmap) {
    		long dateTaken = System.currentTimeMillis();
    		String name = formatDate(dateTaken) + ".jpg";
    		String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name, null);
    		return Uri.parse(uriStr);
    	}

    	/**
    	 * 時刻をフォーマットして文字列で返す
    	 *
    	 * @param dateTaken
    	 * @return
    	 */
    	private String formatDate(long dateTaken) {
    		return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
    	}

    	/**
    	 * 画像を特定のフォルダに保存する
    	 *
    	 * @param cr
    	 * @param bitmap
    	 * @return
    	 */
        public Uri addImageAsApplication(ContentResolver cr, Bitmap bitmap, String prefix) {
        	long dateTaken = System.currentTimeMillis();
        	String name = prefix + "_" + formatDate(dateTaken) + ".jpg";
        	return addImageAsApplication(cr, name, dateTaken, PATH, name, bitmap, null);
        }

        /**
         * 画像を特定のフォルダに保存する
         *
         * @param cr
         * @param name
         * @param dateTaken
         * @param directory
         * @param filename
         * @param source
         * @param jpegData
         * @return
         */
        public Uri addImageAsApplication(ContentResolver cr, String name, long dateTaken, String directory, String filename, Bitmap source, byte[] jpegData) {
        	OutputStream outputStream = null;
        	String filePath = directory + "/" + filename;
        	try {
        		File dir = new File(directory);
        		if (!dir.exists()) {
        			dir.mkdirs();
        			Log.d(TAG, dir.toString() + " create");
        		}
        		File file = new File(directory, filename);
        		if (file.createNewFile()) {
        			outputStream = new FileOutputStream(file);
        			if (source != null) {
        				source.compress(CompressFormat.JPEG, 75, outputStream);
        			} else {
        				outputStream.write(jpegData);
        			}
        		}

        	} catch (FileNotFoundException ex) {
        		Log.w(TAG, ex);
        		return null;
        	} catch (IOException ex) {
        		Log.w(TAG, ex);
        		return null;
        	} finally {
        		if (outputStream != null) {
        			try {
        				outputStream.close();
        			} catch (Throwable t) {
        			}
        		}
        	}

        	ContentValues values = new ContentValues(7);
        	values.put(Images.Media.TITLE, name);
        	values.put(Images.Media.DISPLAY_NAME, filename);
        	values.put(Images.Media.DATE_TAKEN, dateTaken);
        	values.put(Images.Media.MIME_TYPE, "image/jpeg");
        	values.put(Images.Media.DATA, filePath);
        	return cr.insert(IMAGE_URI, values);
        }
    }

    /**
     * スライド用アダプタ
     *
     * @author ishida
     *
     */
    public class MapBitmapAdapter extends BaseAdapter {

    	private Context context;
    	// ロードされた画像ファイルを保持するリスト
    	private List<Bitmap> imageItems;
    	//private int galleryItemBackground;


    	/**
    	 * コンストラクタ
    	 *
    	 * @param context
    	 */
    	public MapBitmapAdapter(Context context) {
    		this.context = context;

    		//TypedArray typedArray = context.obtainStyledAttributes(R.styleable.myGallery);
    		//galleryItemBackground = typedArray.getResourceId(R.styleable.myGallery_android_galleryItemBackground, 0);
    		//typedArray.recycle();

    		imageItems = new ArrayList<Bitmap>();
    	}


    	@Override
    	public int getCount() {
    		// TODO Auto-generated method stub
    		return imageItems.size();
    	}

    	@Override
    	public Object getItem(int position) {
    		// TODO Auto-generated method stub
    		return imageItems.get(position);
    	}

    	@Override
    	public long getItemId(int position) {
    		// TODO Auto-generated method stub
    		return position;
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TODO Auto-generated method stub
    		// ギャラリーに画像を表示するためのイメージビュー作成
    		ImageView imageView = new ImageView(context);

    		// 表示する画像
    		Bitmap bitmap = (Bitmap) getItem(position);
    		imageView.setImageBitmap(bitmap);

    		// イメージビューでの表示
    		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    		imageView.setLayoutParams(new Gallery.LayoutParams((int) (70 * scale), (int) (100 * scale)));

    		// イメージビューの背景
    		//imageView.setBackgroundResource(galleryItemBackground);
    		return imageView;
    	}

    	/**
    	 * メインクラスでロードした画像イメージを追加
    	 *
    	 * @param image
    	 */
    	public void addBitmap(Bitmap image) {
    		imageItems.add(image);
    	}

    	/**
    	 * 不要になった画像イメージをリストから削除します
    	 *
    	 * @param index
    	 */
    	public void deleteBitmap(int index) {
    		imageItems.remove(index);
    	}

    	/**
    	 * クリーンアップ
    	 *
    	 */
    	public void clear() {
    		imageItems.clear();
    	}

    }

}
