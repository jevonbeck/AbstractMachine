package org.ricts.abstractmachine.ui.contentprovider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import org.ricts.abstractmachine.components.compute.isa.OperandInfo;

import java.util.Set;

/**
 * Created by Jevon on 16/02/2017.
 */

public class OperandContentProvider extends ContentProvider {
    private static final String TAG = "OperandContentProvider";

    private static final String [] COL_NAMES = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
    };

    private OperandInfo opInfo;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(COL_NAMES);

        if(opInfo != null){
            String query = uri.getLastPathSegment().toUpperCase();

            int row_count = 0;
            for(String mneumonic : opInfo.validMneumonics()) {
                if(query.equals("") || (mneumonic.startsWith(query) && !mneumonic.equals(query))) {
                    Object [] columnData = new Object[COL_NAMES.length];
                    columnData[0] = row_count++;
                    columnData[1] = mneumonic;
                    columnData[2] = mneumonic;
                    cursor.addRow(columnData);
                }
            }
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public void setOpInfo(OperandInfo info) {
        opInfo = info;
    }
}
