package com.fgnb.utils;

import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class ShellExecutor {


    /**
     * 执行命令
     * @param cmd
     * @throws IOException
     */
    public static void exec(String cmd) throws IOException {
        new DefaultExecutor().execute(CommandLine.parse(cmd));
    }

    /**
     * 执行命令返回执行结果
     * @param cmd
     * @return
     * @throws IOException
     */
    public static String execReturnResult(String cmd) throws IOException {

        CommandLine commandLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);

        executor.setStreamHandler(pumpStreamHandler);
        try {
            executor.execute(commandLine);
            return outputStream.toString() + errorStream.toString();
        }catch (IOException e){
            throw e;
        }finally {
            outputStream.close();
            errorStream.close();
        }

    }

    /**
     * 执行命令 返回watchdog watchdog可杀掉执行的进程
     * @param cmd
     * @return
     * @throws IOException
     */
    public static ExecuteWatchdog execReturnWatchdog(String cmd) throws IOException {
        CommandLine commandLine = CommandLine.parse(cmd);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.execute(commandLine,new DefaultExecuteResultHandler());
        return watchdog;
    }
}
