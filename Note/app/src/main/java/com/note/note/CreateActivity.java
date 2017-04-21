package com.note.note;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class CreateActivity extends AppCompatActivity {
    /**
     * 成员变量声明部分
     */
    private EditText mheadEditText;
    private EditText msortEditText;
    private int s = 0;
    private Button mButton;
    private EditText mbodyEditText;
    private ImageButton mback;

    private AlertDialog alert;
    private Spinner mSpinner;
    private ArrayAdapter<String> adapter;
    private String[] mStrings;
    private LinkedHashSet<String> set;

    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private Cursor mCursor;
    private Cursor mCursor1;
    private Cursor mCursor2;

    private ContentValues mValues;
    private Intent intent;
    private int edit_id;
    private String account;
    private String mString;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        /*****************
         * 组件部分
         */

        msortEditText = (EditText) findViewById(R.id.select_sort);//分类名
        mSpinner = (Spinner) findViewById(R.id.spinner);//分类列表
        mback = (ImageButton) findViewById(R.id.back);//返回键
        mButton = (Button) findViewById(R.id.finish);//新建键
        mbodyEditText = (EditText) findViewById(R.id.body);//内容
        mheadEditText = (EditText) findViewById(R.id.head);//title
        alert = new AlertDialog.Builder(CreateActivity.this).create();//提示框
        set = new LinkedHashSet<>();
        /*****************
         * 数据库部分
         */
        dbhelper = new DBOpen(CreateActivity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();
        mValues = new ContentValues();

        /***************
         * 当是从主页点击的笔记进行编辑时，显示已有笔记内容并记录已有笔记的id
         */
        mCursor = db.rawQuery("select * from NOTE where flag=?", new String[]{"true"});
        if (mCursor.moveToFirst()) {
            String[] arr = mCursor.getString(4).split(":");
            edit_id = mCursor.getInt(0);
            mheadEditText.setText(arr[1]);
            mString=mCursor.getString(3);//记录需要编辑笔记的分类
            msortEditText.setText(mCursor.getString(3));
            mbodyEditText.setText(mCursor.getString(2));
        }
        mCursor.close();

        /****************
         * 新建笔记帐号必须是已登录的account
         * ***********/
        mCursor = db.rawQuery("select * from REMEMBER where state=?", new String[]{"true"});
        if (mCursor.moveToFirst())
            account = mCursor.getString(0);
        mCursor1 = db.rawQuery("select * from SORT where username=?", new String[]{account});

        /**************
         * 为分类列表绑定数据，做法与Main2Activity中的spinner相同
         * ***************************/
        set.add(new String("请选择"));
        if (mCursor1.moveToFirst())
            do {

                set.add(mCursor1.getString(2));

            } while (mCursor1.moveToNext());
        mStrings = new String[set.size()];
        Iterator<String> iterator = set.iterator();

        while (iterator.hasNext()) {
            mStrings[s++] = iterator.next().toString();
        }
        mCursor1.close();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStrings);
        /***********
         * 设置下拉列表的风格
         * ********/
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpinner.setAdapter(adapter);

        /****************
         * mSpinner点击事件
         * ************************/
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mSpinner.getSelectedItem().toString().equals(mStrings[0]))
                    msortEditText.setText(mSpinner.getSelectedItem().toString());/**给分类编辑框setText*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /***************
         * 内容编辑框设置
         * ******************/
        mbodyEditText.setGravity(Gravity.TOP);//从顶部写起
        mbodyEditText.setSingleLine(false);//多行模式
        mbodyEditText.setHorizontallyScrolling(false);//自动换行

        /***************
         * 返回键操作
         * ***************************/
        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**************
                 * 既然是取消编辑，无论是新建还是编辑，flag必须设置为false
                 * ****************/
                alert.setMessage("取消编辑？");
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCursor1 = db.rawQuery("select * from NOTE where flag=?", new String[]{"true"});
                        if (mCursor1.moveToFirst()) {
                            mValues.put("flag", "false");
                            db.update("NOTE", mValues, "flag=?", new String[]{"true"});
                            mValues.clear();
                            mCursor1.close();
                        }
                        intent = new Intent(CreateActivity.this, Main2Activity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();
            }
        });
        /*****************
         * *完成操作
         * **************************/
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCursor.moveToFirst())
                    if (!mheadEditText.getText().toString().equals(""))
                        if (!msortEditText.getText().toString().equals(""))
                            {
                                mCursor2 = db.rawQuery("select * from NOTE where id=? ", new String[]{ Integer.toString(edit_id)});
                                mValues.put("username", mCursor.getString(0));
                                mValues.put("note", mbodyEditText.getText().toString());
                                mValues.put("sort", msortEditText.getText().toString());
                                mValues.put("title", msortEditText.getText().toString() + ":" + mheadEditText.getText().toString());
                                mValues.put("flag", "false");
                                /**************
                                 * 如果是编辑操作，则显示修改成功，即更新数据库相应笔记*
                                 * ************/
                                if (mCursor2.moveToFirst()) {
                                    db.update("NOTE", mValues, "id=?", new String[]{mCursor2.getString(0)});
                                    mValues.clear();
                                    mCursor2.close();
                                    Toast.makeText(CreateActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                }
                                /*******************
                                 * 数据库没有该笔记这插入该笔记
                                 * **************************/
                                else {
                                    db.insert("NOTE", null, mValues);
                                    mValues.clear();
                                    mCursor2.close();
                                    Toast.makeText(CreateActivity.this, "新建成功", Toast.LENGTH_SHORT).show();
                                }
                                /*******************
                                 * 这一步是对于SORT表而言的
                                 * ************************/
                                mValues.put("username", mCursor.getString(0));
                                mValues.put("sort", msortEditText.getText().toString());
                                mCursor2 = db.rawQuery("select * from SORT where username=? and sort=?", new String[]{mCursor.getString(0),
                                        msortEditText.getText().toString()});
                                /******************
                                 * 如果该笔记的分类在这个表中不存在，则新建分类项
                                 * ********/
                                if (!mCursor2.moveToFirst())
                                    db.insert("SORT", null, mValues);

                                intent = new Intent(CreateActivity.this, Main2Activity.class);
                                startActivity(intent);
                                finish();
                            }else
                                Toast.makeText(CreateActivity.this,"请填写分类",Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(CreateActivity.this,"请填写标题",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***********************
     * 按后退键返回桌面，不退出程序*
     * *********************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*********************
     * 重写onDestroy，在finish()前关闭数据库连接
     * *********************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

}
