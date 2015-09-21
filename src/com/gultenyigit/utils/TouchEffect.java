package com.gultenyigit.utils;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * The Class TouchEffect is the implementation of onTouchListener interface. You
 * can apply this to views mostly Buttons to provide Touch effect and that view
 * must have a valid background.The current implementation simply set Alpha
 * value of view background
 */
public class TouchEffect implements OnTouchListener {

	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			Drawable d = v.getBackground();
			d.mutate();
			d.setAlpha(150);
			v.setBackgroundDrawable(d);
		}
		else if(event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction()== MotionEvent.ACTION_CANCEL){
			Drawable d = v.getBackground();
			d.setAlpha(255);
			v.setBackgroundDrawable(d);
			
		}

		return false;
	}

	
}
