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
	 * �R���X�g���N�^
	 *
	 */
    @Override
    public void onCreate(Bundle context) {
        super.onCreate(context);

        // ��ʂ̏�����
        requestWindowFeature(Window.FEATURE_NO_TITLE); // �A�v������\��
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        thisContext = this;
        scale = getResources().getDisplayMetrics().density;


        // ���C�A�E�g
        setContentView(R.layout.gallery);


        // �摜�ۑ��p�I�u�W�F�N�g�̏���
        imageManager = new ImageManager();
        contentResolver = this.getContentResolver();


        // �摜�̃��\�[�XID
        Intent gallery = getIntent();
        resIds = gallery.getIntArrayExtra("RES_IDS");


        // �摜�M�������[����I�����ꂽ�摜��\������C���[�W�r���[
        selectedImage = (ImageView) findViewById(R.id.selectedImage);


        // �摜�M�������[
        Gallery imageMapGallery = (Gallery) findViewById(R.id.ImageGallery);


        // �M�������[�̉摜���X�g�A�_�v�^�[�쐬
        mapAdapter = new MapBitmapAdapter(this);
        imageMapGallery.setAdapter(mapAdapter);
        imageMapGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> adapterView, View parent, int position, long id) {
        		// �I�����ꂽ�摜���C���[�W�r���[�ɕ\��
        		Bitmap selectedBitmap = (Bitmap) mapAdapter.getItem(position);
        		selectedImage.setImageBitmap(selectedBitmap);
        		imagePosition = position;
        	}
        });


        // �A�v���ŕۑ������摜���摜���X�g�A�_�v�^�[�Ƀ��[�h����
        loadMapImage(mapAdapter);


        // �摜���[�h�ɂ��f�[�^���ύX���ꂽ���Ƃ�ʒm���� ����������Ȃ��ƃM�������[���\������Ȃ�
        mapAdapter.notifyDataSetChanged();


        // �Q�[���ɖ߂�
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton3);
        backButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		finish();
        	}
        });


        // �摜��ۑ�
        ImageButton saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {

        		if (resIds.length > 0) {
        			if (imagePosition < 0) {
	               		new AlertDialog.Builder(thisContext).
	            		setNegativeButton("����", null).
	            		setMessage("�ۑ�����摜��I��ł��������B").
	            		create().
	            		show();
        			} else {
                		imageManager.addImageAsApplication(contentResolver, BitmapFactory.decodeResource(getResources(), resIds[imagePosition]), getResources().getString(R.string.file_name) + (imagePosition+1));
                		Toast.makeText(thisContext, "�M�������[�́u"+ APPLICATION_NAME +"�v�ɉ摜��ۑ����܂����B", Toast.LENGTH_SHORT).show();
        			}
        		} else {
               		new AlertDialog.Builder(thisContext).
            		setNegativeButton("����", null).
            		setMessage("�ۑ��o����摜������܂���B").
            		create().
            		show();
        		}
        	}
        });

    }

    /**
     * �摜���X���C�h�Ƀ��[�h
     *
     * @param mapAdapter
     */
    private void loadMapImage(MapBitmapAdapter mapAdapter) {
    	if (resIds.length > 0) {
    		Log.i("debug", resIds.length+"");
    		for (int resId : resIds) {
    			// �摜�����[�h����
    			try {
    				BitmapFactory.Options opt = new BitmapFactory.Options();
    				opt.inSampleSize=4;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId, opt);
                    // �C���[�W���X�g�A�_�v�^�[�ɒǉ�
                    mapAdapter.addBitmap(bitmap);
    			} finally {

    			}
    		}
    	} else {
    		// �t�@�C�����X�g�����݂��Ȃ��ꍇ�̓g�[�X�g
            Toast.makeText(this, "�摜������܂���B�N���A�����X�e�[�W�̉摜���Q�b�g���Ă��������B", Toast.LENGTH_SHORT).show();
    	}
    }


    @Override
    public void onDestroy() {
    	super.onDestroy();

    	cleanupView(findViewById(R.id.galleryLayer));
    }

    /**
     * �w�肵���r���[�K�w���̃h���[���u�����N���A����B
     * �i�h���[���u�����̃R�[���o�b�N���\�b�h�ɂ��A�N�e�B�r�e�B�̃��[�N��h�����߁j
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
        // } else if(view instanceof( xxxx )) {  -- ���ɂ�Drawable ���g�p����UI�R���|�[�l���g������Βǉ�
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
     * �摜�ۑ��p�N���X
     *
     * @author ishida
     *
     */
    public class ImageManager {

    	private static final String TAG = "ImageManager";
    	private final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    	private final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + APPLICATION_NAME;

    	/**
    	 * �摜���J�����r���[�����ĕۑ�
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
    	 * �������t�H�[�}�b�g���ĕ�����ŕԂ�
    	 *
    	 * @param dateTaken
    	 * @return
    	 */
    	private String formatDate(long dateTaken) {
    		return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
    	}

    	/**
    	 * �摜�����̃t�H���_�ɕۑ�����
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
         * �摜�����̃t�H���_�ɕۑ�����
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
     * �X���C�h�p�A�_�v�^
     *
     * @author ishida
     *
     */
    public class MapBitmapAdapter extends BaseAdapter {

    	private Context context;
    	// ���[�h���ꂽ�摜�t�@�C����ێ����郊�X�g
    	private List<Bitmap> imageItems;
    	//private int galleryItemBackground;


    	/**
    	 * �R���X�g���N�^
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
    		// �M�������[�ɉ摜��\�����邽�߂̃C���[�W�r���[�쐬
    		ImageView imageView = new ImageView(context);

    		// �\������摜
    		Bitmap bitmap = (Bitmap) getItem(position);
    		imageView.setImageBitmap(bitmap);

    		// �C���[�W�r���[�ł̕\��
    		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    		imageView.setLayoutParams(new Gallery.LayoutParams((int) (70 * scale), (int) (100 * scale)));

    		// �C���[�W�r���[�̔w�i
    		//imageView.setBackgroundResource(galleryItemBackground);
    		return imageView;
    	}

    	/**
    	 * ���C���N���X�Ń��[�h�����摜�C���[�W��ǉ�
    	 *
    	 * @param image
    	 */
    	public void addBitmap(Bitmap image) {
    		imageItems.add(image);
    	}

    	/**
    	 * �s�v�ɂȂ����摜�C���[�W�����X�g����폜���܂�
    	 *
    	 * @param index
    	 */
    	public void deleteBitmap(int index) {
    		imageItems.remove(index);
    	}

    	/**
    	 * �N���[���A�b�v
    	 *
    	 */
    	public void clear() {
    		imageItems.clear();
    	}

    }

}
