package com.fgnb.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by jiangyitao.
 * 网络工具类
 */
@Slf4j
public class NetUtil {

    /**
     * 检测本地端口是否可用
     *
     * @param port
     *            端口号
     * @return 如果可用返回true,不可用返回false
     */
    public static boolean isPortAvailable(int port) {
        try {
            bindPort("127.0.0.1", port);
            bindPort("0.0.0.0", port);
            return true;
        } catch (IOException e) {
            log.warn("检测端口:" + port + " 被占用");
        }
        return false;
    }

    private static void bindPort(String host, int port) throws IOException {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }

    /**
     * 获取本机ip
     * @return
     */
    public static String getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress().toString();
    }
}
