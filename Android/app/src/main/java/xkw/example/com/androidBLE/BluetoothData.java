package xkw.example.com.androidBLE;

/**
 *
 * @author xkw
 * @date 2018/1/13
 */

public class BluetoothData {
    /**
     * 字符串常量，Intent中的数据
     */
    public final static String DATA = "DATA";
    /**
     * 字符串常量，存放在Intent中的设备对象
     */
    public static final String DEVICE = "DEVICE";
    /**
     * 此程序使用的UUID
     */
    static String DEVICE_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    static String DEVICE_CHARACTERISTIC_SERVICE = "0000ffe1-0000-1000-8000-00805f9b34fb";

    /**
     * 发送的数据
     */
    public static String SEND_DATA = "send";
    public static String CONNECT_SUC = "star";

    /**
     * 读写文件的文件名
     */
    public final static String FILE_NAME = "file.txt";
}
