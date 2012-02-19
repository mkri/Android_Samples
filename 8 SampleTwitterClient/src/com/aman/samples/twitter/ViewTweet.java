package com.aman.samples.twitter;

import twitter4j.Status;
import twitter4j.StatusUpdate;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewTweet extends Activity {
	Bundle extra;
	Twitter twitter;
	RequestToken requestToken;
	int SIGN_IN_CODE = 140;
	long tweetId;
	String tweet;
	ProgressDialog progressDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_tweet);
		extra = getIntent().getExtras();
		String userName	= extra.getString("userName");
		tweet	= extra.getString("tweet");
		tweetId	= extra.getLong("tweetId");
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Sending Tweet..");
		
		String date		= extra.getString("dates");
		Bitmap img		= (Bitmap)extra.getParcelable("img");
		
		TextView 	userNameTv	= (TextView)findViewById(R.id.view_user_name),
					tweetTv		= (TextView)findViewById(R.id.view_user_tweet),
					dateTv		= (TextView)findViewById(R.id.view_tweet_date);
		ImageView	userImg		= (ImageView)findViewById(R.id.view_user_image);
		
		userNameTv.	setText(userName);
		tweetTv.	setText(tweet);
		dateTv.		setText(date);
		userImg.	setImageBitmap(img);
		
		Linkify.addLinks(tweetTv, Linkify.ALL);
		
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
	
	public void twitterLogin() {
		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
					.setOAuthConsumerKey(this.getResources().getString(R.string.consumerKey))
					.setOAuthConsumerSecret(this.getResources().getString(R.string.consumerSecret));
			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();

			requestToken = twitter.getOAuthRequestToken();
			String authUrl = requestToken.getAuthenticationURL();

			Intent twitterSignInIntent = new Intent(getApplicationContext(),
					TwitterSignIn.class);
			twitterSignInIntent.putExtra("urlToOAuth", authUrl);
			startActivityForResult(twitterSignInIntent, SIGN_IN_CODE);
		} catch (TwitterException tex) {
			Toast.makeText(this, tex.getMessage(), Toast.LENGTH_LONG).show();
			Log.e("twitterLogin; TwitterException -->", "" + tex.getMessage());
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("CODE AND RESULT", "Code" + requestCode + " result: "
				+ resultCode);
		if (requestCode == SIGN_IN_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle receivedData = data.getExtras();
				String verifier = receivedData.getString("verifier");
				try {
					AccessToken accessToken = twitter
							.getOAuthAccessToken(requestToken, verifier);
					String token = accessToken.getToken(), secret = accessToken
							.getTokenSecret();

					if (null != token && null != secret) {
						Log.d("SAVING INFO", "SAVING INFO");
						saveLoginInfo(token, secret);
						SharedPreferences sharedPref = getSharedPreferences(
								"DroidConPrefs", MODE_PRIVATE);
						Log.d("IS SIGNED IN",
								"" + sharedPref.getBoolean("isSignedIn", false));
					} else {
						throw new TwitterException("error after OAuth");
					}
				} catch (TwitterException ex) {
					Log.e("Main.onNewIntent", "" + ex.getMessage());
				}
			} else {
				Log.e("Some Error", "" + resultCode);
			}
		}
	}

	public void saveLoginInfo(String twitterToken, String twitterSecret) {
		Log.d("SAVING", "INFO");
		SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.putString("twitterToken", twitterToken);
		prefEditor.putString("twitterSecret", twitterSecret);
		prefEditor.putBoolean("isSignedIn", true);
		prefEditor.commit();
	}

	public void twitterLogout() {
		SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",
				MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		prefEditor.remove("twitterToken");
		prefEditor.remove("twitterSecret");
		prefEditor.remove("isSignedIn");
		prefEditor.commit();
	}
	
	public void killViewTweet(View v){
		this.finish();
	}
	
	public void goToHome(View v){
		Toast.makeText(this, "take the user to main droidcon app on click of this", Toast.LENGTH_LONG).show();
	}
	
	public void compose(View v){
		Intent compose = new Intent(ViewTweet.this, Compose.class);
		compose.putExtra("isReply",false);
		startActivity(compose);
	}
	
	public void reply(View v){
		Intent compose = new Intent(ViewTweet.this, Compose.class);
		compose.putExtra("isReply", true);
		compose.putExtra("toId", tweetId);
		compose.putExtra("toUser",extra.getString("userName"));
		startActivity(compose);
	}
	
	public void retweet(View v){
		try{
//			if(null == twitter){
				SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
				if(!sharedPref.getBoolean("isSignedIn",false)){
					Toast.makeText(this, "You'll have to login to perform this action. press menu to login and try again",Toast.LENGTH_LONG).show();
				}else{
					String token = sharedPref.getString("twitterToken",null);
					String secret = sharedPref.getString("twitterSecret",null);
					if(null!=token && null!=secret){
						ConfigurationBuilder cb = new ConfigurationBuilder();
						cb.setDebugEnabled(true)
								.setOAuthConsumerKey(this.getResources().getString(R.string.consumerKey))
								.setOAuthConsumerSecret(this.getResources().getString(R.string.consumerSecret))
								.setOAuthAccessToken(token)
								.setOAuthAccessTokenSecret(secret);
						TwitterFactory tf = new TwitterFactory(cb.build());
						twitter = tf.getInstance();
						new NetTask().execute();
					}else{
						throw new TwitterException("token and secret were found to be null");
					}
				}
//			}else{
//				new NetTask().execute();
//			}
		}catch(TwitterException tex){
			Log.e("Update Status; Tiwtter Update",""+tex.getMessage());
		}
	}
		
	class NetTask extends AsyncTask<Void, Void, String> {
		
		@Override
		public void onPreExecute(){
			progressDialog.show();
		}
		
		/**
		 * This method is executed in the background, inside anotehr thread
		 */
		@Override
		protected String doInBackground(Void... params) {
			try{
				twitter4j.Status status = twitter.retweetStatus(tweetId);
				Log.d("Replied","tweet ID of the tweet just updated: "+status.getId());
				return "success";
			}catch(TwitterException tex){
				Log.e("Update Status; Tiwtter Update",""+tex.getErrorMessage());
				if(tex.getStatusCode() == 403){
					return "updateLimit";
				}
			}
			return "";
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
			progressDialog.cancel();
			if(result.equals("updateLimit")){
				Toast.makeText(ViewTweet.this,"Retweet failed due to update limitations. is it a duplicate?",Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ViewTweet.this,"Retweet success",Toast.LENGTH_LONG).show();
			}
		}
	}
	
}
