import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CatchDataTool tool = new CatchDataTool();
        while (true) {
            System.out.println("请输入捕获时间:(单位:s)");
            int time = scanner.nextInt();
            tool.setTime(time);
            tool.packetCap();
        }
    }
}
