package com.ayuan.guaguale;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.ayuan.guaguale.GuaGuaKa.OnCompletedListener;

public class MainActivity extends Activity {

	private GuaGuaKa mGuaGuaka;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mGuaGuaka = (GuaGuaKa) findViewById(R.id.id_card);
		mGuaGuaka.setOnCompletedListener(new OnCompletedListener() {
			
			@Override
			public void onCompleted() {
				Toast.makeText(MainActivity.this, "用户已经刮的差不多了", Toast.LENGTH_SHORT).show();
			}
		});
	}

}
