package com.umeng.ad.app;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.SharedPreferences;

import com.umeng.ad.app.utils.Attribute;
import com.umeng.ad.app.utils.DocumentUtils;
import com.umeng.ad.app.utils.ElementUtils;
import com.umeng.ad.app.utils.SAXReader;

class ConfigUtils {

	private static Market getMarket(String marketName,List<Attribute> market_attr,HashMap<String,String> params){
		try {
			try {
				Market
				market = (Market) Market.marketClassMap.get(marketName)
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
				return market;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
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
				//TODO:
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
			int random = (int)((Math.random()) * markets.size());
			Market m = markets.get(random);
			MLog.i("avaliable size "+markets.size()+" random:"+random+" market:" + m);
			return m;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a market app to start to rank
	 * 
	 * @param prams
	 * @return
	 */
	@Deprecated
	protected static Market getAMarketToRank(Context context, String prams) {
		try {
			MLog.d("==analyzeMarketElement");
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(new ByteArrayInputStream(prams
					.getBytes("utf-8")));
			Element rootElement = DocumentUtils.getInstance().getRootElement(
					document);

			/**
			 * Step1.Applications that can run the rank program.
			 */
			Element applicationELement = ElementUtils.getInstance().element(
					rootElement, "applications");
			if (applicationELement == null) {
				MLog.w("Application that can run the rank program is null");
				return null;
			}
			boolean isTheAppHost = analyzeIfIsTheAppHost(context,
					applicationELement);
			if (!isTheAppHost) {
				MLog.w("This application is not the apphost,return directly.");
				return null;
			}
			MLog.v("Yes, this application is just the application that can run the rank program.");

			/**
			 * Step2.Tactics for the market and the app that need rank.
			 */
			Element tacticsElement = ElementUtils.getInstance().element(
					rootElement, "tactics");
			if (tacticsElement == null) {
				MLog.w("Tatics is null, so  there's no market need to rank,return");
				return null;
			}
			HashMap<String, List<? extends Market>> targestMarketApps = analyzeTheTactics(
					context, tacticsElement);
			if (targestMarketApps == null || targestMarketApps.size() < 1) {
				MLog.w("There's no market app need to rank,return null.");
				return null;
			}
			MLog.v("OK,we get the market to run the rank program:"
					+ targestMarketApps);

			/**
			 * Step3.Market app config params.
			 */
			List<Element> paramElements = ElementUtils.getInstance()
					.selectNodes(rootElement, "/manifest/params/market");
			for (Element element : paramElements) {
				String marketName = element.getAttribute("name");
				MLog.d("market:" + marketName);

				/**
				 * 统一分析所有的参数
				 */
				analyzeMarketElement(context,
						targestMarketApps.get(marketName), element);
				/**
				 * Bido
				 */
				if (Market.MARKET_BIDO.equals(marketName)) {
					analyzeMarketBidoElement(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * HiAPK
				 */
				if (Market.MARKET_HIAPK.equals(marketName)) {

					analyzeMarketHiApkElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * Zhihuiyun
				 */
				if (Market.MARKET_ZHIHUIYUN.equals(marketName)) {
					analyzeMarketZhihuiyunElement(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * Gfan
				 */
				if (Market.MARKET_GFAN.equals(marketName)) {
					analyzeMarketGfanElement(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * Lenovo
				 */
				if (Market.MARKET_LENOVO.equals(marketName)) {
					analyzeMarketLenovoElement(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * Meizu
				 */
				if (Market.MARKET_MZ.equals(marketName)) {
					analyzeMarketMZElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * Oppo
				 */
				if (Market.MARKET_OPPO.equals(marketName)) {
					analyzeMarketOppoElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * Nduoa
				 */
				if (Market.MARKET_NDUOA.equals(marketName)) {
					analyzeMarketNduoaElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * Xiaomi
				 */
				if (Market.MARKET_XIAOMI.equals(marketName)) {
					analyzeMarketXiaomiElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * 360
				 */
				if (Market.MARKET_360.equals(marketName)) {
					analyzeMarket360Element(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * Mumayi
				 */
				if (Market.MARKET_MUMAYI.equals(marketName)) {
					analyzeMarketMumayiElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * CoolMart
				 */
				if (Market.MARKET_COOLSMART.equals(marketName)) {
					analyzeMarketCoolMartElement(context,
							targestMarketApps.get(marketName), element);
				}

				/**
				 * ZTE
				 */
				if (Market.MARKET_ZTE.equals(marketName)) {
					analyzeMarketZTEElement(context,
							targestMarketApps.get(marketName), element);

				}

				/**
				 * Sohu
				 */
				if (Market.MARKET_SOHU.equals(marketName)) {
					analyzeMarketSohuElement(context,
							targestMarketApps.get(marketName), element);

				}
			}

			/**
			 * Step4. Get the target target market app
			 */
			return getTheTargetAppNew(context, targestMarketApps);
		} catch (Exception e) {
			MLog.e("getAMarketToRank:" + e.getMessage() + ":" + e.getCause());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * If this application is a host application that need run the rank program
	 * return true;
	 * 
	 * @param context
	 * @param applicationELement
	 *            the element that need to analyze
	 * @return <i>true</i> this is the AppHost
	 *         <hr>
	 *         <i>false</i> this is not a AppHost
	 */
	@Deprecated
	protected static boolean analyzeIfIsTheAppHost(Context context,
			Element applicationELement) {
		try {
			boolean isTheAppHost = true;
			/**
			 * Attribute
			 */
			List<Attribute> attributes = ElementUtils.getInstance().attributes(
					applicationELement);
			for (Attribute attribute : attributes) {
				if ("open".equals(attribute.getName())) {
					isTheAppHost = Boolean.valueOf(attribute.getValue());
					MLog.i("analyzeIfIsTheAppHost application open value:"
							+ attribute.getValue() + "==" + isTheAppHost);
					break;
				}
			}

			/**
			 * App
			 */
			List<Element> appElements = ElementUtils.getInstance().elements(
					applicationELement, "app");
			if (appElements == null || appElements.size() < 1) {
				MLog.w("analyzeIfIsTheAppHost appElements is null ,return");
				return isTheAppHost;
			}
			for (Element element : appElements) {
				List<Attribute> appAttributes = ElementUtils.getInstance()
						.attributes(element);
				for (Attribute attribute : appAttributes) {
					if ("package".equals(attribute.getName())) {
						MLog.i(context.getApplicationInfo().packageName + ":"
								+ attribute.getValue());
						if (context.getApplicationInfo().packageName
								.equals(attribute.getValue())) {
							MLog.i("analyzeIfIsTheAppHost OK just the package:");
							String channel = element.getAttribute("channel");

							String open = element.getAttribute("open");
							MLog.d("analyzeIfIsTheAppHost open:" + open);
							if (channel != null) {
								if (containsStringInArray(
										cutParamsWithSeperator(channel),
										u.getMetaData(context, "UMENG_CHANNEL"))) {

									isTheAppHost = (open != null ? Boolean
											.valueOf(open) : isTheAppHost);
									MLog.i("analyzeIfIsTheAppHost ok just contains the channel open:"
											+ isTheAppHost);
								} else {
									MLog.w("analyzeIfIsTheAppHost NO did not contain the channel");
								}
							} else {
								MLog.w("analyzeIfIsTheAppHost Oh,no the channel is null");
								isTheAppHost = (open != null ? Boolean
										.valueOf(open) : isTheAppHost);
							}
							break;
						}
					}
				}
			}
			return isTheAppHost;
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Analyze the tactics in server setting.We can get the market and apps that
	 * this application need to rank.
	 * 
	 * @param context
	 * @param tacticsElement
	 * @return The market contains package that need to rank.
	 */
	@Deprecated
	protected static HashMap<String, List<? extends Market>> analyzeTheTactics(
			Context context, Element tacticsElement) {
		try {
			HashMap<String, List<? extends Market>> map = new HashMap<String, List<? extends Market>>();

			/** The max number to rank a app in 2G/3G network */
			int numForMoblieRank = 2;
			/** The max number to rank a app in wifi network */
			int numForWifiRank = 8;

			/** Attribute */
			List<Attribute> attributes = ElementUtils.getInstance().attributes(
					tacticsElement);
			for (Attribute attribute : attributes) {
				if ("numForMoblieRank".equals(attribute.getName())) {
					numForMoblieRank = Integer.valueOf(attribute.getValue());
					MLog.i("analyzeTheTactics numForMoblieRank value:"
							+ attribute.getValue() + "==" + numForMoblieRank);
				}
				if ("numForWifiRank".equals(attribute.getName())) {
					numForWifiRank = Integer.valueOf(attribute.getValue());
					MLog.i("analyzeTheTactics numForWifiRank value:"
							+ attribute.getValue() + "==" + numForWifiRank);
				}
			}

			/** Market apps */
			List<Element> marketElements = ElementUtils.getInstance().elements(
					tacticsElement, "market");
			if (marketElements == null || marketElements.size() < 1) {
				MLog.w("analyzeTheTactics marketElements is null ,return");
				return null;
			}
			for (Element element : marketElements) {
				String marketName = element.getAttribute("name");

				MLog.i("analyzeTheTactics market:" + marketName);

				/** Open? */
				boolean marketpened = true;
				String bidoOpenedString = element.getAttribute("open");
				marketpened = (bidoOpenedString != null ? Boolean
						.valueOf(bidoOpenedString) : marketpened);
				if (!marketpened) {
					MLog.w(marketName + " Opened:" + bidoOpenedString + "="
							+ marketpened);
					continue;
				}

				/** Packages null? */
				List<Element> packageElements = ElementUtils.getInstance()
						.elements(element, "app");
				if (packageElements == null || packageElements.size() < 1) {
					MLog.w(marketName + " packageElements:" + packageElements);
					continue;
				}

				/** numForMoblieRank */
				int _numForMoblieRank = numForMoblieRank;
				String numForMoblieRankS = element
						.getAttribute("numForMoblieRank");
				_numForMoblieRank = (numForMoblieRankS != null ? Integer
						.valueOf(numForMoblieRankS) : _numForMoblieRank);

				/** numForMoblieRank */
				int _numForWifiRank = numForWifiRank;
				String numForWifiRankS = element.getAttribute("numForWifiRank");
				_numForWifiRank = (numForWifiRankS != null ? Integer
						.valueOf(numForWifiRankS) : _numForWifiRank);

				/**
				 * Add market name and priority into the hashmap .
				 */
				List<Market> markets = new ArrayList<Market>();
				for (Element pElement : packageElements) {
					String packageName = pElement.getAttribute("package");
					MLog.v("package_name:" + packageName);
					Market market = (Market) Market.marketClassMap.get(
							marketName).newInstance();

					market.MARKET_NAME = marketName;
					market.PACKAGE_NAME = packageName;
					market.maxNumForNet = _numForMoblieRank;
					market.maxNumForWifi = _numForWifiRank;
					markets.add(market);
				}
				map.put(marketName, markets);
			}
			return map;
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param array
	 * @param string
	 * @return if the array contain the string
	 */
	@Deprecated
	protected static boolean containsStringInArray(String[] array, Object string) {
		try {
			for (String a : array) {
				MLog.i("containsStringInArray=>" + a + ":" + string);
				if (a.equals(String.valueOf(string))) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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

	@Deprecated
	protected static List<? extends Market> analyzeMarketElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			MLog.v("analyzeMarketElement:" + element);
			if (element != null) {
				/**
				 * Apps
				 */
				Iterator<Element> elements = ElementUtils.getInstance()
						.elementIterator(element, "app");
				if (elements == null) {
					MLog.w("This market didn't have apps , so return.");
					return null;
				}

				/**
				 * market 参数
				 */
				List<Attribute> marketAttributes = ElementUtils.getInstance()
						.attributes(element);

				while (elements.hasNext()) {
					Element pElement = elements.next();
					List<Attribute> attributes = ElementUtils.getInstance()
							.attributes(pElement);
					if (marketAttributes != null && attributes != null) {
						attributes.addAll(marketAttributes);
					}

					Market market = null;
					/**
					 * Package Name
					 */
					String pkgNameS = pElement.getAttribute("package");
					if (pkgNameS == null) {
						MLog.w("pElement pkgNameS:" + pkgNameS);
						continue;
					}

					if (tacticsMarkets == null) {
						MLog.w("tacticsMarkets is null ,return");
						break;
					}
					for (int i = 0; i < tacticsMarkets.size(); i++) {
						if (tacticsMarkets.get(i).getPackageName()
								.equals(pkgNameS)) {
							market = tacticsMarkets.get(i);
						}
					}
					MLog.d("package name:" + pkgNameS);
					if (market == null) {
						MLog.w("Null, continue;");
						continue;
					}

					for (Attribute attribute : attributes) {
						String key = attribute.getName();
						String value = attribute.getValue();
						MLog.d(market.MARKET_NAME + market.PACKAGE_NAME
								+ " put key:" + key + " value:" + value);
						market.params.put(key, value);
						// 处理关键字
						if ("keyword".equals(key)) {
							market.setAppKeywords(cutParamsWithSeperator(value));
						}
					}
					MLog.i("Param is " + market.MARKET_NAME + "-"
							+ market.PACKAGE_NAME);
				}
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Analyze Bido
	 * 
	 * @return
	 */
	@Deprecated
	protected static List<MarketBido> analyzeMarketBidoElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketBido m = (MarketBido) market;
					m.setScoreParms(m.params.get("scoreParams"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Analyze HiAPK
	 * 
	 * @return
	 */
	@Deprecated
	protected static List<MarketHiAPK> analyzeMarketHiApkElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketHiAPK> analyzeMarketZhihuiyunElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketGfan> analyzeMarketGfanElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {

			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketGfan m = (MarketGfan) market;
					m.setGfanClientVersionCode(m.params
							.get("gfanClientVersionCode"));
					m.setGfanClientVersionName(m.params
							.get("gfanClientVersionName"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Lenovo
	 * 
	 * @param context
	 * @param tacticsMarkets
	 * @param element
	 * @return
	 */
	@Deprecated
	protected static List<MarketLenovo> analyzeMarketLenovoElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketMZ> analyzeMarketMZElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketMZ m = (MarketMZ) market;
					m.setCookie(m.params.get("cookie"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param context
	 * @param tacticsMarkets
	 * @param element
	 * @return
	 */
	@Deprecated
	protected static List<MarketOppo> analyzeMarketOppoElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketOppo m = (MarketOppo) market;
					m.setAPP_ID(m.params.get("appId"));
					m.setDetailParam(m.params.get("detailParam"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketNDuoa> analyzeMarketNduoaElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketNDuoa m = (MarketNDuoa) market;
					m.setAPP_ID(m.params.get("appId"));
					m.setKey(m.params.get("key"));
					m.setVersionCode(m.params.get("versionCode"));
					m.setVersionName(m.params.get("versionName"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}

		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketXiaomi> analyzeMarketXiaomiElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<Market360> analyzeMarket360Element(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					Market360 m = (Market360) market;
					String im = m.params.get("im");
					String[] array = im.split("\\|");
					m.setParam_mid(array[0]);
					m.setParam_m2(array[1]);
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketMumayi> analyzeMarketMumayiElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketMumayi m = (MarketMumayi) market;
					m.setAPP_ID(m.params.get("appId"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketCoolSmart> analyzeMarketCoolMartElement(
			Context context, List<? extends Market> tacticsMarkets,
			Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketCoolSmart m = (MarketCoolSmart) market;
					m.setAPP_ID(m.params.get("appId"));
					m.setResourceVesionId(m.params.get("resourceVersionId"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketZTE> analyzeMarketZTEElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketZTE m = (MarketZTE) market;
					m.setAPP_ID(m.params.get("appId"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static List<MarketSohu> analyzeMarketSohuElement(Context context,
			List<? extends Market> tacticsMarkets, Element element) {
		try {
			if (element != null && tacticsMarkets != null) {
				for (Market market : tacticsMarkets) {
					MarketSohu m = (MarketSohu) market;
					m.setAPP_ID(m.params.get("appId"));
				}
				MLog.v("analyzeMarket" + tacticsMarkets.get(0).MARKET_NAME
						+ " Element:");
			}
		} catch (Exception e) {
			MLog.e(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Deprecated
	protected static Market getTheTargetAppNew(Context context,
			HashMap<String, List<? extends Market>> targetApps) {
//		try {
//			List<Market> apps = new ArrayList<Market>();
//			Set<String> keys = targetApps.keySet();
//
//			/**
//			 * Step1.put all the applications into #apps
//			 */
//			for (String marketPriorty : keys) {
//				List<Market> ap = (List<Market>) targetApps.get(marketPriorty);
//				if (ap != null && ap.size() > 0) {
//					apps.addAll(ap);
//				}
//			}
//
//			/**
//			 * Step2.make all avaliable applications into a data constructor
//			 * */
//			SharedPreferences sp_control = context.getSharedPreferences(
//					Market.PREFNAME_CONTROL, Context.MODE_PRIVATE);
//			SharedPreferences sp = context.getSharedPreferences(
//					Market.PREFNAME, Context.MODE_PRIVATE);
//
//			List<Market> avaliableApps = new ArrayList<Market>();
//			for (Market market : apps) {
//				// If the rank control is null or no rank control,break
//
//				boolean jinqueAvaliable = sp_control.getBoolean(
//						market.getSharePrfKeyForRankControl(), false);
//				MLog.d("jingqueAvaliable:" + jinqueAvaliable);
//
//				// If the app rank over ,then return. Max rank number
//				int maxNumForAday = market.getMaxNumForNet();
//				String apn = u.getAPN(context);
//				if (apn != null && apn.toLowerCase().equals("wifi")) {
//					maxNumForAday = market.getMaxNumForWifi();
//				}
//				boolean rank_over = false;
//				try {
//					String rankCountKey = market
//							.getSharePrfKeyForRankCount(context);
//					int downloadCount = sp.getInt(rankCountKey, 0);
//					rank_over = (downloadCount >= maxNumForAday);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				// a..............
//				if (jinqueAvaliable && !rank_over) {
//					MLog.e("avaliableApp:" + market);
//					avaliableApps.add(market);
//				}
//			}
//			if (avaliableApps.size() > 0) {
//				MLog.e("avaliableApps size:" + avaliableApps.size());
//				return avaliableApps.get(((int) (Math.random() * avaliableApps
//						.size())));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return null;
	}
}
