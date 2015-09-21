package com.gultenyigit.chattapp;

import com.parse.Parse;

import android.app.Application;

public class ChattApp extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	
		Parse.initialize(this, "3Qz3hZj9GgAU1fyBhBjIpalcrA6IELyE5DJrklKP","Chu6rYAvC997qG7uGK2dF8wNoUHX51Bdb1nFF1mv");
	
	}
	
}
