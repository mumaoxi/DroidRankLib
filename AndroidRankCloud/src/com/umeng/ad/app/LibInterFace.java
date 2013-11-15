package com.umeng.ad.app;

import android.content.Context;

public interface LibInterFace {

	/**
	 * 打开开关
	 * 
	 * @param logEnable
	 */
	public void libEnableLog(boolean logEnable);

	/**
	 * 初始化参数
	 * 
	 * @param context
	 */

	public void libInit(Context context, String apikey, String channel);

	/**
	 * 开刷
	 * 
	 * @param context
	 */

	public void libStart(Context context, String apikey, String channel);

	/**
	 * 上传数据到服务器端
	 * 
	 * @param context
	 */

	public void libPostData(Context context, String apikey, String channel);
}
