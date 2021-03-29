package com.aliyunarp.arp;

public class ArpInfo {

	private String ip;
	private String mac;
	private String name;
	
	public ArpInfo() {
		super();
	}
	public ArpInfo(String mac) {
		super();
		this.mac = mac;
	}
	public ArpInfo(String ip, String mac) {
		super();
		this.ip = ip;
		this.mac = mac;
	}


	public ArpInfo(String ip, String mac, String name) {
		super();
		this.ip = ip;
		this.mac = mac;
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	
	
}
