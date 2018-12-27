package xkw.example.com.androidBLE;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 *
 * @author xkw
 * @date 2017/12/27
 */
public class BLEDevice implements Serializable, Comparable {
    private String deviceName;
    private String deviceAddress;

    public BLEDevice(){

    }

    BLEDevice(String deviceName, String deviceAddress){
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
    }

    String getDeviceName(){
        return deviceName;
    }

    String getDeviceAddress(){
        return deviceAddress;
    }

    public void setDeviceName(String name){
        deviceName = name;
    }

    public void setDeviceAddress(String address){
        deviceAddress = address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BLEDevice){
            BLEDevice temp = (BLEDevice)obj;
            // each BLE module has a unique address
            return this.deviceAddress.equals(temp.deviceAddress);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof BLEDevice){
            BLEDevice device = (BLEDevice)o;
            if (device.getDeviceName() == null && deviceName == null){
                return 0;
            }
        }
        return 0;
    }
}
