package com.example.gpstracking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AndroidGPSTrackingActivity extends Activity {
	
	Button btnShowLocation,btnonsubmit;
	EditText addressView1;
	EditText locView2;
	EditText locView6;
	TextView locView;
	TextView locView5;
	TextView addressView;

	boolean flag;

	private ProgressDialog pDialog;

	private static String url_lodge = "http://www.inagtech.co.in/sos/location.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	JSONParser jsonParser = new JSONParser();

	// GPSTracker class
	GPSTracker gps;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
		addressView1 = (EditText) findViewById(R.id.addressView);
		locView2 = (EditText) findViewById(R.id.locView);
		locView6 = (EditText) findViewById(R.id.locView5);
		addressView = (TextView) findViewById(R.id.addressView);
		locView = (TextView) findViewById(R.id.locView);
		locView5= (TextView) findViewById(R.id.locView5);
		btnonsubmit = (Button)findViewById(R.id.submit1);


		// show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {		
				// create class object
		        gps = new GPSTracker(AndroidGPSTrackingActivity.this);

				// check if GPS enabled		
		        if(gps.canGetLocation()){
		        	
		        	double latitude = gps.getLatitude();
		        	double longitude = gps.getLongitude();
					String lat = "Lat:"+latitude;
					String Long = "Long:"+longitude;
					locView.setText(lat);
					locView5.setText(Long);
					LocationAddress.getAddressFromLocation(latitude, longitude,
							getApplicationContext(), new GeocoderHandler());
		        	
		        	// \n is for new line
		        	Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();	
		        }else{
		        	// can't get location
		        	// GPS or Network is not enabled
		        	// Ask user to enable GPS/network in settings
		        	gps.showSettingsAlert();
		        }
				
			}
		});

		btnonsubmit.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View view)
			{

				new Lodge().execute();
			}
		});
    }



	class Lodge extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AndroidGPSTrackingActivity.this);
			pDialog.setMessage("Lodging..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}


		protected String doInBackground(String... args) {
			String address = addressView1.getText().toString();
			String latitude = locView2.getText().toString();
			String longitude = locView6.getText().toString();

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("address", address));
			params.add(new BasicNameValuePair("latitude", latitude));
			params.add(new BasicNameValuePair("longitude", longitude));

			// getting JSON Object
			// Note that create product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_lodge,
					"POST", params);

			// check log cat fro response
			Log.d("Create Response", json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1)
				{

					flag = true;
    				/*Intent i3 = new Intent(getApplicationContext(), ThirdActivity.class);

    				startActivity(i3);
    				Toast.makeText(getApplicationContext(), "Complaint has been lodged.", Toast.LENGTH_SHORT).show();

    				// closing this screen
    				finish();*/
				} else {
					flag = false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url)
		{
			pDialog.dismiss();
			if(flag==true)
			{
				Intent i3 = new Intent(getApplicationContext(), AndroidGPSTrackingActivity.class);

				startActivity(i3);
				Toast.makeText(getApplicationContext(), "Complaint has been lodged.", Toast.LENGTH_SHORT).show();
			}
			else
			{
				String msg = "Submittion Failed ! Check your internet connection!";
				Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
				// dismiss the dialog once done
			}

		}

	}


	public class GeocoderHandler extends Handler
	{
		@Override
		public void handleMessage(Message message) {
			String locationAddress;
			String address=new String();
			switch (message.what) {
				case 1:
					Bundle bundle = message.getData();
					locationAddress = bundle.getString("address");
					break;
				default:
					locationAddress = null;
			}
			for(String sl: locationAddress.split("Address:\n"))
			{
				if(sl.contains("Latitude"))
				{

				}
				else
				{
					address = address+sl;
					if(address.contains("null"))
					{
						address=address.replace("null\n", "");
					}
				}
			}
			addressView.setText(address);
		}
	}
}

