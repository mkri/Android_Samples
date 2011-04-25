/**
Copyright 2011, Aman Alam, http://www.sheikhaman.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   **/

package com.aman.samples.dbdemo.helpers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * A helper class that creates the database (copies the one you put into assets folder), 
 * opens it, lets you access the data from it, and closes the database.
 * 
 *  You can freely enhance this easy example to suit your needs.
 *  And in case you end up doing something more efficient, please let me know and update this code.
 * @author Aman Alam
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper{
	
	private static String DB_PATH = "";
	private static String TABLE_NAME = "MyTable";
	private static final String DB_NAME = "DBDemo.sqlite";
	private SQLiteDatabase myDataBase;
	private final Context myContext;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		DB_PATH = "/data/data/"
				+ context.getApplicationContext().getPackageName()
				+ "/databases/";
		try {
			createDataBase();
		} catch (IOException e) {
			Log.e("DatabaseHelper.onCreate",""+e.getMessage());
		}
	}
	
	/**
	 * Creates an empty database on the system and rewrites it with your own database.
	 **/
	public void createDataBase() throws IOException {
		//lets check if a database already exists at the specified location, if it doesn't, lets copy our db
		boolean dbExist = checkDataBase();
		if (!dbExist) {
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				Log.e("DatabaseHelper.createDataBase",""+e.getMessage());
			}
		}
	}
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		} catch (SQLiteException e) {
			Log.e("DatabaseHelper.checkDataBase",""+e.getMessage()+", Cause: "+e.getCause());
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	/**
	 * Copies database from local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transferring byte-streams.
	 * */
	private void copyDataBase() throws IOException {
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	

	/**
	 * Open the database
	 * @throws SQLException
	 */
	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);		
	}

	/**
	 * Close the database if exist
	 */
	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}
	
	/**
	 * Queries the database table for all rows and returns an array containing 'text' column 
	 * @return data[] array that holds all the data found in the 'text' column
	 */
	public String[] getData(){
		String[] data = {"NoText"};
		String[] columns = {"text"};
		try{
			openDataBase();
			Cursor cur = myDataBase.query(TABLE_NAME,columns,null,null,null,null,null);
			cur.moveToFirst();
			data = new String[cur.getCount()];
			for(int i=0;i<cur.getCount();i++){
				data[i] = cur.getString(0);
				cur.moveToNext();
			}
			cur.close();
		    close();
		}catch(Exception ex){
			Log.e("DatabaseHandler.getCategoryJoke",""+ex.getMessage());
		}
		
		return data;
	}
	
	/**
	 * Method of SQLiteOpenHelper that we don't currently need
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {}

	/**
	 * Method of SQLiteOpenHelper that we don't currently need
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	}
}
