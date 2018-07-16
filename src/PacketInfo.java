import java.net.InetAddress;

public class PacketInfo {
    public InetAddress src_ip;
    public InetAddress dst_ip;
    public int src_port;
    public int dst_port;

    public PacketInfo(InetAddress src_ip, InetAddress dst_ip, int src_port, int dst_port) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.src_port = src_port;
        this.dst_port = dst_port;
    }
}
