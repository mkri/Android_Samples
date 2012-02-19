package com.aman.samples.twitter;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Compose extends Activity {
	/** Called when the activity is first created. */
	String textToTweet="";
	Twitter twitter;
	RequestToken requestToken;
	int	SIGN_IN_CODE	= 140;
	long toId;
	boolean isReply;
	ProgressDialog progressDialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		Bundle extras = getIntent().getExtras();
		isReply = extras.getBoolean("isReply");
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Sending Tweet..");
		
		final EditText tweetText = (EditText)findViewById(R.id.compose_tweet_text);
		final TextView preview = (TextView)findViewById(R.id.preview_text);
		final TextView limitText = (TextView)findViewById(R.id.char_count);
	
		final TextWatcher tweetTextWatcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				limitText.setText(""+(128-tweetText.getText().length()));
				if(tweetText.getText().length()>0){
					preview.setText(tweetText.getText()+" #DevFestX");
					textToTweet = tweetText.getText()+" #DevFestX";
				}else{
					preview.setText("");
					textToTweet = "";
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {}
			
			@Override
			public void afterTextChanged(Editable arg0) {}
		};
		tweetText.addTextChangedListener(tweetTextWatcher);
		
		if(isReply){
			tweetText.setText("@"+extras.getString("toUser"));
			toId = extras.getLong("toId");
		}
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
				if(("denied").equals(verifier)){
					Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.denied), Toast.LENGTH_SHORT).show();
				}else{
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

	public void sendTweet(View v){
		try{
//			if(null == twitter){
				SharedPreferences sharedPref = getSharedPreferences("DroidConPrefs",MODE_PRIVATE);
				if(!sharedPref.getBoolean("isSignedIn",false)){
					Log.d("NOT LOGGED IN","NOT LOGGED IN");
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
					}else{
						throw new TwitterException("token and secret were found to be null");
					}
					
					
					//Now sending tweet
					new NetTask().execute();
				}
//			}else{
//				new NetTask().execute();
//			}
		}catch(TwitterException tex){
			Log.e("Update Status; Tiwtter Update",""+tex.getMessage());
		}
	}
	
	public void goToHome(View v){
		Toast.makeText(this, "take the user to main droidcon app on click of this", Toast.LENGTH_LONG).show();
	}
	
	public void killComposeTweet(View v){
		this.finish();
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
				
				if(isReply){
					StatusUpdate statusToUpdate = new StatusUpdate(textToTweet);
					statusToUpdate.setInReplyToStatusId(toId);
					twitter4j.Status status = twitter.updateStatus(statusToUpdate);
					Log.d("Replied","tweet ID of the tweet just updated: "+status.getId());
				}else{
					twitter4j.Status status = twitter.updateStatus(textToTweet);
					Log.d("Replied","tweet ID of the tweet just updated: "+status.getId());
				}
				textToTweet = "";
				return "success";
			}catch(TwitterException tex){
				Log.e("Update Status; Tiwtter Update",""+tex.getMessage());
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
				Toast.makeText(Compose.this,"Status update failed due to update limitations. is it a duplicate?",Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(Compose.this,"Status Updated",Toast.LENGTH_LONG).show();
				Compose.this.finish();
			}
		}
	}
	
	public void shareTweet(View v){
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/html");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToTweet);
		startActivity(Intent.createChooser(sharingIntent,"Share using"));
	}
}
