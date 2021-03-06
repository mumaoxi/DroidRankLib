package com.umeng.ad.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Base64;

abstract class Market {
	protected String[] appKeywords = { u.DEFAULT_KEYWORD };
	protected static final String PREFNAME = ".umeng";

	protected String MARKET_NAME = "Market";
	protected String PACKAGE_NAME = "PackageName";

	protected HashMap<String, String> params = new HashMap<String, String>();

	protected int maxNumForWifi = 5;
	protected int maxNumForNet = 3;

	protected static final String MARKET_BIDO = u.getInstance().getMName(0);
	protected static final String MARKET_HIAPK = u.getInstance().getMName(1);
	protected static final String MARKET_ZHIHUIYUN = u.getInstance()
			.getMName(2);
	protected static final String MARKET_GFAN = u.getInstance().getMName(7);
	protected static final String MARKET_LENOVO = u.getInstance().getMName(11);
	protected static final String MARKET_MZ = u.getInstance().getMName(3);
	protected static final String MARKET_OPPO = u.getInstance().getMName(4);
	protected static final String MARKET_NDUOA = u.getInstance().getMName(6);
	protected static final String MARKET_XIAOMI = u.getInstance().getMName(12);
	protected static final String MARKET_360 = u.getInstance().getMName(13);
	protected static final String MARKET_MUMAYI = u.getInstance().getMName(14);
	protected static final String MARKET_COOLSMART = u.getInstance().getMName(
			15);
	protected static final String MARKET_ZTE = u.getInstance().getMName(16);
	protected static final String MARKET_SOHU = u.getInstance().getMName(17);
	protected static final String MARKET_SOGOU = u.getInstance().getMName(18);

	// All market name and className collection
	protected static final HashMap<String, Class<?>> marketClassMap = new HashMap<String, Class<?>>();
	static {
		marketClassMap.put(MARKET_BIDO, MarketBido.class);
		marketClassMap.put(MARKET_HIAPK, MarketHiAPK.class);
		marketClassMap.put(MARKET_ZHIHUIYUN, MarketZhihuiyun.class);
		marketClassMap.put(MARKET_GFAN, MarketGfan.class);
		marketClassMap.put(MARKET_LENOVO, MarketLenovo.class);
		marketClassMap.put(MARKET_MZ, MarketMZ.class);
		marketClassMap.put(MARKET_OPPO, MarketOppo.class);
		marketClassMap.put(MARKET_NDUOA, MarketNDuoa.class);
		marketClassMap.put(MARKET_XIAOMI, MarketXiaomi.class);
		marketClassMap.put(MARKET_360, Market360.class);
		marketClassMap.put(MARKET_MUMAYI, MarketMumayi.class);
		marketClassMap.put(MARKET_COOLSMART, MarketCoolSmart.class);
		marketClassMap.put(MARKET_ZTE, MarketZTE.class);
		marketClassMap.put(MARKET_SOHU, MarketSohu.class);
		marketClassMap.put(MARKET_SOGOU, MarketSogou.class);
	}

	public Market() {
	}

	/**
	 * 初始化必须的参数，每一个app所需要的不同，所以这里定义了这个抽象方法
	 * 
	 * @param context
	 */
	protected abstract void initAllParams();

	class RankAsyncTask extends AsyncTask<Context, Void, Boolean> {

		private Context context;

		RankAsyncTask(Context context) {
			this.context = context;
		}

