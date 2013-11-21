package com.umeng.ad.app;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketSogou extends Market {

	private String APP_ID = "";

	public MarketSogou() {
	}

	public MarketSogou(String packageName) {
		MARKET_NAME = MARKET_SOGOU;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {

		return getAnalysis();
	}

	/**
	 * 
	 */
	private boolean getAnalysis() {
		try {
			String url = "http://mobile.zhushou.sogou.com/android/download.html?app_id="
					+ APP_ID;
			MLog.v(MARKET_NAME + ":" + url);
			HttpGet httpGet = new HttpGet(url);
			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 10 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("Sohu getAnalysis=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("body:" + body);
			if (code == HttpStatus.SC_MOVED_TEMPORARILY) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getAPP_ID() {
		return APP_ID;
	}

	public void setAPP_ID(String aPP_ID) {
		APP_ID = aPP_ID;
	}

	@Override
	protected void initAllParams() {
		try {
			setAPP_ID(params.get("a_appId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
