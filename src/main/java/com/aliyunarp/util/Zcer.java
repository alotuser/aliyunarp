package com.aliyunarp.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

public class Zcer {

	
	public static void main(String[] args) {
		
		
		String msg= RuntimeUtil.execForStr(newMacStr());
		//newName();
		restart();
		System.out.println(msg);
		
	}
	
	
	public static void restart() {
		RuntimeUtil.execForStr("netsh interface set interface WLAN disabled");
		RuntimeUtil.execForStr("netsh interface set interface WLAN enabled");
	}
	
	
	public static void newName() {
		
		String mac= MacUtil.randomMac().replace(":", "").toUpperCase();
		
		
		RuntimeUtil.execForStr("Rename-Computer -"+mac);
		
	}
	
	
	
	
	/**
	 * 无线网的MAC值的第二个数只能是2 、6、A、E中的一个，否则修改就不会起作用，如060C29E7B28C。
	 * @return
	 */
	public static String newMacStr() {
		
		String mac= MacUtil.randomMac().replace(":", "").toUpperCase();
		char c=RandomUtil.randomChar("26AE");
		String[] mcs= StrUtil.cut(mac, 1);
		mcs[1]=c+"";
		
		String nmac= String.join("", mcs);
		System.out.println(nmac);
		return getCmdStr("NetworkAddress",nmac);
	}
	
	/**
	 * 
	 * @param k
	 * @param v
	 * @return
	 */
	public static  String getCmdStr(String k,String v) {
		String pre="reg add ";
		String key="HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e972-e325-11ce-bfc1-08002be10318}\\0001";
		String cmds=pre+key+ " /v " + k + " /d " +v+ " /f";
		return cmds;
	}
	
	
	
}
