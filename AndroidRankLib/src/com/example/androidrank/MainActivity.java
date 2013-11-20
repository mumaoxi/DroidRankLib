package com.example.androidrank;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.ad.app.LibInterFace;
import com.umeng.ad.app.MobiclickAgent;

public class MainActivity extends Activity {

	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// MobiclickAgent agent = MobiclickAgent.getInstance();
		// agent.enableLog(true);
		// agent.init(this);
		// agent.start(this);
		LibInterFace interFace = new MobiclickAgent();
		interFace.libEnableLog(true);
		 interFace.libInit(this,"2d4c5104","360软件管家");
		 interFace.libStart(this,"2d4c5104","360软件管家");

		Log.i("TAG", "model:" + Build.MODEL);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Log.i("TAG", "imei:" + telephonyManager.getDeviceId());
		Log.i("TAG", "brand:" + Build.BRAND);
		Log.i("TAG", "model:" + Build.MODEL);
		Log.i("TAG", "manufacture:" + Build.MANUFACTURER);
		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new MyClient());
		mWebView.loadUrl("http://gadget.sinaapp.com/enter.php/Boast/Analysis/ipGet");

		
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// mWebView.loadUrl("javascript:alert('ok');");
				mWebView.loadUrl("javascript:DXA();");
				// mWebView.loadUrl("javascript:document.getElementById('chat_area_').value='有文字了没有？呢';");
				Log.i("TAG", "url:" + "ok");
			}
		}, 1000 * 12);
	}

	private class MyClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			// if
			// ("http://webchat.b.qq.com/webchat.htm?sid=2188z8p8p8p8x8q8R8P8q".equals(url))
			// {
			// mWebView.loadUrl("file:///android_asset/myjs.js ");
			// }
			// if ("file:///android_asset/myjs.js".equals(url)) {
			mWebView.loadUrl("javascript:alert('ok');");
			// }
			Log.i("TAG", "url:" + url);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LibInterFace interFace = new MobiclickAgent();
			interFace.libPostData(this, "2d4c5104", "360软件管家");
		}
		return super.onKeyDown(keyCode, event);
	}
}
