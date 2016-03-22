package com.delacrmi.persistences;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by miguel on 07/10/15.
 */
public class ConnectSQLite extends SQLiteOpenHelper{
    //Catalog tablesCreater
    protected static List<String> tablesCreater;
    protected static List<String> tablesNames;
    protected static boolean validate = true;
    public Object entitiesBackup;

    {
        if((tablesCreater == null || tablesNames == null) && validate)
            throw new  NullPointerException();
    }

    public ConnectSQLite(Context context, String DBName,
                         CursorFactory factory, int version) {
        super(context, DBName, factory, version);

    }

    private void createTables(SQLiteDatabase db) {
        for (String value: tablesCreater) {
            Log.d("creating", value);
            db.execSQL(value);
        }
    }

    private void dropTables(SQLiteDatabase db){
        for (String value:tablesNames) {
            Log.d("creating", value);
            db.execSQL("drop table if exists "+value);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("creating", "Creando");
        beforeToCreate(db);
        createTables(db);
        afterToCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.d("creating", "Actualizando");
        beforeToUpdate(db);
        dropTables(db);
        createTables(db);
        afterToUpdate(db);
    }

    protected void setEntitiesBackup(Object entitiesBackup){
        this.entitiesBackup = entitiesBackup;
    }
    protected Object getEntitiesBackup(){
        return entitiesBackup;
    }

    protected void beforeToUpdate(SQLiteDatabase db){}
    protected void afterToUpdate(SQLiteDatabase db){}
    protected void beforeToCreate(SQLiteDatabase db){}
    protected void afterToCreate(SQLiteDatabase db){}
}
