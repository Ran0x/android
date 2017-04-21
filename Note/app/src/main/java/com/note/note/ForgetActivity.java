package com.note.note;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/7.
 */

public class ForgetActivity extends AppCompatActivity {
    /************************成员变量声明部分**********************/
    private EditText mAccount;
    private EditText mPassword;
    private EditText mRePassword;

    private EditText mAnswer;
    private Button mButton;
    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private Cursor mCursor;
    private TextView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget);

        /*****************组件部分********************************/

        mButton = (Button) findViewById(R.id.modify_pass_2);//修改密码按钮
        mAccount = (EditText) findViewById(R.id.account_2);//用户名
        mPassword = (EditText) findViewById(R.id.password_2);//密码
        mRePassword = (EditText) findViewById(R.id.repassword_2);//确认密码
        mAnswer = (EditText) findViewById(R.id.answer_2);//确认密保PIN




        /*******************数据库连接********************************/
        dbhelper = new DBOpen(ForgetActivity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();



        /*************用户名验证**********************/

        mAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_5);//定义提示文本
                mCursor = db.rawQuery("select * from USER where username=?", new String[]{mAccount.getText().toString()});
                if (mCursor.moveToFirst()) {
                    view.setText(null);
                    /**用户不存在时无法更改密码，提示用户不存在*/
                } else if (!mCursor.moveToFirst())
                    view.setText("用户不存在");
                mCursor.close();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*************密码修改（6-16位）**********************/

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_7);//提示文本
                if (mPassword.getText().length() < 6 || mPassword.getText().length() > 16)
                    view.setText("6-16位");
                else
                    view.setText(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*************密码确认）**********************/

        mRePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_8);
                if (!mPassword.getText().toString().equals(mRePassword.getText().toString()))
                    view.setText("密码不一致");
                else
                    view.setText(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**************密保验证（错误则无法修改）**************/

        mAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view = (TextView) findViewById(R.id.warn_6);
                mCursor = db.rawQuery("select * from USER where username=?", new String[]{mAccount.getText().toString()});
                if (mCursor.moveToFirst())
                    if (mAnswer.getText().toString().equals(mCursor.getString(2)))
                        view.setText(null);
                    else
                        view.setText("PIN码错误");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        /**************修改操作**********************/

        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCursor = db.rawQuery("select * from USER where username=?", new String[]{mAccount.getText().toString()});
                /**判断用户是否存在*/

                if (mCursor.moveToFirst()) {
                    /**判断密保*****/
                    if (mAnswer.getText().toString().equals(mCursor.getString(2)))
                        if (mPassword.getText().length() >= 6 && mPassword.getText().length() <= 16)
                            if (mPassword.getText().toString().equals(mRePassword.getText().toString())) {
                                ContentValues values = new ContentValues();
                                values.put("username", mAccount.getText().toString());
                                values.put("password", mPassword.getText().toString());

                                values.put("answer", mAnswer.getText().toString());
                                db.update("USER", values, "username=?", new String[]{mAccount.getText().toString()});
                                values.clear();
                                mCursor.close();
                                db.close();
                                Toast.makeText(ForgetActivity.this, "修改成功，返回登录", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                    }
                }
            }

        });
    }

    /********************返回上一个页面**********************************************/
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