		@Override
		protected Boolean doInBackground(Context... params) {
			try {
				return prepareToRank(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onCancelled() {
			MLog.i("oncalled");
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result != null && result && !isCancelled()) {
				try {
					MLog.i("Task excute over,result is :" + result);
					saveData(context);
				} catch (Exception e) {
					MLog.e(e.getMessage());
					e.printStackTrace();
				}
			} else {
				MLog.w("Task excute over ,resutl is " + result + " ,cancelled "
						+ isCancelled());
			}
		}
	}

	/**
	 * Save data to local shared preference and uploads data to server.
	 * 
	 * @param context
	 * @throws Exception
	 */
	void saveData(Context context) throws Exception {
		/**
		 * Save data
		 */
		SharedPreferences sp = context.getSharedPreferences(PREFNAME,
				Context.MODE_PRIVATE);
		String rankCountKey = getSharePrfKeyForRankCount(context);
		int downloadCount = sp.getInt(rankCountKey, 0);
		Editor editor = sp.edit();
		editor.putInt(rankCountKey, (downloadCount + 1));
		editor.commit();
		MLog.v("Save data  " + rankCountKey + "=>" + (downloadCount + 1));

		/**
		 * Umeng online params analysis
		 */
	}

	/**
	 * Start to rank the market app
	 * 
	 * @param context
	 * @return asynck task you can cancel it when unused.
	 */
	protected RankAsyncTask go(Context context) {
		try {
			RankAsyncTask rankAsyncTask = new RankAsyncTask(context);
			rankAsyncTask.execute(context);
			return rankAsyncTask;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract boolean prepareToRank(Context context) throws Exception;

	// protected String getSharePrfKeyForRankControl() {
	// try {
	// Date date = new Date(System.currentTimeMillis());
	// String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
	// dateString = dateString + getPackageName() + getMarketName();
	//
	// return dateString;
	// } catch (Exception e) {
	// MLog.e(e.getStackTrace());
	// e.printStackTrace();
	// }
	// return "null";
	// }

	protected String getSharePrfKeyForRankCount(Context context) {
		try {
			Date date = new Date(System.currentTimeMillis());
			String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
			dateString = dateString + "@" + getPackageName() + "@"
					+ getMarketName() + "@";
			String apn = u.getAPN(context);
			MLog.d("rankcountKey:" + (dateString + apn));
			String key = Base64.encodeToString((dateString + apn).getBytes(),
					Base64.DEFAULT).replace("\n", "");
			String dkey = new String(Base64.decode(key.getBytes(),
					Base64.DEFAULT));
			MLog.i("rankcountKey base64 encode :" + key);
			MLog.i("rankcountKey base64 decode :" + dkey);
			return key;
		} catch (Exception e) {
			MLog.e(e.getStackTrace());
			e.printStackTrace();
		}
		return "null";
	}

	protected String getMarketName() {
		return MARKET_NAME;
	}

	protected String getPackageName() {
		return PACKAGE_NAME;
	}

	protected void setPackageName(String packageName) {
		PACKAGE_NAME = packageName;
	}

	protected int getMaxNumForNet() {
		return maxNumForNet;
	}

	protected void setMaxNumForNet(int maxNumForNet) {
		this.maxNumForNet = maxNumForNet;
	}

	protected int getMaxNumForWifi() {
		return maxNumForWifi;
	}

	protected void setMaxNumForWifi(int maxNumForWifi) {
		this.maxNumForWifi = maxNumForWifi;
	}

	protected String getAppKeyword() {
		try {
			return (appKeywords != null && appKeywords.length > 0 ? appKeywords[(int) (Math
					.random() * appKeywords.length)] : u.DEFAULT_KEYWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return u.DEFAULT_KEYWORD;
	}

	/**
	 * 更新MarketApp在线参数
	 * 通过搜索找到的APP，把关键参数获取到之后更新到服务器端，这样保持服务器端永远是最新的在线参数，省去了手工更新参数的麻烦
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	protected void updateMarketAppParams(Context context, JSONArray key,
			JSONArray value) {
		try {
			MLog.v("=====updateMarketAppParams=====");
			String url = "http://umeng.sinaapp.com/enter.php/Api/OnlineParam/insert";
			MLog.i("updateMarketAppParams:" + url);
			// Request
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");

			// body
			List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
			httpParams.add(new BasicNameValuePair("market_name", MARKET_NAME));
			httpParams
					.add(new BasicNameValuePair("package_name", PACKAGE_NAME));
			httpParams.add(new BasicNameValuePair("param_name", key.toString()));
			httpParams.add(new BasicNameValuePair("param_value", value.toString()));
			httpParams.add(new BasicNameValuePair("type", "app"));

			httpPost.setEntity(new UrlEncodedFormEntity(httpParams, HTTP.UTF_8));

			// Client params
			DefaultHttpClient client = new DefaultHttpClient();
			HttpParams params = new BasicHttpParams();
			u.setParamsForHttpConnection(params);
			HttpProtocolParams.setUserAgent(params, u.randomAgent());
			client.setParams(params);

			// Response
			HttpResponse response = client.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			MLog.i("updateMarketAppParams,status:" + statusCode);

			String content = u
					.readContentFromHttpResponse(response, HTTP.UTF_8);

			MLog.d("updateMarketAppParams,content:" + content);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Market [MARKET_NAME=" + MARKET_NAME + ", PACKAGE_NAME="
				+ PACKAGE_NAME + "]";
	}

	protected void setAppKeywords(String[] appKeywords) {
		this.appKeywords = appKeywords;
	}
}
