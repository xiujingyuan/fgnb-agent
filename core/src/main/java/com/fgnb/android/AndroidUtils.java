package com.fgnb.android;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.RawImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class AndroidUtils {

    private static final String CPU_INFO_SHELL = "cat /proc/cpuinfo |grep Hardware";
    private static final String MEM_SIZE_SHELL = "cat /proc/meminfo |grep MemTotal";
    private static final String IP_SHELL = "ip addr show |grep inet |grep -v inet6 |grep -v \"127.0.0.1\"";
    private static final String RESOLUTION_SHELL = "wm size";
    private static Map<String,String> sdkMap = new HashMap();

    static {
        sdkMap.put("8","2.2");
        sdkMap.put("10","2.3");
        sdkMap.put("14","4.0");
        sdkMap.put("15","4.0.3");
        sdkMap.put("16","4.1.2");
        sdkMap.put("18","4.3.1");
        sdkMap.put("19","4.4.2");
        sdkMap.put("20","4.4w.2");
        sdkMap.put("21","5.0.1");
        sdkMap.put("22","5.1.1");
        sdkMap.put("23","6.0");
        sdkMap.put("24","7.0");
        sdkMap.put("25","7.1.1");
        sdkMap.put("26","8.0");
        sdkMap.put("27","8.1.0");
    }

    /**
     * 获取分辨率
     */
    public static String getResolution(IDevice iDevice){
        try {
            return executeShellCommand(iDevice,RESOLUTION_SHELL).split(":")[1].trim();
        } catch (Exception e) {
            log.error("获取分辨率出错",e);
            return "分辨率获取失败";
        }
    }

    /**
     * 获取CPU信息
     * @return
     */
    public static String getCpuInfo(IDevice iDevice){
        try {
            String output = executeShellCommand(iDevice,CPU_INFO_SHELL);
            if(!StringUtils.isEmpty(output)){
                return output.split(":")[1].trim();
            }
        }catch (Exception e){
            log.error("CPU信息获取失败",e);
        }
        return "CPU信息获取失败";
    }

    /**
     * 等待设备上线
     * @param maxWaitTimeSeconds
     * @throws Exception
     */
    public static void waitForDeviceOnline(IDevice iDevice,long maxWaitTimeSeconds) throws Exception{
        long startTime = System.currentTimeMillis();
        while(true){
            if(System.currentTimeMillis()-startTime>maxWaitTimeSeconds*1000){
                log.error("[{}]设备在{}秒内没有上线",iDevice.getSerialNumber(),maxWaitTimeSeconds);
                throw new Exception("设备未上线");
            }
            if(iDevice.isOnline()){
                return;
            }
            Thread.sleep(1000);
        }
    }

    /**
     * 获取内存信息
     * @return
     */
    public static String getMemSize(IDevice iDevice){
        try {
            String output = executeShellCommand(iDevice,MEM_SIZE_SHELL);
            if(!StringUtils.isEmpty(output)){
                String kB = (output.replaceAll(" ", "")).replaceAll("\n","").replaceAll("\r","").split(":")[1];
                kB = kB.substring(0,kB.length()-2);
                //向上取整
                double ceil = Math.ceil(Long.parseLong(kB)/(1024.0*1024));
                return ceil+" GB";
            }
        }catch (Exception e){
            log.error("内存信息获取失败",e);
        }
        return "内存信息获取失败";
    }

    /**
     * 获取IP地址
     * @return
     */
    public static String getIp(IDevice iDevice) {
        try {
            String output = executeShellCommand(iDevice,IP_SHELL);
            if(!StringUtils.isEmpty(output)){
                return output.split("/")[0].trim().split(" ")[1];
            }
        } catch (Exception e) {
            log.error("获取设备IP失败",e);
        }
        return "获取设备IP失败";
    }

    /**
     * 设备名
     * @return
     */
    public static String getDeviceName(IDevice iDevice){
        return iDevice.getProperty("ro.product.brand") + " " + iDevice.getProperty("ro.product.model");
    }

    /**
     * 安卓版本
     * @return
     */
    public static String getAndroidVersion(IDevice iDevice){
        return sdkMap.get(getApiLevel(iDevice));
    }

    /**
     * 截屏
     * @return
     * @throws Exception
     */
    public static File screenshot(IDevice iDevice) throws Exception{
        RawImage rawImage = iDevice.getScreenshot();
        //rawImage转换为Image
        BufferedImage image = new BufferedImage(rawImage.width,rawImage.height,BufferedImage.TYPE_INT_ARGB);
        int index = 0;
        int IndexInc = rawImage.bpp >> 3;
        for (int y = 0 ; y < rawImage.height ; y++) {
            for (int x = 0 ; x < rawImage.width ; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                image.setRGB(x, y, value);
            }
        }
        File file = new File(iDevice.getSerialNumber()+".png");
        ImageIO.write(image, "png", file);
        return file;
    }

    /**
     * 获取CPU架构
     * @return
     */
    public static String getCpuAbi(IDevice iDevice) {
        return iDevice.getAbis().get(0);
    }

    /**
     * 获取手机sdk版本
     * @return
     */
    public static String getApiLevel(IDevice iDevice){
        return iDevice.getProperty("ro.build.version.sdk");
    }

    /**
     * 检查是否安装了app
     * @param iDevice
     * @param packageName 包名
     * @return
     * @throws Exception
     */
    public static boolean hasInstalledApp(IDevice iDevice,String packageName) throws Exception{
        String result = executeShellCommand(iDevice, "pm list packages|grep " + packageName);
        log.info("[{}]是否安装了{} => {}",iDevice.getSerialNumber(),packageName,result);
        if(StringUtils.isEmpty(result)){
            return false;
        }
        return true;
    }

    /**
     * 检查APP是否在运行
     * @param iDevice
     * @param packageName
     * @return
     * @throws Exception
     */
    public static boolean isAppRunning(IDevice iDevice,String packageName) throws Exception{
        String result = executeShellCommand(iDevice,"ps |grep "+packageName);
        if(StringUtils.isEmpty(result)){
            return false;
        }
        return true;
    }

    /**
     * 强制关闭app
     * @param iDevice
     * @param packageName
     * @throws Exception
     */
    public static void forceStopApp(IDevice iDevice,String packageName) throws Exception {
        iDevice.executeShellCommand("am force-stop "+packageName,new NullOutputReceiver());
    }

    /**
     * 执行命令
     * @param cmd
     * @return
     * @throws Exception
     */
    public static String executeShellCommand(IDevice iDevice, String cmd) throws Exception{
        CollectingOutputReceiver collectingOutputReceiver = new CollectingOutputReceiver();
        iDevice.executeShellCommand(cmd,collectingOutputReceiver);
        return collectingOutputReceiver.getOutput();
    }

}
