package com.gultenyigit.chattapp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gultenyigit.demo.custom.CustomActivity;
import com.gultenyigit.model.Conversation;
import com.gultenyigit.utils.Const;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


/*
 * The Class Chat is the Activity class that holds main chat screen.It shows
 * all the conversation  messages between two users and also allows the user to
 * send and receive messages.
 */
public class Chat extends CustomActivity {

	private ArrayList<Conversation> convList;
	private ChatAdapter adp;
	private EditText txt;
	private String buddy;
	private Date lastMsgDate;
	private boolean isRunning;
	private static Handler handler;
	private boolean isCrypted=false;
	private boolean isDecrypted = false;
	private String CryptWord=null;
	private final static String NORMALE_DON_MSG="Sifreleme Kaldirildi";
	private boolean isNormal = false;
	
	
	private final static String SMG = "Sifreli Mesaj Gonderme";
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		
		convList = new ArrayList<Conversation>();
		ListView list = (ListView)findViewById(R.id.list);
		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);
		
		txt = (EditText)findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		
		setTouchNClick(R.id.btnSend);
		
		buddy = getIntent().getStringExtra(Const.EXTRA_DATA);
		
		ActionBar ab = getActionBar();
		
		if(ab != null){
			ab.setTitle(buddy);
		}	
		
		handler = new Handler();
	}
		
	@Override
	protected void onResume(){
		super.onResume();
		isRunning = true;
		loadConversationList();
	}
	@Override
	protected void onPause(){
		super.onPause();
		isRunning = false;
	}
	@Override
	public void onClick(View v){
		super.onClick(v);
		
		if(v.getId() == R.id.btnSend){
			sendMessage();
		}	
		
	}
	/*
	 * Call this method to Send message to opponent.It does nothing if the text
	 * is empty otherwise it creates a ParseObject for Chat message and send it
	 * to Parse server.
	 */
	private void sendMessage(){
		if(txt.getText().length() == 0){
			return;
		}
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(),0);
		
		String s = txt.getText().toString();
		if (isCrypted) {
		   String sifreli = vigenereEncrypt(s, CryptWord);
		   s = sifreli;
		}
		
		
		final Conversation c = new Conversation(s,new Date(),
				UserList.user.getUsername());
		c.setStatus(Conversation.STATUS_SENDING);
		convList.add(c);
		adp.notifyDataSetChanged();
		txt.setText(null);
		
		ParseObject po = new ParseObject("Chat");
		po.put("sender",UserList.user.getUsername());
		po.put("receiver",buddy);
		//po.put("createdAt"," ");
		po.put("message",s);
		po.saveEventually(new SaveCallback(){
			
			@Override
			public void done(ParseException e){
				if(e == null)
					c.setStatus(Conversation.STATUS_SENT);
				else
					c.setStatus(Conversation.STATUS_FAILED);
				adp.notifyDataSetChanged();
			}
		});
	}
	private void sendMessageMy(String mesaj){
		if(mesaj.length() == 0){
			return;
		}
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(),0);
		
		String s = mesaj;
		
		final Conversation c = new Conversation(s,new Date(),
				UserList.user.getUsername());
		c.setStatus(Conversation.STATUS_SENDING);
		convList.add(c);
		adp.notifyDataSetChanged();
		txt.setText(null);
		
		ParseObject po = new ParseObject("Chat");
		po.put("sender",UserList.user.getUsername());
		po.put("receiver",buddy);
		//po.put("createdAt"," ");
		po.put("message",s);
		po.saveEventually(new SaveCallback(){
			
			@Override
			public void done(ParseException e){
				if(e == null)
					c.setStatus(Conversation.STATUS_SENT);
				else
					c.setStatus(Conversation.STATUS_FAILED);
				adp.notifyDataSetChanged();
			}
		});
	}
	
	/*
	 * Load Conversation list from Parse server and save the date of last message
	 * that will be used to load only recent new messages
	 */
	
	private void loadConversationList(){
		ParseQuery<ParseObject> q = ParseQuery.getQuery("Chat");
		
		if(convList.size() == 0)
		{
			//load all messages...
			ArrayList<String> a1 = new ArrayList<String>();
			a1.add(buddy);
			a1.add(UserList.user.getUsername());
			q.whereContainedIn("sender",a1);
			q.whereContainedIn("receiver",a1);
		}
		else{
			//load only  newly received message.
			if(lastMsgDate != null)
				q.whereGreaterThan("createdAt", lastMsgDate);
			q.whereEqualTo("sender", buddy);
			q.whereEqualTo("receiver",UserList.user.getUsername());
		}
		
		q.orderByDescending("createdAt");
		q.setLimit(30);
		q.findInBackground(new FindCallback<ParseObject>(){
			
			@Override
			public void done(List<ParseObject> li,ParseException e){
				if(li != null && li.size()>0){
					for(int i= li.size()-1; i>=0;i--){
						ParseObject po = li.get(i);
						Conversation c = new Conversation(po
								.getString("message"),po.getCreatedAt(),
								po.getString("sender"));
						if (c.getMsg().equals(SMG) && i==li.size()-1){
							alertDiaologKullan();
						}
						if (c.getMsg().equals(NORMALE_DON_MSG) && i==li.size()-1){
							isCrypted=false;
							isDecrypted = false;
							CryptWord = null;
							isNormal = true;
						}
						String msg=null;
						if (isDecrypted && CryptWord!=null){
							msg = vigenereDecrypt(c.getMsg(), CryptWord);
							c.setMsg(msg);
						}
						
						convList.add(c);
						if(lastMsgDate == null
								||lastMsgDate.before(c.getDate()))
							lastMsgDate = c.getDate();
						adp.notifyDataSetChanged();
					}
				}
				handler.postDelayed(new Runnable(){
					
					@Override
					public void run(){
						if(isRunning)
							loadConversationList();
					}
				},1000);
			}

			
		});	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting, menu);
		return super.onCreateOptionsMenu(menu);
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId())
		{
		case android.R.id.home: finish(); break;
		case R.id.vigenere:
				if (item.isChecked()){ 
					item.setChecked(false);
				}else {
					item.setChecked(true);
					alertDiaologKullan();
				}
					Toast.makeText(getApplicationContext(), "Sifreleme modu secildi",
							Toast.LENGTH_SHORT).show();
				return true;
		case R.id.normal:
			if (item.isChecked()){ 
				item.setChecked(false);
			}else {
				item.setChecked(true);
			}
			isCrypted=false;
			isDecrypted=false;
			isNormal =true;
			CryptWord = null;
			sendMessageMy(NORMALE_DON_MSG);
			/*
			 * 
			 * 
			 */
				Toast.makeText(getApplicationContext(), "Normal mod secildi",
						Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public void getOverFlowMenu(){
		try{
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("MenuKey");
			if(menuKeyField != null){
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void alertDiaologKullan(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		alert.setTitle("Þifrele / Þifre Çöz");
		LinearLayout layout =  new LinearLayout(this);
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		
		layout.setLayoutParams(param);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		
		final EditText et = new EditText(this);
		et.setSingleLine();
		alert.setView(et);
		et.setHint("Þifrele/Þifre Çöz");
		
		//ad.setTransformationMethod(PasswordTransformationMethod.getInstance());
		//yazýyý þifreli yapabiliriz üst taraftaki kodla
		
		layout.addView(et);
		
		alert.setView(layout);
		//alert.setMessage("");
		
		alert.setPositiveButton("Þifrele", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String sifre = et.getText().toString();
				isCrypted= true;
				isDecrypted = false;
				isNormal =false;
				CryptWord = sifre;
				sendMessageMy(SMG);
				//Toast.makeText(getApplicationContext(), sifre,
				//		Toast.LENGTH_SHORT).show();
				
			}
		});
		
		alert.setNegativeButton("Þifre Çöz", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String sifrecoz = et.getText().toString();
				isCrypted= false;
				isDecrypted = true;
				isNormal = false;
				CryptWord = sifrecoz;
				//Toast.makeText(getApplicationContext(), sifrecoz, 
				//		Toast.LENGTH_SHORT).show();
				
			}
		});
		
		alert.show();
	}
	
	
	public String vigenereEncrypt(String message,final String key){
        String res = "";
        message = message.toUpperCase();
        for (int i = 0, j = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < 'A' || c > 'Z') continue;
            res += (char)((c + key.charAt(j) - 2 * 'A') % 26 + 'A');
            j = ++j % key.length();
        }
        return res;
	}
	public String vigenereDecrypt(String message ,final String key){
        String res = "";
        message = message.toUpperCase();
        for (int i = 0, j = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < 'A' || c > 'Z') continue;
            res += (char)((c - key.charAt(j) + 26) % 26 + 'A');
            j = ++j % key.length();
        }
        return res;
	}
	

	/*
	 * The Class ChatAdapter is the adapter class for Chat ListView.This
	 * adapter shows the sent or Received Chat message in each list item
	 */
	private class ChatAdapter extends BaseAdapter{
		@Override
		public int getCount(){
			return convList.size();
		}
		@Override
		public Conversation getItem(int arg0){
			return convList.get(arg0);
		}
		@Override
		public long getItemId(int arg0){
			return arg0;
		}
		
		public View getView(int pos, View v, ViewGroup arg2)
		{
			Conversation c = getItem(pos);
			
			if(c.isSent())
				v = getLayoutInflater().inflate(R.layout.chat_item_sent,null);
			else
				v = getLayoutInflater().inflate(R.layout.chat_item_rcv,null);
			
			TextView lbl = (TextView)v.findViewById(R.id.lbl1);
			lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this,
					c.getDate().getTime(),DateUtils.SECOND_IN_MILLIS,
					DateUtils.DAY_IN_MILLIS,0));
			
			lbl = (TextView)v.findViewById(R.id.lbl2);
			lbl.setText(c.getMsg());
			lbl = (TextView)v.findViewById(R.id.lbl3);
			
			if(c.isSent()){
				if(c.getStatus() == Conversation.STATUS_SENT)
					lbl.setText("Gönderildi");
				else if(c.getStatus() == Conversation.STATUS_SENDING)
					lbl.setText("Göneriliyor...");
				else
					lbl.setText("Gönderilemedi");
			}else
				lbl.setText("");
			
			return v;
		}
	}
	
	
}
