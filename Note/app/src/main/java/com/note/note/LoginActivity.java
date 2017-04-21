package com.note.note;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.tablemanager.Connector;
import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * Created by Administrator on 2017/4/7.
 */

public class LoginActivity extends AppCompatActivity {
    /************************成员变量声明部分**********************/
    private Button mSignButton;
    private Button mLoginButton;
    private TextView mTextView;
    private TextView mAccount;
    private EditText mPassword;

    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private Cursor cursor;

    private Cursor cursor2;
    private CheckBox mCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        /*****************组件部分********************************/

        mAccount = (TextView) findViewById(R.id.account);//用户名
        mPassword = (EditText) findViewById(R.id.password);//密码
        mCheckBox = (CheckBox) findViewById(R.id.checkBox);//记住密码
        mCheckBox.setChecked(true);//默认记住密码
        mLoginButton = (Button) findViewById(R.id.login);//登录按钮
        mSignButton = (Button) findViewById(R.id.sign);//注册按钮
        mTextView = (TextView) findViewById(R.id.forget);//忘记密码文本

        /*******************数据库连接********************************/
        dbhelper = new DBOpen(LoginActivity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();

        /******************当数据库查询到有记住密码的帐号，首先默认用户名和密码为记住密码的帐号**********/

        cursor2 = db.rawQuery("select * from REMEMBER ", null);
        if (cursor2.moveToFirst()) {
                if(cursor2.getString(3).equals("true")){
                    mAccount.setText(cursor2.getString(0));
                    mPassword.setText(cursor2.getString(2));
                }
        }

        /**********************登录按钮响应事件********************************************/

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                cursor = db.rawQuery("select * from USER where username=? ", new String[]{mAccount.getText().toString()});
                /**作用户密码判断**/
                /**用户存在**/
                if (cursor.moveToFirst()) {
                    if (mPassword.getText().toString().equals(cursor.getString(1))) {
                        values.put("username", mAccount.getText().toString());
                        values.put("state", "true");//更新登录状态
                        values.put("password", mPassword.getText().toString());
                        if (mCheckBox.isChecked())//记住密码选择
                            values.put("flag", "true");
                        else
                            values.put("flag", "false");
                        if(cursor2.moveToFirst())
                            db.update("REMEMBER", values, "username=?", new String[]{cursor2.getString(0)});
                        values.clear();
                        cursor.close();
                        cursor2.close();
                        Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                        startActivity(intent);
                        finish();
                    } else
                        Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(LoginActivity.this, "用户不存在,请注册", Toast.LENGTH_SHORT).show();
            }
        });


        /********************注册按钮操作，跳转到注册页面*******************/
        mSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignActivity.class);
                startActivity(intent);
            }
        });


        /********************忘记密码文本，跳转到注册页面*******************/
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(intent);
            }
        });


    }

    /*********************** 按后退键返回桌面，不退出程序**********************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
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
