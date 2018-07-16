import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

public class DataPacket {
    public String time;
    public boolean up_or_down;
    public int upload_bit;
    public int download_bit;
    public Packet packet;

    public DataPacket(String time, boolean up_or_down, int upload_bit, int download_bit, Packet packet) {
        this.time = time;
        this.up_or_down = up_or_down;
        this.upload_bit = upload_bit;
        this.download_bit = download_bit;
        this.packet = packet;
    }

    @Override
    public String toString() {
        if (packet.getClass().equals(UDPPacket.class))
            return ((UDPPacket)packet).toString();
        else
            return ((TCPPacket)packet).toString();
    }
}
