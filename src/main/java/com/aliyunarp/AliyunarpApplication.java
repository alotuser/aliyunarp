package com.aliyunarp;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.aliyunarp.arp.Arp;
import com.aliyunarp.arp.ArpInfo;

import cn.hutool.core.util.ZipUtil;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;

@SpringBootApplication
public class AliyunarpApplication {

	public static void main(String[] args) throws InterruptedException {
		// SpringApplication.run(AliyunarpApplication.class, args);

		try {

			
			
			
			
			
			
			
//			String arpIp = "192.168.10.17";
//			// 枚举网卡并打开设备
//			NetworkInterface[] devices = JpcapCaptor.getDeviceList();
//			for (int i = 0; i < devices.length; i++) {
//				System.out.println(i + "." + devices[i].description);
//			}
//			System.out.print("\n选择一个网卡：");
//			NetworkInterface device = devices[7];//2
//
//			System.out.println("\n-------------------------------------------------\n");
//
//			Arp arp=new Arp();
//			
//			arp.goodTime(device,new ArpInfo(arpIp,"a4-50-46-d8-2b-5e"),new ArpInfo(arpIp,"2A-D0-37-BC-9D-99"));
//			
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	
	
	
	
	

	
}
