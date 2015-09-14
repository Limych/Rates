/*
 * Copyright (c) 2015 Andrey “Limych” Khrolenok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.khrolenok.rates;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.List;

import trikita.log.Log;

public class ExRatesProvider extends ContentProvider {
    public static final String AUTHORITY = Settings.APP_ID + ".provider";
    public static final String EXRATES_TABLE = "exrates";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EXRATES_TABLE);

    public static final int EXRATES = 1;
    public static final int EXRATES_GROUPS = EXRATES;
    public static final int EXRATES_CURRENCIES = 2;
    public static final int EXRATES_GOODS = 3;
    public static final int EXRATES_RATES = 4;
    
    private ExRatesDBHandler exRatesDB;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, EXRATES_TABLE, EXRATES_GROUPS);
        uriMatcher.addURI(AUTHORITY, EXRATES_TABLE + "/*", EXRATES_CURRENCIES);
        uriMatcher.addURI(AUTHORITY, EXRATES_TABLE + "/*/*", EXRATES_GOODS);
        uriMatcher.addURI(AUTHORITY, EXRATES_TABLE + "/*/*/*", EXRATES_RATES);
    }

    @Override
    public boolean onCreate() {
        exRatesDB = new ExRatesDBHandler(getContext(), null, null, 1);
        return false;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase sqlDB = exRatesDB.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)) {
            case EXRATES:
                id = sqlDB.insert(ExRatesDBHandler.TABLE_EXRATES,
                        null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(EXRATES_TABLE + "/" + id);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ExRatesDBHandler.TABLE_EXRATES);

        final List<String> segments = uri.getPathSegments();
        final String segment_group = segments.get(1);
        final String segment_currency = segments.get(2);
        final String segment_good = segments.get(3);

        switch (uriMatcher.match(uri)) {
            case EXRATES_GROUPS:
                break;
            case EXRATES_CURRENCIES:
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_GROUP + "=" + segment_group);
                break;
            case EXRATES_GOODS:
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_GROUP + "=" + segment_group);
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_CURRENCY + "=" + segment_currency);
                break;
            case EXRATES_RATES:
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_GROUP + "=" + segment_group);
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_CURRENCY + "=" + segment_currency);
                queryBuilder.appendWhere(ExRatesDBHandler.COLUMN_GOOD + "=" + segment_good);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(exRatesDB.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqlDB = exRatesDB.getWritableDatabase();
        int rowsUpdated;

        switch (uriMatcher.match(uri)) {
            case EXRATES:
                rowsUpdated = sqlDB.update(ExRatesDBHandler.TABLE_EXRATES,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqlDB = exRatesDB.getWritableDatabase();
        int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case EXRATES:
                rowsDeleted = sqlDB.delete(ExRatesDBHandler.TABLE_EXRATES,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }



    private class ExRatesDBHandler extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "exratesDB.db";

        public static final String TABLE_EXRATES = "exrates";

        public static final String COLUMN_GROUP = "group";
        public static final String COLUMN_CURRENCY = "currency";
        public static final String COLUMN_GOOD = "good";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_FACE_VALUE = "face_value";
        public static final String COLUMN_BID_INIT = "bid_init";
        public static final String COLUMN_BID_HIGH = "bid_high";
        public static final String COLUMN_BID_LOW = "bid_low";
        public static final String COLUMN_BID_LAST = "bid_last";

        public ExRatesDBHandler(Context context, String name,
                           SQLiteDatabase.CursorFactory factory, int version) {
            super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        }

        private static final String DATABASE_CREATE =
                "CREATE TABLE if NOT EXISTS " + TABLE_EXRATES + " (" +
                        COLUMN_GROUP + " TEXT," +
                        COLUMN_CURRENCY + " TEXT," +
                        COLUMN_GOOD + " TEXT," +
                        " UNIQUE (" + COLUMN_GROUP +", " + COLUMN_CURRENCY + ", " + COLUMN_GOOD + "))";

        @Override
        public void onCreate(SQLiteDatabase db) {
            if( BuildConfig.DEBUG ) Log.w(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if( BuildConfig.DEBUG ) Log.w("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXRATES);
            onCreate(db);
        }
    }

}
