package com.aliyunarp.util;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliyunarp.arp.ArpInfo;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;

public class NetsUtil {

	/**
	 * 执行单条指令
	 * 
	 * @param cmd 命令
	 * @return 执行结果
	 * @throws Exception
	 */
	public static String command(String cmd) throws Exception {
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		InputStream in = process.getInputStream();
		StringBuilder result = new StringBuilder();
		byte[] data = new byte[256];
		while (in.read(data) != -1) {
			String encoding = System.getProperty("sun.jnu.encoding");
			result.append(new String(data, encoding));
		}
		return result.toString();
	}

	/**
	 * 获取mac地址
	 * 
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static String getMacAddress(String ip){
		try {
			String result = command("arp -a " + ip);
			String regExp = "([0-9A-Fa-f]{2})([-:][0-9A-Fa-f]{2}){5}";
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(result);
			StringBuilder mac = new StringBuilder();
			while (matcher.find()) {
				String temp = matcher.group();
				mac.append(temp);
			}
			return mac.toString();
		} catch (Exception e) {
			
		}
		
		return null;
	}

	// 获取mac地址
	public static String getMacAddress() {
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			byte[] mac = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				if (netInterface.isLoopback() || netInterface.isVirtual() || netInterface.isPointToPoint()
						|| !netInterface.isUp()) {
					continue;
				} else {
					mac = netInterface.getHardwareAddress();
					if (mac != null) {
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < mac.length; i++) {
							sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
						}
						if (sb.length() > 0) {
							return sb.toString();
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return "";
	}

	// 获取ip地址
	public static String getIpAddress() {
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				if (netInterface.isLoopback() || netInterface.isVirtual() || netInterface.isPointToPoint()
						|| !netInterface.isUp()) {
					continue;
				} else {
					Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						ip = addresses.nextElement();
						if (ip != null && ip instanceof Inet4Address) {
							return ip.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return "";
	}

	
	public static String getWanIp(String ip) {
		String[] ips= StrUtil.split(ip, ".");
		ips[ips.length-1]="1";
		return String.join(".", ips);
	}
	
	public static String getRandomIp(String ip) {
		String[] ips= StrUtil.split(ip, ".");
		ips[ips.length-1]=(Convert.toInt(ips[ips.length-1])+RandomUtil.randomInt(1, 100))+"";
		return String.join(".", ips);
	}
	
	public static String getPcName(String ip) {
		String str = RuntimeUtil.execForStr("ping -n 1 -l 1 -a " + ip);
		return StrUtil.subBetween(str, "Ping", "[" + ip + "]");
	}

	
	public static String getMacAddressStr(String result) {
		
		StringBuilder mac = new StringBuilder();
		try {
			String regExp = "([0-9A-Fa-f]{2})([-:][0-9A-Fa-f]{2}){5}";
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(result);
			while (matcher.find()) {
				String temp = matcher.group();
				mac.append(temp);
			}
		} catch (Exception e) {
		}
		return mac.toString();
	}
	public static String getIpAddressStr(String result) {
		
		StringBuilder mac = new StringBuilder();
		try {
			String regExp = "([0-9]{1,3})([-:.][0-9]{1,3}){3}";
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(result);
			while (matcher.find()) {
				String temp = matcher.group();
				mac.append(temp);
			}
		} catch (Exception e) {
		}
		return mac.toString();
	}
	
	public static List<ArpInfo> getArpListInfo() {
		List<ArpInfo> ims=new ArrayList<ArpInfo>();
		
		List<String> lines = (RuntimeUtil.execForLines("arp -a"));
		boolean flag = false;
		int count = 0;

		for (String inline : lines) {
			if (inline.indexOf("接口") > -1) {
				flag = !flag;
				if (!flag) {
					// 碰到下一个"接口"退出循环
					break;
				}
			}
			if (flag) {
				count++;
				if (count > 2) {
					// 有效IP  String[] str = inline.split(" {4}");
					String mac= getMacAddressStr(inline);
					String ip=getIpAddressStr(inline);
					ArpInfo info= new ArpInfo(ip, mac); 
					ims.add(info);
				}
			}
		}
		
		return ims;
		
	}
	
//	public static void main(String[] args) {
//		
//		
//		//List<ArpInfo> lim= getArpListInfo();
//	
//		
//		System.out.println(StrUtil.similar("192.168.10.17", "17"));
//	}

	

}
