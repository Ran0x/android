package com.note.note;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ManageActivity extends AppCompatActivity {
    /************************成员变量声明部分**********************/
    private ListView list;
    private AlertDialog alert;
    private AlertDialog alert2;
    private AlertDialog alert1;

    private Cursor cursor;
    private Intent intent;
    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private ArrayAdapter<String> adapter;

    private ContentValues values;
    private LinkedHashSet set;


    private String[] mStrings;
    private String account;

    private Button mDelete;
    private Button mNew;

    private Spinner spinner;


    private TextView textView;
    private LayoutInflater layoutInflater;
    private View view;

    private Spinner mSpinner;
    private EditText mEditText_1;
    private int pos;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        /*****************组件部分********************************/
        mDelete = (Button) findViewById(R.id.delete);//删除按钮
        mNew = (Button) findViewById(R.id.newsort);//管理分类按钮
        list = (ListView) findViewById(R.id.listSort);//显示相应分类的笔记
        spinner = (Spinner) findViewById(R.id.spinner_sort);//分类下拉列表
        set = new LinkedHashSet<>();
        alert = new AlertDialog.Builder(ManageActivity.this).create();
        alert1 = new AlertDialog.Builder(ManageActivity.this).create();
        alert2 = new AlertDialog.Builder(ManageActivity.this).create();


        /*******************数据库连接********************************/
        dbhelper = new DBOpen(ManageActivity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();
        values = new ContentValues();
        /*************LayoutInflater的作用是找到layout*************************/
        layoutInflater = (LayoutInflater) ManageActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.newsort, null);
        /************这是R.layout.newsort里的两个组件***********/
        mSpinner = (Spinner) view.findViewById(R.id.editsort);
        mEditText_1 = (EditText) view.findViewById(R.id.editsort_1);

        /*******************记录登录状态的用户名account*********************************/
        cursor = db.rawQuery("select * from REMEMBER where state=?", new String[]{"true"});
        if (cursor.moveToFirst()) {

            account = cursor.getString(0);
            cursor.close();
        }
        /*********************为spinner绑定数据****************************************/
        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
        showAll(cursor, 2);
        spinner.setAdapter(adapter);

        /********************为list绑定数据************************************************/
        spinner_show();
        textView=(TextView)findViewById(R.id.noData);
        list.setEmptyView(textView);
        /*******************listItem点击事件响应，点击只提示是否删除，这里不新建笔记，只管理**************************************/
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                alert = new AlertDialog.Builder(ManageActivity.this).create();
                alert.setMessage("删除该笔记");
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("NOTE", "username=? and sort=? and title=?", new String[]{account, spinner.getSelectedItem().toString(), list.getItemAtPosition(position).toString()});
                        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});

                        showAll(cursor, 2);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(pos);
                        spinner_show();
                        Toast.makeText(ManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.setButton(AlertDialog.BUTTON_NEGATIVE, "放弃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alert.show();
            }
        });
        /*********************删除按钮功能：删除该分类的所有笔记********************************/
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlerg();
            }

        });
        /***********************管理分类按钮功能
         * 1.删除分类并删除其笔记
         * 2.新建分类
         * 3.修改分类名******************************************/
        mNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlerg_1();

            }
        });

    }

    /**************删除分类下所有笔记的方法*****************/
    private void showAlerg() {
        alert.setMessage("是否删除该分类所有笔记？");
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ManageActivity.this, "删除全部笔记成功", Toast.LENGTH_SHORT).show();
                cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
                db.delete("NOTE", "username=? and sort=?", new String[]{account, spinner.getSelectedItem().toString()});
                showAll(cursor, 2);
                spinner.setAdapter(adapter);
                spinner.setSelection(pos);
                spinner_show();
            }
        });
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    /****************管理分类方法**************************/
    private void showAlerg_1() {



        alert2.setMessage("新建/修改分类");


        /**************为mSpinner绑定adapter***************************/
        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
        showAll(cursor, 2);
        mSpinner.setAdapter(adapter);

        /**************alert添加view*******************/
        alert2.setView(view);

        /**************新建操作************************/

        alert2.setButton(AlertDialog.BUTTON_POSITIVE, "新建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cursor = db.rawQuery("select * from SORT where username=? and sort=?", new String[]{account, mEditText_1.getText().toString()});
                values.put("username", account);
                values.put("sort", mEditText_1.getText().toString());
                /*******************已存在的分类无须新建************************/
                if (cursor.moveToFirst()) {
                    Toast.makeText(ManageActivity.this, "分类已存在，无须新建", Toast.LENGTH_SHORT).show();
                    cursor.close();
                } else {
                    /**将新建的分类插入到SORT表中**/
                    db.insert("SORT", null, values);
                    cursor.close();
                    /**重新为两个spinner和List绑定数据*****/
                    cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
                    showAll(cursor, 2);
                    spinner.setAdapter(adapter);
                    mSpinner.setAdapter(adapter);
                    spinner_show();

                    Toast.makeText(ManageActivity.this, "新建成功", Toast.LENGTH_SHORT).show();
                }
                showAlerg_1();
            }
        });
        /**************删除分类操作************************/
        alert2.setButton(AlertDialog.BUTTON_NEUTRAL, "删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alert1.setMessage("将删除该分类以及该分类的所有笔记");
                alert1.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("SORT", "username=? and sort=?", new String[]{account, mSpinner.getSelectedItem().toString()});
                        db.delete("NOTE", "username=? and sort=?", new String[]{account, mSpinner.getSelectedItem().toString()});

                        /**重新为两个spinner和List绑定数据*****/
                        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});

                        showAll(cursor, 2);
                        spinner.setAdapter(adapter);
                        mSpinner.setAdapter(adapter);
                        spinner_show();

                        Toast.makeText(ManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
                        if(cursor.moveToFirst())
                            showAlerg_1();
                        else
                            list.setAdapter(null);
                    }
                });
                alert1.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showAlerg_1();
                    }
                });
                alert1.show();
            }
        });
        /**************修改分类操作
         * 因为修改分类名会直接影响两个表，即SORT表和NOTE表，所有必须同时更新这两张表中要修改的SORT的行************************/
             alert2.setButton(AlertDialog.BUTTON_NEGATIVE, "修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                values.put("username", account);
                values.put("sort", mEditText_1.getText().toString());
                db.update("SORT", values, "username=? and sort=?", new String[]{account, mSpinner.getSelectedItem().toString()});
                values.clear();
                cursor=db.rawQuery("select * from NOTE where sort=? and username=?",new String[]{mSpinner.getSelectedItem().toString(),account});
                if(cursor.moveToFirst()){
                    String [] arr1=cursor.getString(4).split(":");
                    values.put("username", account);
                    values.put("sort", mEditText_1.getText().toString());
                    values.put("title",mEditText_1.getText().toString()+":"+arr1[1]);
                    db.update("NOTE", values, "username=? and sort=?", new String[]{account, mSpinner.getSelectedItem().toString()});
                }
                values.clear();
                cursor.close();
                /**重新为两个spinner和List绑定数据*****/
                cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
                showAll(cursor, 2);
                spinner.setAdapter(adapter);
                mSpinner.setAdapter(adapter);
                spinner_show();

                Toast.makeText(ManageActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                showAlerg_1();
            }
        });

        alert2.show();

    }

    /**************绑定数据的方法，该方法与Main2Activity类似*********************/
    private void showAll(Cursor cursor, int index) {
        int s = 0;
        if (!set.isEmpty())
            set.clear();
        if (cursor.moveToFirst()) {
            do {
                set.add(cursor.getString(index));
            } while (cursor.moveToNext());
        }
        mStrings = new String[set.size()];
        Iterator it = set.iterator();
        while (it.hasNext()) {
            mStrings[s++] = it.next().toString();
        }
        adapter = new ArrayAdapter(ManageActivity.this, android.R.layout.simple_list_item_1, mStrings);
        cursor.close();
    }

    /**********************为listview绑定数据*************************/
    private void spinner_show( ) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos=position;
                cursor = db.rawQuery("select * from NOTE where sort=? and username=?", new String[]{spinner.getSelectedItem().toString(), account});
                showAll(cursor, 4);//调用绑定数据方法
                list.setAdapter(adapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    /********************返回上一个页面**********************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            intent=new Intent(ManageActivity.this,Main2Activity.class);
            startActivity(intent);
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
