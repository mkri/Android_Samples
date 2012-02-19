package com.aman.samples.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PublicSearch extends Activity {
	String[] tweetsArr 	= new String[0];
	long[] tweetIds		= new long[0];
	String[] userNames 	= new String[0];
	String[] dates 		= new String[0];
	Bitmap[] imgs		= new Bitmap[0];
	int	SIGN_IN_CODE	= 140;
	ListView tweetList;
	Twitter twitter,twitter_tweet;
	RequestToken requestToken;
	CountDownTimer countdownTimer;
	ProgressDialog progressDialog;
	ProgressBar progressBar;
	boolean isRefresh = false;
	TweetListAdapter tweetListAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tweetList = (ListView) findViewById(R.id.home_tweet_list);
		tweetList.setCacheColorHint(0);
		tweetList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos,
					long id) {
				Log.d("POSIION CLICKED",""+pos);
				Intent viewTweet = new Intent(PublicSearch.this,ViewTweet.class);
				viewTweet.putExtra("userName",userNames[pos]);
				viewTweet.putExtra("tweet",tweetsArr[pos]);
				viewTweet.putExtra("tweetId",tweetIds[pos]);
			Log.d("TweetID, while sending",""+tweetIds[pos]);
				viewTweet.putExtra("dates",dates[pos]);
				viewTweet.putExtra("img",imgs[pos]);
				startActivity(viewTweet);
			}
			
		});
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Refreshing..");
		progressDialog.setCancelable(false);
		progressBar = (ProgressBar)findViewById(R.id.home_progress_bar);
		new NetTask().execute();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.clear();
		MenuInflater inflater = getMenuInflater();
	    SharedPreferences sharedPrefs = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
	    boolean isSignedIn = sharedPrefs.getBoolean("isSignedIn", false);
	    Log.d("IS SIGNED IN",""+isSignedIn);
	    if(isSignedIn){
	    	inflater.inflate(R.menu.signed_in, menu);
	    }else{
	    	inflater.inflate(R.menu.signed_out, menu);
	    }
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.sign_in:
	    	Log.d("Twitter","SignIn");
	    	twitterLogin();
	        return true;
	    case R.id.sign_out:
	    	Log.d("Twitter","SignOut");
	    	twitterLogout();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void twitterLogin(){
			try{
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
				  .setOAuthConsumerKey(this.getResources().getString(R.string.consumerKey))
				  .setOAuthConsumerSecret(this.getResources().getString(R.string.consumerSecret));
				TwitterFactory tf = new TwitterFactory(cb.build());
				twitter_tweet = tf.getInstance();
				
				requestToken = twitter_tweet.getOAuthRequestToken();
				String authUrl = requestToken.getAuthenticationURL();
				
				Intent twitterSignInIntent = new Intent(getApplicationContext(),TwitterSignIn.class);
				twitterSignInIntent.putExtra("urlToOAuth", authUrl);
				startActivityForResult(twitterSignInIntent, SIGN_IN_CODE);
			}catch(TwitterException tex){
				Toast.makeText(this, tex.getMessage(), Toast.LENGTH_LONG).show();
				Log.e("twitterLogin; TwitterException -->",""+tex.getMessage());
			}
	}
	
	public void onActivityResult(int requestCode, int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.d("CODE AND RESULT","Code"+requestCode+" result: "+resultCode);
		if(requestCode == SIGN_IN_CODE){
			if(resultCode == RESULT_OK){
				Bundle receivedData = data.getExtras();
				String verifier = receivedData.getString("verifier");
				
				if(("denied").equals(verifier)){
					Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.denied), Toast.LENGTH_SHORT).show();
				}else{
					try {
						AccessToken accessToken = twitter_tweet.getOAuthAccessToken(requestToken,
								verifier);
						String 	token = accessToken.getToken(),
								secret = accessToken.getTokenSecret();
						
						if(null!=token && null!=secret){
							Log.d("SAVING INFO","SAVING INFO");
							saveLoginInfo(token,secret);
							SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
							Log.d("IS SIGNED IN",""+sharedPref.getBoolean("isSignedIn",false));
						}else{
							throw new TwitterException("error after OAuth");
						}
					} catch (TwitterException ex) {
						Log.e("Main.onNewIntent", "" + ex.getMessage());
					}
				}
			}else{
				Log.e("Some Error",""+resultCode);
			}
		}
	}
	
	public void saveLoginInfo(String twitterToken, String twitterSecret){
		Log.d("SAVING","INFO");
		SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putString("twitterToken", twitterToken);
		prefEditor.putString("twitterSecret", twitterSecret);
		prefEditor.putBoolean("isSignedIn", true);
		prefEditor.commit();
	}
	
	public void twitterLogout(){
		SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.remove("twitterToken");
		prefEditor.remove("twitterSecret");
		prefEditor.remove("isSignedIn");
		prefEditor.commit();
	}
	
	public String getDateDiff(Date createdDate) {
		String diff = "nothing set";
		Date currentDate = new Date(); // Getting current date

		double createdAtMillis = createdDate.getTime(); // converting created date to milliseconds
		double currentAtMillis = currentDate.getTime(); // converting todays date to milliseconds
		double difference = currentAtMillis - createdAtMillis; // calculating differences in milliseconds

		double seconds = (double) difference / 1000; // converting millisecond's difference to seconds

		if (seconds > 60) {
			int minutes = (int) seconds / 60; // converting seconds to minutes
			if (minutes > 60) {
				int hours = minutes / 60; // converting minutes to hours
				if (hours > 24) {
					int days = hours / 24; // converting hours to days
					if (days > 2) {
						diff = "on "
								+ 
								getMonthName(createdDate.getMonth()) + " "
								+ createdDate.getDate() + ", "
								+ (1900 + createdDate.getYear());// more than 2 days old, we write the date itself
					} else {
						diff = "about " + 
								(days == 1 ? days + " day ago" : days
										+ " days ago"); // if under 2 days ago, then we write number of days
					}
				} else {
					diff = "about " + 
								(hours==1 ? hours + " hour ago" : hours 
											+ " hours ago"); // if under one day, we display the hours
				}
			} else {
				diff = "about " + minutes + " minutes ago"; // if under one hour, we display the minutes
			}
		} else {
			diff = "about " + (int) seconds + " seconds ago"; // if under one minute, we display the seconds
		}

		return diff;
	}
	
	String getMonthName(int monthNumber) {
		switch (monthNumber) {
		case 1:
			return "January";
		case 2:
			return "February";
		case 3:
			return "March";
		case 4:
			return "April";
		case 5:
			return "May";
		case 6:
			return "June";
		case 7:
			return "July";
		case 8:
			return "August";
		case 9:
			return "September";
		case 10:
			return "October";
		case 11:
			return "November";
		case 12:
			return "december";
		default:
			return "";
		}
	}

	/**
	 * Custom Adapter class for the ListView containing tweets
	 * 
	 * @author aman
	 */
	class TweetListAdapter extends BaseAdapter {

		TextView userName, tweetText, dateTime;
		ImageView userImg;

		/**
		 * returns the count of elements in the Array that is used to draw the
		 * text in rows
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return tweetsArr.length;
		}

		/**
		 * returns the text of the row that was clicked (not Implemented at this
		 * point)
		 * 
		 * @param position
		 *            The position of the row that was clicked (0-n)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * returns the ID of the item that was clicked in the row (not
		 * implemented at this point)
		 * 
		 * @param position
		 *            The position of the row that was clicked (0-n)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		int pos;
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			pos = position;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.list_row, parent, false);
			}
			userName 	= (TextView) row.findViewById(R.id.user_name);
			tweetText 	= (TextView) row.findViewById(R.id.tweet_text);
			dateTime 	= (TextView) row.findViewById(R.id.date_time);
			userImg		= (ImageView)row.findViewById(R.id.user_image);
			
			userName.setText(userNames[position]);
			tweetText.setText(tweetsArr[position]);
			dateTime.setText(dates[position]);
			userImg.setImageBitmap(imgs[position]);

			return row;
		}
	}
	
	Bitmap loadImageFromWeb(String imgUrl){
		Bitmap bmp;
		try{
		URL url = new URL(imgUrl);
		Object content = url.getContent();
		InputStream is = (InputStream) content;
		
			bmp = BitmapFactory.decodeStream(is);
			
		}catch(OutOfMemoryError oumEx){
			Log.e("IMAGE SIZE TOO LARGE","For->"+imgUrl);
			bmp = null;
		}catch(IOException ioex){
			Log.e("SOME PROBLEM WITH IMAGE","For->"+imgUrl);
				bmp = null;
		}
		
		return bmp;
	}
	
	
	/**
	 * AsyncTask class that handles all the network activity in the background.
	 * 
	 * @author aman
	 * 
	 */
	class NetTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute(){
			if(isRefresh){
				progressDialog.show();
			}
		}
		/**
		 * This method is executed in the background, inside anotehr thread
		 */
		@Override
		protected String doInBackground(Void... params) {
			String results = getTweets();
			return results;
		}

		/**
		 * This method is executed on the UI Thread, when the task is canceled
		 */
		@Override
		protected void onCancelled() {		}

		/**
		 * This method is executed on the UI Thread, when the AsyncTask finishes
		 * working
		 */
		@Override
		protected void onPostExecute(String result) {
			if(result.equals("success")){
				if(isRefresh){
					if(null == tweetListAdapter){
						tweetListAdapter= new TweetListAdapter();
						tweetList.setAdapter(tweetListAdapter);
					}else{
						tweetListAdapter.notifyDataSetChanged();
					}
					progressDialog.hide();
				}else{
					tweetListAdapter = new TweetListAdapter();
					tweetList.setAdapter(tweetListAdapter);
					progressBar.setVisibility(View.GONE);
				}
			}else{
				progressDialog.hide();
				progressBar.setVisibility(View.GONE);
				Toast.makeText(PublicSearch.this,"Some error occured with Twitter",Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public String getTweets() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(this.getResources().getString(R.string.consumerKey))
		  .setOAuthConsumerSecret(this.getResources().getString(R.string.consumerSecret));
//		  .setOAuthAccessToken(this.getResources().getString(R.string.accessToken))
//		  .setOAuthAccessTokenSecret(this.getResources().getString(R.string.accessTokenSecret));
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
		
		Query query = new Query("devfest");
		QueryResult results;
		try {
			results = twitter.search(query);
			List<Tweet> tweets = results.getTweets();
			Tweet singleTweet;
			tweetsArr = new String[tweets.size()];
			tweetIds = new long[tweets.size()];
			userNames = new String[tweets.size()];
			dates = new String[tweets.size()];
			imgs = new Bitmap[tweets.size()];
			for (int i = 0; i < tweets.size(); i++) {
				singleTweet 	= tweets.get(i);
				
				userNames[i] 	= singleTweet.getFromUser();
				tweetsArr[i]	= singleTweet.getText();
				tweetIds[i]		= singleTweet.getId();
			Log.d("TweetID, while fething tweets",""+tweetIds[i]+" Originally fetched: "+singleTweet.getId());
				dates[i]		= getDateDiff(singleTweet.getCreatedAt());
				imgs[i]			= loadImageFromWeb(singleTweet.getProfileImageUrl());
			}
			return "success";
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			Log.e("TWITTER ERROR"," "+e+" || "+e.getStatusCode()+" || "+e.getErrorMessage());
			return "error";
		}
	}
	
	public void goToHome(View v){
		Toast.makeText(this, "take the user to main droidcon app on click of this", Toast.LENGTH_LONG).show();
	}
	
	public void compose(View v){
		Intent compose = new Intent(PublicSearch.this, Compose.class);
		compose.putExtra("isReply",false);
		startActivity(compose);
	}
	
	public void refresh(View v){
		isRefresh = true;
		new NetTask().execute();
	}
	
	public void onDestroy(){
		super.onDestroy();
		progressDialog.cancel();
	}
}