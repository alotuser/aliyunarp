package com.aliyunarp.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

public class TestUtil {

	
	static void printPrefixedAddresses(String addressStr) {
		  IPAddressString ipAddressString = new IPAddressString(addressStr);
		  IPAddress address = ipAddressString.getAddress();
		  System.out.println("count: " + address.getCount());
		  IPAddress hostAddress = ipAddressString.getHostAddress();
		  IPAddress prefixBlock = address.toPrefixBlock();
		  Integer prefixLength = ipAddressString.getNetworkPrefixLength();  
		  System.out.println(address);
		  System.out.println(address.toCanonicalWildcardString());
		  System.out.println(hostAddress);
		  System.out.println(prefixLength);
		  System.out.println(prefixBlock);
		  System.out.println();
		}
	
	public static void main(String[] args) {
		try {
			String ip="192.168.10.17";
			InetAddress  addip = InetAddress.getByName(ip);
			String hn=addip.getHostName();
			 //获取登录过的设备
            if (!ip.equals(hn)) {
                //检查设备是否在线，其中1000ms指定的是超时时间
                boolean status = InetAddress.getByName(addip.getHostName()).isReachable(1000);     // 当返回值是true时，说明host是可用的，false则不可。
                System.out.println("IP地址为:" + ip + "\t\t设备名称为: " + addip.getHostName() + "\t\t是否可用: " + (status ? "可用" : "不可用"));
            }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
