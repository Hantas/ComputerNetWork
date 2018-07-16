import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.*;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class CatchDataTool implements PacketReceiver {

    //网络接口
    private NetworkInterface[] devices;
    //捕获包
    private JpcapCaptor captor;
    //捕获时间
    private int time;
    private String start;
    private String end;

    private static List<DataPacket> tcp_list = new ArrayList<>();
    private static List<DataPacket> udp_list = new ArrayList<>();

    public void setTime(int time) {
        this.time = time;
    }

    public CatchDataTool() {
        //获得网络接口列表
        devices = JpcapCaptor.getDeviceList();
    }

    public void packetCap() {
        try {
            System.out.println("正在捕获中...");
            start = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            //打开一个特别的网络接口并返回实例
            captor = JpcapCaptor.openDevice(devices[3], 65535, false, 20);
            captor.setFilter("ip", true);
            //实时捕获,第一个参数代表捕获的数量,若为-1则一直捕获
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(time * 1000);
                        captor.breakLoop();
                        end = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        throw new InterruptedException();
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
            captor.loopPacket(-1, this);
            System.out.println("时间片: " + start + "-" + end);
            System.out.println("请设置时间片大小:(单位:s)");
            TimeSplit.setSection(new Scanner(System.in).nextInt());
            List<String> timeSlice = TimeSplit.timeSplit(start, end);
            AnalysisTool.analysis(udp_list, tcp_list, start, timeSlice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivePacket(Packet packet) {
        int protocol = ((IPPacket) packet).protocol;
        if (protocol == 17) {
            udp_list.add(AnalysisTool.getDataPacket(packet));
        } else if (protocol == 6) {
            tcp_list.add(AnalysisTool.getDataPacket(packet));
        }
    }
}
