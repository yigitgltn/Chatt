package com.gultenyigit.chattapp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;



import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gultenyigit.demo.custom.CustomActivity;
import com.gultenyigit.utils.Const;
import com.gultenyigit.utils.Utils;
import com.parse.FindCallback;
import com.parse.ParseUser;

/*
 * The Class userList is the Activity class, It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */

public class UserList extends CustomActivity{

	private  ArrayList<ParseUser> uList;
	public static ParseUser user;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list);
		
		ActionBar ab = getActionBar();
		
		if(ab != null){
			ab.setDisplayHomeAsUpEnabled(false);
		}
		updateUserStatus(true);
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		updateUserStatus(false);
		
	}
	@Override
	protected void onResume(){
		super.onResume();
		loadUserList();
	}
	
	private void updateUserStatus(boolean online){
		user.put("online", online);
		user.saveEventually();
	}
	
	private void loadUserList(){
		final ProgressDialog dia = ProgressDialog.show(this, null,
				getString(R.string.alert_loading));
		ParseUser.getQuery().whereNotEqualTo("username",user.getUsername())
			.findInBackground(new FindCallback<ParseUser>() {

				@Override
				public void done(List<ParseUser> li,
						com.parse.ParseException e) {
					dia.dismiss();
					if(li != null){
						if(li.size() == 0)
							Toast.makeText(UserList.this, 
									R.string.msg_no_user_found,
									Toast.LENGTH_SHORT).show();
						uList = new ArrayList<ParseUser>(li);
						ListView list = (ListView)findViewById(R.id.list);
						list.setAdapter(new UserAdapter());
						list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int pos, long arg3) {
								ParseUser pu = uList.get(pos);
								String userName = pu.getUsername();
								startActivity(new Intent(UserList.this,
										Chat.class).putExtra(Const.EXTRA_DATA,
												userName));
								
							}
							
						});
					}
					else{
						Utils.showDialog(UserList.this,
								getString(R.string.err_users)+" "
								+e.getMessage());
						e.printStackTrace();
					}
					
				}
			});
	
	}
	
	
	/*
	 * The Class UserAdapter is the adapter class for User ListView.This
	 * adapter shows the user name and it's only online status for each item
	 */
	
	private class UserAdapter extends BaseAdapter{
		
		@Override
		public int getCount(){
			return uList.size();
		}
		@Override
		public ParseUser getItem(int arg0){
			return uList.get(arg0);
		}


		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@SuppressLint("InflateParams") @Override
		public View getView(int pos, View v, ViewGroup arg2) {
			if(v == null)
				v = getLayoutInflater().inflate(R.layout.chat_item, null);
			
			ParseUser c = getItem(pos);
			TextView lbl = (TextView)v;
			lbl.setText(c.getUsername());
			lbl.setCompoundDrawablesWithIntrinsicBounds(
					c.getBoolean("online") ? R.drawable.ic_online
							:R.drawable.ic_offline, 0, R.drawable.arrow, 0);
			return v;
		}
		
		
	}
	
	
	
}
