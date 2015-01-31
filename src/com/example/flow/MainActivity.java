package com.example.flow;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {
	private static View viewthingy;
	private static String amountthingy;
	private static final int PAYMENT = Menu.FIRST;
	private static boolean signed_in = false;
	private static String username;
	private static String password;
	private static PayPalConfiguration config = new PayPalConfiguration()

	// Start with mock environment. When ready, switch to sandbox
	// (ENVIRONMENT_SANDBOX)
	// or live (ENVIRONMENT_PRODUCTION)
			.environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)

			.clientId("Ac95xBAWKOG4Lk-FI68IBxvlpgMymXn5XLLRk2GReKvBnbNU6PKBmc--10FG");
	//static MainActivity x = new MainActivity();
	//private static MyJavaScriptInterface myJSI = x.new MyJavaScriptInterface();
	

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
	}

	@Override
	public void onDestroy() {
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, new DonationFragment()).commit();
		Log.i("POSITION", Integer.toString(position));
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section4);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class DonationFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			// Inflate the layout for this fragment
			View view = inflater.inflate(R.layout.don_layout, container, false);
			viewthingy = view;
			
			Button donButton = (Button) view.findViewById(R.id.donButton);
			donButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					EditText donTextView = (EditText) viewthingy.findViewById(R.id.donTextView);
					String amount = donTextView.getText().toString();
					if(!amount.equals("") && !amount.equals(".")) {
						amountthingy = amount;
						PayPalPayment payment = new PayPalPayment(
								new BigDecimal(amount), "USD", "Donation",
								PayPalPayment.PAYMENT_INTENT_SALE);
	
						Intent intent = new Intent(DonationFragment.this.getActivity(), PaymentActivity.class);
						intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
						startActivityForResult(intent, PAYMENT);
					} else {
						Toast.makeText(DonationFragment.this.getActivity(), "Please enter a valid amount", Toast.LENGTH_LONG).show();
					}
				}
			});

			return view;
		}
		
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent intent) {
			EditText donTextView = (EditText) viewthingy.findViewById(R.id.donTextView);
			donTextView.setText("");
			WebView fuckingWebView = (WebView) viewthingy.findViewById(R.id.webView1);
			if(requestCode == PAYMENT) {
				switch(resultCode) {
				case PaymentActivity.RESULT_OK:
					Toast.makeText(DonationFragment.this.getActivity(), "THANK YOU FOR YOUR DONATION! :D", Toast.LENGTH_LONG).show();
					String username = "kamy_ai@yahoo.com";
					
					Calendar cal = Calendar.getInstance();
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					String date_time = dateFormat.format(cal.getTime());
					String[] dateList = date_time.split(" ");
					date_time = dateList[0] + dateList[1];
					try {
						String info = "http://flowproject.ca/?Donation%username=" + username +
								 "%date_time=" + date_time + "%amount=" + amountthingy + "%";
						
						fuckingWebView.loadUrl(info);
						
						Log.i("THE INFO", info);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case PaymentActivity.RESULT_CANCELED:
					Toast.makeText(DonationFragment.this.getActivity(), "DONATION UNSUCCESSFUL", Toast.LENGTH_LONG).show();
					break;
				}
			}
		}

		
	}
	
	//final Context myApp = this;
	
	

	
	public static class SignInFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			// Inflate the layout for this fragment
			View view = inflater.inflate(R.layout.signin_layout, container, false);
			viewthingy = view;
			
			Button signButton = (Button) view.findViewById(R.id.signinButton);
			signButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					EditText userTextView = (EditText) viewthingy.findViewById(R.id.userField);
					EditText passTextView = (EditText) viewthingy.findViewById(R.id.passField);
					
					username = userTextView.getText().toString();
					password = passTextView.getText().toString();
					//String info = "http://flowproject.ca/?Verification%username=" + username +
					//			  "%password=" + password + "%";
					
					//WebView signWV = (WebView) viewthingy.findViewById(R.id.webView1);
					/*
					signWV.getSettings().setJavaScriptEnabled(true);
					signWV.addJavascriptInterface(myJSI, "HTMLOUT");*/
					
					/* WebViewClient must be set BEFORE calling loadUrl! */
					/*
					signWV.setWebViewClient(new WebViewClient() {
					    @Override
					    public void onPageFinished(WebView view, String url)
					    {
					        /* This call inject JavaScript into the page which just finished loading. */
					/*        
					signWV.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
					    }
					});*/
					
					/* load a web page */
					//signWV.loadUrl(info);
					//signWV.loadUrl(info);
					if(username.equals("kamy_ai@yahoo.com") && password.equals("12345678"))
					{
						signed_in = true;
					}
				}
			});
			return view;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	
	public static String getHTML(String urlString) throws Exception

	{

		try {
	
			// create object to store html source text as it is being collected
		
			StringBuilder html = new StringBuilder();
		
			// open connection to given url
		
			URL url = new URL(urlString);
		
			URLConnection connection = url.openConnection();
		
			// create BufferedReader to buffer the given url's HTML source
		
			BufferedReader htmlbr = new BufferedReader(new InputStreamReader(
		
			connection.getInputStream()));
		
			String line;
		
			// read each line of HTML code and store in StringBuilder
		
			while ((line = htmlbr.readLine()) != null) {
		
			html.append(line);
		
			}
		
			htmlbr.close();
		
			// convert StringBuilder into a String and return it
		
			return html.toString();
		
		} catch (Exception e) {
	
			e.printStackTrace();
			return null;

		}

	}
	
	/* An instance of this class will be registered as a JavaScript interface */
	/*
	class MyJavaScriptInterface
	{
	    @SuppressWarnings("unused")
	    public void processHTML(String html)
	    {
	        if(html.equals("1")) {
	        	signed_in = true;
	        	Log.i("IT WORKED!", "OR DID IT?!");
	        } else if(html.equals("0")) {
	        	Log.i("IT WORKED!", "NOPE!");
	        }
	    }
	}*/

}
