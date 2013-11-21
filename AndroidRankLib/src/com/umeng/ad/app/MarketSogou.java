package com.umeng.ad.app;

import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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

	private String getAppId(String app_name, String packagename) {
		String appname = URLEncoder.encode(app_name);
		String appid = "";
		String url = "http://mobile.zhushou.sogou.com/android/getapplist.html?f=search";
		HttpPost httpPost = new HttpPost(url);
		JSONObject search = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject limit = new JSONObject();
		try {
			limit.put("limit", "25");
			limit.put("module", "search");
			limit.put("gruopid", "mix");
			limit.put("start", "0");
			limit.put("keyword", appname);
			search.put("search", limit);
			data.put("data", search);
			MLog.i("post param = " + data);

			httpPost.setEntity(new StringEntity(data.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
		httpPost.setHeader("Content-Type", "text/plain; charset=utf-8");

		HttpClient client = new DefaultHttpClient();

		try {
			HttpResponse httpResponse = client.execute(httpPost);
			int code = httpResponse.getStatusLine().getStatusCode();
			MLog.i("response statuc code = " + code);
			if (code == 200) {
				String retSrc = EntityUtils.toString(httpResponse.getEntity());
				MLog.i("result str = " + retSrc);
				JSONObject result = new JSONObject(retSrc);
				JSONArray list = result.getJSONObject("search")
						.getJSONObject("mix").getJSONArray("list");
				for (int i = 0; i < list.length(); i++) {
					JSONObject item = list.getJSONObject(i);
					if (item.getString("packagename").equalsIgnoreCase(
							packagename)) {
						appid = item.getString("appid");
						MLog.i("get appid = " + appid);
						return appid;
					}
				}

			} else {
				MLog.i("request failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		MLog.i("get appid = " + appid);
		return appid;

	}

}
