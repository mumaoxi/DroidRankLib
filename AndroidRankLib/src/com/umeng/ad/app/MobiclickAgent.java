/**
 * 
 */
package com.umeng.ad.app;

import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.umeng.ad.app.Market.RankAsyncTask;
import com.umeng.ad.app.NetCenter.RequestListener;
import com.umeng.ad.app.u.CompressType;

public class MobiclickAgent implements RequestListener, LibInterFace {

	private static final String HEADER_APIKEY = "apikey";
	private static final String HEADER_CHANNEL = "channel";
	private static final String HEADER_VERSIONCODE = "versioncode";
	private static final String HEADER_VERSIONNAME = "versionname";
	private static final String HEADER_DEVICEID = "deviceid";

	public MobiclickAgent() {

	}

	static MobiclickAgent mobiclickAgent;

	static MobiclickAgent getInstance() {
		if (mobiclickAgent == null) {
			mobiclickAgent = new MobiclickAgent();
		}
		return mobiclickAgent;
	}

	public static void enableLog(boolean logEnable) {
		MLog.logEnable = logEnable;
	}

	/**
	 * 初始化RankConfig参数
	 * 
	 * @param context
	 * @param apikey
	 */
	public static void init(Context context, String apikey, String channel) {

		/**
		 * 取配置参数XML文件,
		 */
		String url = u.getRankControlURL(context);
		MLog.i("umeng_ad_url:" + url);
		if (url == null || url.trim().length() < 1 || !url.startsWith("http:")) {
			MLog.w("The umeng_ad_url is :" + url
					+ " ,that is not the correct url address");
			return;
		}
		try {
			WebConnection connection = new WebConnection(context);
			connection.setTag(1);
			connection.getHeaders().put("Accept-Encoding", "gzip");
			connection.getHeaders().put(HEADER_APIKEY, apikey);
			connection.getHeaders().put(HEADER_CHANNEL,
					String.valueOf(URLEncoder.encode(channel, "utf-8")));
			connection.getHeaders().put(HEADER_VERSIONCODE,
					u.getAppVersionCode(context) + "");
			connection.getHeaders().put(HEADER_VERSIONNAME,
					u.getAppVersionName(context));
			connection.setUrl(url);
			connection.getHeaders().put(HEADER_DEVICEID, u.getIMEI(context));
			NetCenter.getInstance().startRequest(connection,
					MobiclickAgent.getInstance(), context);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Use this method to make a download behavior.
	 * 
	 * @param context
	 * @param maxNumForAday
	 *            The max number to download in a day.
	 * @return RankAsyncTask
	 */
	public static RankAsyncTask start(Context context, String apiKey,
			String channel) {
		MLog.d("MobiclickAgent start。。。。。");
		try {
			AsyncTask<Context, Void, Market> startAsyncTask = new AsyncTask<Context, Void, Market>() {
				Context context;

				@Override
				protected Market doInBackground(Context... params) {
					try {
						this.context = params[0];
						int iSecrect = u.SECRET_KEY;
						MLog.e("Get jni Seceret:" + iSecrect);

						if (!u.checkNet(params[0])) {
							MLog.e("No network ,return directly.");
							return null;
						}

						String parms = null;
//						if (MLog.logEnable) {
//							MLog.v("logEnabled  read local test file");
//							parms = MobiclickAgent.readXMLTestFile(params[0]);
//						} else {
							MLog.v("logunEnabled  read server real file");
							parms = MobiclickAgent
									.readConfigFromPref(params[0]);
							if ((parms == null) || (parms.trim().length() < 1)) {
								MLog.w("readConfigFromPref finish ,params is null,return");
								return null;
							}

							char secret = (char) iSecrect;
							parms = u.decodeS(parms, secret);
//						}

						if (parms == null) {
							MLog.w("Params is null,return directly.");
							return null;
						}
						Market market = ConfigUtils.getAMarketAppToRank(
								params[0], parms);
						if (market == null) {
							MLog.w("The market app that need rank is null,return;");
							return null;
						}
						return market;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Market market) {
					super.onPostExecute(market);
					try {
						if (market != null) {
							Market.RankAsyncTask localRankAsyncTask = market
									.go(this.context);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			startAsyncTask.execute(new Context[] { context });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	/**
//	 * For test
//	 * 
//	 * @param context
//	 * @return
//	 */
//	private static String readXMLTestFile(Context context) {
//		try {
//			StringBuffer stringBuffer = new StringBuffer("");
//			InputStream inputStream = context.getAssets().open(
//					"licence/umeng_rank_config.xml");
//			BufferedReader bufferedReader = new BufferedReader(
//					new InputStreamReader(inputStream, "utf-8"));
//			String line = "";
//			while ((line = bufferedReader.readLine()) != null) {
//				stringBuffer.append("\n" + line);
//			}
//			inputStream.close();
//			bufferedReader.close();
//			return stringBuffer.toString().replaceFirst("\n", "");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	/**
	 * Read config context which got from server
	 * 
	 * @param context
	 * @return
	 */
	private static String readConfigFromPref(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences(
					Market.PREFNAME, Context.MODE_PRIVATE);
			return sp.getString("config", null);
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onRequestFinished(Context context,
			HashMap<String, String> requestFinish) {
		// CONFIG file
		if (requestFinish != null
				&& Integer.valueOf(requestFinish.get("tag")) == 1) {

			try {
				int code = Integer.valueOf(requestFinish.get("status_code"));
				MLog.i("status_code" + code);
				if (code != HttpStatus.SC_OK) {
					return;
				}
				String body = requestFinish.get("body");
				MLog.i("body" + body);
				MLog.v("OriginalString:" + u.decodeS(body, u.SECRET_KEY));
				SharedPreferences sp = context.getSharedPreferences(
						Market.PREFNAME, Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("config", body);
				editor.commit();
			} catch (Exception e) {
				MLog.e(e.getMessage());
				e.printStackTrace();
			}
		}

		/**
		 * Rank control result returned
		 */
		if (requestFinish != null
				&& Integer.valueOf(requestFinish.get("tag")) == 3) {
			try {
				int code = Integer.valueOf(requestFinish.get("status_code"));
				MLog.i("report status_code" + code);
				if (code != HttpStatus.SC_OK) {
					return;
				}
				String body = requestFinish.get("body");
				MLog.v("report result:" + body);
			} catch (Exception e) {
				MLog.e(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 1.获取需要上传到服务器的所有marketapp的key值 2.清理.umeng.xml中不必要的参数，remove非今天刷的key
	 * 
	 * @param context
	 * @return
	 */
	private static List<Market> getMarketAppsToPost(Context context) {
		try {
			List<Market> markets = new ArrayList<Market>();
			SharedPreferences sp = context.getSharedPreferences(
					Market.PREFNAME, Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date(System.currentTimeMillis());
			String dateStr = format.format(date);
			Map<String, ?> map = sp.getAll();
			if (map != null) {
				Set<String> keys = map.keySet();
				for (String key : keys) {
					try {
						String okey = new String(Base64.decode(key.getBytes(),
								Base64.DEFAULT));
						// 包含@的是刷量的key
						if (okey.contains("@")) {
							String[] array = okey.split("@");
							// 把以往日期的key都清理掉，防止sharedprefrence越来越大
							if (!array[0].equals(dateStr)) {
								Log.w("TAG", "remove key:" + key);
								editor.remove(key);
								continue;
							}

							// 如果已经reportok，那么就不需要再report
							if (sp.getInt(key + "_report", 0) >= sp.getInt(key,
									0)) {
								Log.w("TAG", okey + " has already reank over.");
								continue;
							}

							Market market = (Market) Market.marketClassMap.get(
									array[2]).newInstance();
							market.MARKET_NAME = array[2];
							market.PACKAGE_NAME = array[1];
							markets.add(market);
							Log.d("TAG", "okey:" + market.MARKET_NAME + ":"
									+ market.PACKAGE_NAME);
						}
					} catch (Exception e) {
						Log.e("TAG", e.getMessage());
						continue;
					}
				}
			}
			editor.commit();
			return markets;
		} catch (Exception e) {
			MLog.e("getMarketAppsToPost e," + e.getMessage());
		}
		return null;
	}

	/**
	 * 上传刷量结果到服务器端
	 * 
	 * @param context
	 * @param apikey
	 */
	public static void postDataToServer(final Context context,
			final String apikey, final String channel) {
		try {
			new AsyncTask<Void, Void, String[]>() {

				@Override
				protected String[] doInBackground(Void... params) {
					try {
						// Checknetwork
						if (!u.checkNet(context)) {
							MLog.w("network not avaliable");
							return null;
						}

						// 获取到所有需要上传的market app
						List<Market> markeAppKeys = getMarketAppsToPost(context);
						if (markeAppKeys == null || markeAppKeys.size() < 1) {
							MLog.w(" no market app need to rank.");
							return null;
						}
						MLog.d(" need post market app size is:"
								+ markeAppKeys.size());

						/**
						 * Step4.Make all the keys for rank number to JSON
						 */
						SharedPreferences sPreferences = context
								.getSharedPreferences(Market.PREFNAME,
										Context.MODE_PRIVATE);

						Editor editor = sPreferences.edit();

						// md5 constant:umeng
						String md5constant = u.MD5_CONSTANT;
						JSONArray jsonArray = new JSONArray();
						JSONObject j = new JSONObject();
						j.put(MD5Utils.string2MD5("method" + md5constant),
								MD5Utils.string2MD5("post_rank_report"
										+ md5constant));
						jsonArray.put(j);

						// 一个一个地分析每个市场app
						for (Market marketApp : markeAppKeys) {
							JSONObject jsonObject = new JSONObject();
							String countKey = marketApp
									.getSharePrfKeyForRankCount(context);
							int rankCount = sPreferences.getInt(countKey, 0);
							int reportedCount = sPreferences.getInt(countKey
									+ "_report", 0);
							int reportCount = (reportedCount <= rankCount) ? (rankCount - reportedCount)
									: 0;

							if (reportCount > 0) {
								jsonObject
										.put(MD5Utils.string2MD5("market"
												+ md5constant), MD5Utils
												.string2MD5(marketApp
														.getMarketName()
														+ md5constant));
								jsonObject.put(MD5Utils.string2MD5("package"
										+ md5constant), MD5Utils
										.string2MD5(marketApp.getPackageName()
												+ md5constant));
								jsonObject.put(
										MD5Utils.string2MD5("count"
												+ md5constant),
										MD5Utils.string2MD5(reportCount
												+ md5constant));
								jsonArray.put(jsonObject);
							}
							editor.putInt(countKey + "_report", rankCount);
						}
						editor.commit();

						/**
						 * Step5.report rank count to server.
						 */
						if (jsonArray.length() < 2) {
							MLog.w("no need to report,return");
							return null;
						}

						String url = u.getRankControlURL(context);
						MLog.i("umeng_ad_url:" + url);
						if (url == null || url.trim().length() < 1
								|| !url.startsWith("http:")) {
							MLog.w("The umeng_ad_url is :" + url
									+ " ,that is not the correct url address");
							return null;
						}
						return new String[] { url, jsonArray.toString() };
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(String[] result) {
					super.onPostExecute(result);
					if (result != null && result.length > 1) {
						try {
							WebConnection connection3 = new WebConnection(
									context);
							connection3.getHeaders().put(HEADER_APIKEY, apikey);
							connection3.getHeaders().put(
									HEADER_CHANNEL,
									String.valueOf(URLEncoder.encode(channel,
											"utf-8")));
							connection3.getHeaders().put(HEADER_VERSIONCODE,
									u.getAppVersionCode(context) + "");
							connection3.getHeaders().put(HEADER_VERSIONNAME,
									u.getAppVersionName(context));
							connection3.getHeaders().put(HEADER_DEVICEID,
									u.getIMEI(context));

							connection3.setHttpMethod("post_json");
							connection3.setCompressType(CompressType.GZIP);
							connection3.setTag(3);
							connection3.setJsonString(result[1]);
							connection3.setUrl(result[0]);
							NetCenter.getInstance().startRequest(connection3,
									MobiclickAgent.getInstance(), context);

							MLog.d("control_jsons:" + result[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void libEnableLog(boolean logEnable) {
		enableLog(logEnable);
	}

	@Override
	public void libInit(Context context, String apikey, String channel) {
		try {
			init(context, apikey, channel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void libStart(Context context, String apikey, String channel) {
		try {
			start(context, apikey, channel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void libPostData(Context context, String apikey, String channel) {
		try {
			postDataToServer(context, apikey, channel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
