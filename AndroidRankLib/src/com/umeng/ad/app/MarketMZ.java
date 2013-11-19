package com.umeng.ad.app;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketMZ extends Market {

	private String APP_ID = "";
	private String cookie = "";

	public MarketMZ() {
	}

	public MarketMZ(String packageName) {
		MARKET_NAME = MARKET_MZ;
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
			long mills = System.currentTimeMillis();
			String userAgent = u.getInstance().getAgent(
					(int) (System.currentTimeMillis() % 14));
			String url = String
					.format("http://app.meizu.com/service/ms_versionAct/getFreeAppDownloadUrl.jsonp?p0=%1$s&%2$s",
							APP_ID, mills);
			MLog.v("Meizu:" + url);
			HttpGet httpGet = new HttpGet(url);

			/**
			 * Logger.i(this,curlString);
			 */
			httpGet.addHeader("Cookie", cookie);
			MLog.v("Meizu,cookie:" + cookie);
			httpGet.addHeader("Referer", String.format(
					"http://app.meizu.com/phone/apps/%1$s", APP_ID));

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
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, userAgent);
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("getAnalysis=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.i("body:" + body);
			JSONObject object = new JSONObject(body);
			if (object.getString("reply") != null
					&& object.getString("reply").startsWith("http://")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
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
		}
	}

}
