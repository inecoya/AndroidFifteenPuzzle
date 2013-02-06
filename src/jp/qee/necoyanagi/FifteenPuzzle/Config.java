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

	// DB�p�I�u�W�F�N�g
	SQLite sqliteHelper;
	SQLiteDatabase puzzleDB;

	// �ϐ�
	private int imageType = 0;


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


        // ���C�A�E�g
		setContentView(R.layout.config);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);


		// DB�ڑ�
		sqliteHelper = new SQLite(getApplicationContext());
		puzzleDB = sqliteHelper.getWritableDatabase();
		Cursor cursor = puzzleDB.query("config", new String[] { "value" }, "code=?", new String[] {"image_type"}, null, null, null);



		// �{�^���C�x���g��`
		ImageButton button = (ImageButton) findViewById(R.id.backButton2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // �{�^�����N���b�N���ꂽ���ɌĂяo����܂�
            	finish();
            }
        });


        // ���W�I�{�^��
        if (cursor.moveToFirst()) {
        	int resId = getResources().getIdentifier("radiobutton" + String.valueOf(cursor.getInt(0)), "id", getPackageName());
            radioGroup.check(resId);
        }

        // ���W�I�{�^�� �`�F���W�C�x���g��`
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            	RadioButton radioButton = (RadioButton) findViewById(checkedId);
            	if (radioButton.getText().equals("�A���h���C�h")) {
                	imageType = 1;
            	} else {
            		imageType = 0;
            	}

            	// �ݒ��ۑ�
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
     * ���j���[�\��
     *
     */
    /*
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	//���j���[�C���t���[�^�[���擾
    	MenuInflater inflater = getMenuInflater();
    	//xml�̃��\�[�X�t�@�C�����g�p���ă��j���[�ɃA�C�e����ǉ�
    	inflater.inflate(R.menu.config, menu);
    	//�ł�����true��Ԃ�
    	return true;
    }
    */

    /**
     * ���j���[�N���b�N
     *
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.save: // �ۑ�
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
