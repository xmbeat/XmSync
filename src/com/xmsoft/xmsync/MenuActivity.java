package com.xmsoft.xmsync;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.*;

public class MenuActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
	}
	
	public void menuClick(View view){
		Intent intent = new Intent();
		if (view.getId() == R.id.btn_pad){
			intent.putExtras(getIntent().getExtras());
			intent.setClass(this, MouseActivity.class);
			
		}else if (view.getId() == R.id.btn_folder){
			intent.putExtras(getIntent().getExtras());
			intent.setClass(this,FileViewActivity.class);
		}else{
			return;
		}
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

}
