import javafx.scene.chart.PieChart;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalysisTool {

    private static InetAddress host_address;
    private static Map<Integer, DataPacket> map = new HashMap<>();

    static {
        try {
            host_address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static DataPacket getDataPacket(Packet packet) {
        if (packet.getClass().equals(UDPPacket.class)) {
            UDPPacket udpPacket = (UDPPacket) packet;
            boolean isUpload = udpPacket.src_ip.equals(host_address);
            return isUpload ? new DataPacket(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), isUpload, udpPacket.caplen, 0, packet) : new DataPacket(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), isUpload, 0, udpPacket.caplen, packet);
        } else {
            TCPPacket tcpPacket = (TCPPacket) packet;
            boolean isUpload = tcpPacket.src_ip.equals(host_address);
            return isUpload ? new DataPacket(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), isUpload, tcpPacket.caplen, 0, packet) : new DataPacket(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), isUpload, 0, tcpPacket.caplen, packet);
        }

    }

    public static void analysis(List<DataPacket> udp_list, List<DataPacket> tcp_list, String start, List<String> timeSplit) {
        System.out.println("主机号:" + host_address);
        for (String upper : timeSplit) {
            //UDP解析
            List<DataPacket> udp_lower = udp_list.stream().takeWhile(DataPacket -> TimeSplit.compare(DataPacket.time, upper)).collect(Collectors.toList());
            udp_list = udp_list.stream().skip(udp_lower.size()).collect(Collectors.toList());
            List<DataPacket> tcp_lower = tcp_list.stream().takeWhile(DataPacket -> TimeSplit.compare(DataPacket.time, upper)).collect(Collectors.toList());
            tcp_list = tcp_lower.stream().skip(tcp_lower.size()).collect(Collectors.toList());
            System.out.println("时间片:" + start + "-" + upper);
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            start = upper;
            System.out.println("UDP协议");
            System.out.println("包个数:" + udp_lower.size());
            System.out.println("上传总量:" + udp_lower.stream().mapToInt((packet) -> packet.upload_bit).summaryStatistics().getSum() + "byte,下载总量:" + udp_lower.stream().mapToInt((packet) -> packet.download_bit).summaryStatistics().getSum() + "byte");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            //udp_lower.forEach(System.out::println);
            //TCP解析
            System.out.println("TCP协议");
            System.out.println("包个数:" + tcp_lower.size());
            Map<Integer, DataPacket> map = new HashMap<>();
            for (DataPacket dataPacket : tcp_lower) {
                TCPPacket tcpPacket = (TCPPacket) (dataPacket.packet);
                int hash = getHash(tcpPacket.src_ip, tcpPacket.dst_ip, tcpPacket.src_port, tcpPacket.dst_port);
                if (map.get(hash) != null) {
                    DataPacket old = map.get(hash);
                    old.download_bit += dataPacket.download_bit;
                    old.upload_bit += dataPacket.upload_bit;
                    map.put(hash, old);
                } else
                    map.put(hash, dataPacket);
            }
            Set<Integer> hashes = map.keySet();
            for (Integer hash : hashes) {
                DataPacket dataPacket = map.get(hash);
                TCPPacket tcpPacket = (TCPPacket)(dataPacket.packet);
                InetAddress dst_ip;
                int src_port;
                int dst_port;
                if (tcpPacket.src_ip.equals(host_address)){
                    dst_ip = tcpPacket.dst_ip;
                    src_port = tcpPacket.src_port;
                    dst_port = tcpPacket.dst_port;
                }else {
                    dst_ip = tcpPacket.src_ip;
                    src_port = tcpPacket.dst_port;
                    dst_port = tcpPacket.src_port;
                }
                System.out.println(host_address.getHostAddress() + "\t" + src_port + " -----> " + dst_ip + "\t" + dst_port);
                System.out.println("上传总量:" + dataPacket.upload_bit + "byte,下载总量:" + dataPacket.download_bit + "byte");
                System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
            }
            System.out.println();
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        }
    }

    public static int getHash(InetAddress src_ip, InetAddress dst_ip, int src_port, int dst_port) {
        long s_ip = ipToLong(src_ip.toString().substring(1));
        long d_ip = ipToLong(dst_ip.toString().substring(1));
        long port = src_port * dst_port;
        long res = (s_ip ^ d_ip) ^ port;
        return (int) ((res & 0x00ff) ^ (res >> 16));
    }

    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }
}
