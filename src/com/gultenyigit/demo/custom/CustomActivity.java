package com.gultenyigit.demo.custom;



import com.gultenyigit.chattapp.R;
import com.gultenyigit.utils.TouchEffect;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;



import android.view.View;
import android.view.View.OnClickListener;


/**
 * This is a common activity that all other activities of the app can extend to
 * inherit the common behaviors like implementing a common interface that can be
 *used all child activities
 */

public class CustomActivity extends Activity implements OnClickListener{

	public static final TouchEffect TOUCH = new TouchEffect();
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		
		if(actionBar == null)
		{
			return;
		}
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.icon);
		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.actionbar_bg));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		
	}
	
	public View setTouchNClick(int id){
		View v =setClick(id);
		if(v != null)
			v.setOnTouchListener(TOUCH);
		return v;
	}
	public View setClick(int id){
		View v = findViewById(id);
		if(v!=null)
			v.setOnClickListener(this);
		return v;
	}
	@Override
	public void onClick(View v) {
	
	}

	
}
