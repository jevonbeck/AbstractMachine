package org.ricts.abstractmachine.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import org.ricts.abstractmachine.R;
import org.ricts.abstractmachine.ui.network.MemoryPortMultiplexerView;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        MemoryPortMultiplexerView muxView = (MemoryPortMultiplexerView) findViewById(R.id.mux);
        if(muxView != null){
            muxView.setSelectWidth(1);
        }
    }

}
