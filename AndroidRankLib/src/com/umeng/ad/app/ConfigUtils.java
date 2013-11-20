package com.umeng.ad.app;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.SharedPreferences;

import com.umeng.ad.app.utils.Attribute;
import com.umeng.ad.app.utils.DocumentUtils;
import com.umeng.ad.app.utils.ElementUtils;
import com.umeng.ad.app.utils.SAXReader;

class ConfigUtils {

	private static Market getMarket(String marketName,
			List<Attribute> market_attr, HashMap<String, String> params) {
		try {
			try {
//				MLog.v("getMarket:" + marketName);
//				MLog.i("classMap:" + Market.marketClassMap);
				Market market = (Market) Market.marketClassMap.get(marketName)
						.newInstance();
				market.MARKET_NAME = marketName;
				for (Attribute attribute : market_attr) {
					String name = attribute.getName();
					String value = attribute.getValue();
					params.put("m_" + name, value);
					if ("numForMoblieRank".equals(name)) {
						market.maxNumForNet = Integer.valueOf(value);
					}
					if ("numForWifiRank".equals(name)) {
						market.maxNumForWifi = Integer.valueOf(value);
					}
				}
				market.params = params;
				return market;
			} catch (Exception e) {
				MLog.e("getMarket:" + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			MLog.e("getMarket2:" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取到一个可以
	 * 
	 * @param context
	 * @param prams
	 * @return
	 */
	protected static Market getAMarketAppToRank(Context context, String prams) {
		try {
			MLog.d("==getAMarketAppToRank");
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(new ByteArrayInputStream(prams
					.getBytes("utf-8")));
			Element rootElement = DocumentUtils.getInstance().getRootElement(
					document);

			// 获取所有的Market Element
			List<Element> marketElements = ElementUtils.getInstance().elements(
					rootElement, "market");
			List<Market> markets = new ArrayList<Market>();
			/**
			 * 分析每一个market
			 */
			for (Element market_ele : marketElements) {
				// market 的名字
				String marketName = ElementUtils.getInstance().attributeValue(
						market_ele, "name");
				// market 所有的attribute
				List<Attribute> market_attr = ElementUtils.getInstance()
						.attributes(market_ele);
				HashMap<String, String> params = new HashMap<String, String>();
				Market market = getMarket(marketName, market_attr, params);
				// TODO:
				// 如果市场不存在，跳过本次循环
				if (market == null) {
					MLog.w("market is null continue");
					continue;
				}

				/**
				 * 分析这个市场下所有的app
				 */
				List<Element> appElements = ElementUtils.getInstance()
						.elements(market_ele, "app");
				// 如果app为空，跳过本次循环，没有必要在往下分析了
				if (appElements == null || appElements.size() < 1) {
					continue;
				}
				for (Element app_ele : appElements) {
					market = getMarket(marketName, market_attr, params);
					// app 的包名
					String packageName = ElementUtils.getInstance()
							.attributeValue(app_ele, "package");
					market.PACKAGE_NAME = packageName;
					// app 所有的attribute
					List<Attribute> app_attr = ElementUtils.getInstance()
							.attributes(app_ele);
					for (Attribute attribute : app_attr) {
						String name = attribute.getName();
						String value = attribute.getValue();
						params.put("a_" + name, value);
						if ("numForMoblieRank".equals(name)) {
							market.maxNumForNet = Integer.valueOf(value);
						}
						if ("numForWifiRank".equals(name)) {
							market.maxNumForWifi = Integer.valueOf(value);
						}
						if ("keyword".equals(name)) {
							market.setAppKeywords(cutParamsWithSeperator(value));
						}
					}
					/**
					 * 初始化自己的特殊参数
					 */
					market.initAllParams();

					/**
					 * 过滤掉已经刷完的市场APP
					 */
					SharedPreferences sp = context.getSharedPreferences(
							Market.PREFNAME, Context.MODE_PRIVATE);

					int maxNumForAday = market.getMaxNumForNet();
					String apn = u.getAPN(context);
					if (apn != null && apn.toLowerCase().equals("wifi")) {
						maxNumForAday = market.getMaxNumForWifi();
					}
					boolean rank_over = false;
					try {
						String rankCountKey = market
								.getSharePrfKeyForRankCount(context);
						int downloadCount = sp.getInt(rankCountKey, 0);
						rank_over = (downloadCount >= maxNumForAday);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (rank_over) {
						MLog.w("market is null continue");
						continue;
					}
					markets.add(market);
				}// 结束for循环

			}// 结束 分析每一个market

			/**
			 * 返回可以用的market
			 */
			if (markets.size() < 1) {
				return null;
			}
			int random = (int) ((Math.random()) * markets.size());
			Market m = markets.get(random);
			MLog.i("avaliable size " + markets.size() + " random:" + random
					+ " market:" + m);
			return m;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * cut params with seperator '|'
	 * 
	 * @param params
	 * @return
	 */
	protected static String[] cutParamsWithSeperator(String params) {
		try {
			if (params != null && params.contains("|")) {
				String[] keywords = params.split("\\|");
				return keywords;
			} else if (params != null) {
				String[] parString = { params };
				return parString;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
