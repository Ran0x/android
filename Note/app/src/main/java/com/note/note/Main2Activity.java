package com.note.note;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;


public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /***************成员变量声明部分****************************/
    private AlertDialog alert;
    private ListView list;
    private Spinner spinner;
    private DBOpen dbhelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private Intent intent;
    private ArrayAdapter<String> adapter;
    private ContentValues values;
    private String[] mStrings;
    private LinkedHashSet<String> set;
    private LinkedHashMap<String, String> map;
    private String account;
    private EditText search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        /***************显示ToolBar******************************************/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * 组件定义部分
         */
        spinner = (Spinner) findViewById(R.id.spinner_check);//分类框
        list = (ListView) findViewById(R.id.list);//相应分类的笔记列表
        search = (EditText) findViewById(R.id.search);//搜索框
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);//侧边栏
        /***
         * 数据库定义部分
         */
        dbhelper = new DBOpen(Main2Activity.this, "Note.db", null, 2);
        db = dbhelper.getWritableDatabase();
        values = new ContentValues();
        /**
         * 数据类型定义部分
         */
        map = new LinkedHashMap<>();//用于搜索框
        set = new LinkedHashSet<>();//用于给adapter设置内容
        alert = new AlertDialog.Builder(Main2Activity.this).create();//用于提示框

        /**
         * 获取目前登录的用户名
         */
        cursor = db.rawQuery("select * from REMEMBER where state=?", new String[]{"true"});
        if (cursor.moveToFirst())
            account = cursor.getString(0);


        /***
         * 为spinner配置adapter
         */
        cursor = db.rawQuery("select * from SORT where username=?", new String[]{account});
        showAll(cursor, 2);//设置adapter
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /*************************spinner响应事件，为list显示响应分类的笔记********/
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cursor = db.rawQuery("select * from NOTE where sort=?", new String[]{spinner.getSelectedItem().toString()});
                showAll(cursor, 4);
                list.setAdapter(adapter);//为list设置adapter,显示相应笔记内容
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        /*************************list为空时友情提示***************************/
        TextView textView=(TextView)findViewById(R.id.noData1);
        list.setEmptyView(textView);

        /***搜索框自动响应事件
         * *只要输入相应的笔记title，只要数据库存在某成员的笔记title，则直接显示在Listview中
         * */
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //获取相应title的笔记
                String sql  = "select * from " +"NOTE"+
                        " where username=? and title like ?";
                cursor=db.rawQuery(sql,new String[]{account,"%"+search.getText().toString()+"%"});
                int i = 0;
                if (!map.isEmpty()) map.clear();
                if (cursor.moveToFirst()) {
                    do {
                        map.put(cursor.getString(4), cursor.getString(3));
                    } while (cursor.moveToNext());
                    mStrings = new String[map.size()];
                    Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> me = iterator.next();
                        mStrings[i++] = me.getKey();
                    }
                }
                adapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_1, mStrings);
                list.setAdapter(adapter);//设置Listview的adapter
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        /**
         * 为list(ListView)设置item点击响应即点击编辑笔记事件
         */
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!map.isEmpty()){
                    cursor = db.rawQuery("select * from NOTE where title=? and sort=?",
                            new String[]{list.getItemAtPosition(position).toString(),
                                    map.get(list.getItemAtPosition(position).toString())});
                    if (cursor.moveToFirst()) {
                        values.put("flag", "true");/**需要编辑的笔记设置其flag为true，
                         由于boolean值是在数据库中为0 1使用adb shell检验时容易看错，故直接使用String*/
                        db.update("NOTE", values, "title=? and sort=?",
                                new String[]{list.getItemAtPosition(position).toString(),
                                map.get(list.getItemAtPosition(position).toString())});//更新其flag值
                        values.clear();
                        cursor.close();
                    }
                }
                else {
                    cursor.close();
                    cursor = db.rawQuery("select * from NOTE where title=? and sort=?",
                            new String[]{list.getItemAtPosition(position).toString(),
                                    spinner.getSelectedItem().toString()});
                    if (cursor.moveToFirst()) {
                        values.put("flag", "true");/**
                         需要编辑的笔记设置其flag为true，
                         由于boolean值是在数据库中为0 1使用adb shell检验时容易看错，故直接使用String*/
                        db.update("NOTE", values, "title=? and sort=?",
                                new String[]{list.getItemAtPosition(position).toString(),
                                spinner.getSelectedItem().toString()});//更新其flag值
                        values.clear();
                        cursor.close();
                    }
                }
                intent = new Intent(Main2Activity.this, CreateActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /***侧边栏DrawerLayout功能
        /**ActionBarDrawerToggle 是DrawerLayout.DrawerListener的实现，该DrawerLayout由Toobar和NavigationView组成
         * */


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /************************
         * 浮动按钮，单击跳转为新建笔记页面
         *
         */

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor = db.rawQuery("select * from NOTE where flag=?", new String[]{"true"});
                if (cursor.moveToFirst()) {
                    /*********当点击的是新建笔记按钮时，必须将所有笔记的编辑标记改为false****/
                    values.put("flag", "false");
                    db.update("NOTE", values, "flag=?", new String[]{"true"});
                    values.clear();
                    cursor.close();
                }
                intent = new Intent(Main2Activity.this, CreateActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /******************侧边栏菜单生成*****************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }
    /*****************ToolBar点击Item响应事件**********************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.noteall) {
            intent = new Intent(Main2Activity.this, ManageActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /*****************侧边栏菜单点击响应事件***************************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        /*************修改密码item*******************/
        if (id == R.id.modify_pass) {
            alert.setMessage("点击修改密码");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Main2Activity.this, ForgetActivity.class);
                    startActivity(intent);

                }
            });
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
            return true;
            /************注销帐号item**********************/
        } else if (id == R.id.zhuxiao) {
            alert.setMessage("注销帐号！");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    zhuxiaoOrOUT();
                    intent = new Intent(Main2Activity.this, LoginActivity.class);

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
            return true;
            /*************退出item******************/
        } else if (id == R.id.out) {
            alert.setMessage("确定退出？");
            alert.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    zhuxiaoOrOUT();
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                    System.exit(0);
                    finish();

                }
            });
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
            return true;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * 绑定数据，通过给Set增加元素，再遍历给数组，直接给adapter绑定数据，相对于list的好处是不重复元素
     * 我们知道如果相同标题相同分类的笔记只有一种操作，就是编辑笔记
     */
    private void showAll(Cursor cursor, int index) {
        int s = 0;
        /*************先清空set******************/
        if (!set.isEmpty())
            set.clear();
        /*************add操作********************/
        if (cursor.moveToFirst()) {
            do {
                set.add(cursor.getString(index));
            } while (cursor.moveToNext());
        }
        /**************遍历动作，在这里new 数组好处可以不浪费内存空间*********************/
        mStrings = new String[set.size()];
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            mStrings[s++] = it.next().toString();
        }
        /**************绑定数据操作*****************************/
        adapter = new ArrayAdapter<String>(Main2Activity.this, android.R.layout.simple_list_item_1, mStrings);
        cursor.close();

    }

    /*********************重写onDestroy，在finish()前关闭数据库连接*********************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();

    }


    private void zhuxiaoOrOUT() {

        cursor = db.rawQuery("select * from REMEMBER where state=?", new String[]{"true"});
        if (cursor.moveToFirst()) {
            /**注销/退出时必须将登录状态设为false**/
            values.put("state", "false");
            db.update("REMEMBER", values, "username=?", new String[]{cursor.getString(0)});
            values.clear();
            cursor.close();
        }
        cursor = db.rawQuery("select * from NOTE where flag=?", new String[]{"true"});
        /**************注销/退出时编辑标记要设为false，登录状态设为false***************/
        if (cursor.moveToFirst()) {

            values.put("flag", "false");
            db.update("NOTE", values, "flag=?", new String[]{"true"});
            values.clear();
            cursor.close();
        }
    }
    /*** ***************按返回键返回桌面，不销毁程序*****************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

