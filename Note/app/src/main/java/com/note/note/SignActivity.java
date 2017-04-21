package com.note.note;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/7.
 */

public class SignActivity extends AppCompatActivity {
    /************************成员变量声明部分**********************/
    private Button mSign_1_Button;
    private EditText mAccount;
    private EditText mPassword;
    private EditText mRePassword;

    private EditText mAnswer;

    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private Cursor mCursor;
    private TextView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign);
        /*******************数据库连接********************************/
        dbhelper = new DBOpen(SignActivity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();
        /*****************组件部分********************************/

        mSign_1_Button = (Button) findViewById(R.id.sign_1);//注册按钮
        mAccount = (EditText) findViewById(R.id.account_1);//注册用户名
        mPassword = (EditText) findViewById(R.id.password_1);//注册密码
        mRePassword = (EditText) findViewById(R.id.repassword_1);//确认密码
        mAnswer = (EditText) findViewById(R.id.answer_1);//密保PIN


        /*************用户名验证**********************/
        mAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /************用户名存在则提示用户存在，提醒用户更改其他用户名*************/
                mCursor = db.rawQuery("select * from USER where username=?", new String[]{mAccount.getText().toString()});
                view = (TextView) findViewById(R.id.warn_1);
                if (mCursor.moveToFirst()) {
                    view.setText("用户存在");
                    mCursor.close();
                } else
                    view.setText(null);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /***************密码验证**********************
         * 6-16位*/
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_2);
                if (mPassword.getText().length() < 6 || mPassword.getText().length() > 16)
                    view.setText("6-16位");
                else
                    view.setText(null);

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        /************确认密码验证，要和密码一致才可***********/
        mRePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_3);
                if (!mPassword.getText().toString().equals(mRePassword.getText().toString()))
                    view.setText("密码不一致");
                else
                    view.setText(null);


            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        /***************注册按钮功能***********************/
        mSign_1_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /************用户不存在即注册*****************/
                if (!mCursor.moveToFirst())
                    if (mPassword.getText().length() >= 6 && mPassword.getText().length() <= 16)
                        if (mPassword.getText().toString().equals(mRePassword.getText().toString())) {
                            /**添加数据*/
                            ContentValues values = new ContentValues();
                            values.put("username", mAccount.getText().toString());
                            values.put("password", mPassword.getText().toString());

                            values.put("answer", mAnswer.getText().toString());
                            db.insert("USER", null, values);
                            values.clear();
                            db.close();
                            Toast.makeText(SignActivity.this, "注册成功，返回登录", Toast.LENGTH_LONG).show();
                            /************返回登录操作****************/
                            Intent intent = new Intent(SignActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
            }
        });

    }

    /**************按下后退键返回上一个页面*********************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*********************重写onDestroy，在finish()前关闭数据库连接*********************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

}



