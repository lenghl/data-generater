package com.dbapp.data.generater;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.dbapp.data.generater.Util.getFileList;

/**
 * 描述:
 * 数据下载sftp
 *
 * @author lenghl
 * @create 2018-12-12 9:46
 */
public class DataDownload {

    private static final Logger logger = LogManager.getLogger(DataDownload.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String config = "conf.json";
    private static final String status = "status.json";
    private static String rootDir = System.getProperty("user.dir");
    private static final ExecutorService THREAT_POOL = new ThreadPoolExecutor(
            1, 1,
            5 * 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        logger.info("main函数的参数: {}", String.join(",", args));
        if (args.length == 1) {
            rootDir = args[0];
        }
        String configPath = rootDir + File.separator + config;
        File config = new File(configPath);
        List<HostDirConf> hostDirConfs = null;
        if (!config.exists()) {
            logger.error("当前配置文件 {} 不存在! 请设置配置文件.", configPath);
            return;
        }
        logger.info("当前配置文件地址: {}", configPath);
        try {
            hostDirConfs = objectMapper.readValue(config, new TypeReference<List<HostDirConf>>(){});
        } catch (IOException e) {
            logger.error("读取配置文件异常, 请检查配置文件.", e);
            return;
        }

        for (;;) {
            for (HostDirConf hostDirConf : hostDirConfs) {
                THREAT_POOL.submit(() -> downloadDataFromHost(hostDirConf));
            }

            logger.info("开始睡眠{}s", hostDirConfs.get(0).getSleepTime());
            try {
                Thread.sleep(1000 * Integer.parseInt(hostDirConfs.get(0).getSleepTime()));
            } catch (InterruptedException e) {
                logger.error("睡眠异常", e);
            }
            logger.info("睡眠{}s结束", hostDirConfs.get(0).getSleepTime());
        }
    }
    private static Map<String, ChannelSftp> map = new HashMap<>();

    private static void downloadDataFromHost(HostDirConf hostDirConf) {
        /** 以下为循环过程 */
        ChannelSftp sftp = null;
        String mapkey = hostDirConf.getHost() + hostDirConf.getUserName();
        try {
            sftp = map.get(mapkey);

            String srcDirectory = hostDirConf.getSrcDirectory();
            String destDirectory = hostDirConf.getDestDirectory();
            String md5 = md5Password(hostDirConf.getHost() + hostDirConf.getDestDirectory());
            HostDirStatus hostDirStatus = getHostStatus().stream().filter(s -> md5.equals(s.getId())).findFirst()
                    .orElse(new HostDirStatus(md5, hostDirConf.getHost(), hostDirConf.getSrcDirectory()));
            logger.debug("主机:{}, 目录:{}, 已经下载完毕的文件:{}",
                    hostDirConf.getHost(), hostDirConf.getDestDirectory(), String.join(",", hostDirStatus.getFileNames()));

            if (sftp == null || sftp.isClosed() || !sftp.isConnected()) {
                logger.info("初始化sftp客户端, 主机账号: {}", mapkey);
                JSch ssh = new JSch();
                Session session = ssh.getSession(hostDirConf.getUserName(), hostDirConf.getHost(), 22);
                Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword(hostDirConf.getPassWord());

                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();
                sftp = (ChannelSftp) channel;
                map.put(mapkey, sftp);
            }

            List<String> filePathList = getFileList(sftp, srcDirectory);
            logger.debug("{}目录地址的所有文件: {}", srcDirectory, String.join(",", filePathList));
            Set<String> downloadedFiles = new HashSet<>(hostDirStatus.getFileNames());
            filePathList = filePathList.stream()
                    .filter(filePath -> !downloadedFiles.contains(filePath))
                    .filter(filePath -> filePath.contains(".tar.gz"))
                    .collect(Collectors.toList());
            logger.debug("{}目录地址需要下载的所有文件: {}", srcDirectory, String.join(",", filePathList));
            sftp.cd(srcDirectory);

            for (String srcFilePath : filePathList) {
                logger.info("开始下载主机: {}上的文件: {}", hostDirConf.getHost(), srcFilePath);
                String destFilePath = destDirectory + File.separator + new File(srcFilePath).getName();
                File destDir = new File(destDirectory);
                boolean dirExists = false;
                dirExists = destDir.exists();
                boolean fileExists = false;
                if (!dirExists) {
                    dirExists = destDir.mkdirs();//创建目录
                }
                if (dirExists) {
                    new File(destFilePath).delete();
                    fileExists = new File(destFilePath).createNewFile();
                }

                if (!fileExists) {
                    logger.error("本地文件创建异常, 请检查本地输出文件目录是否存在!");
                    continue;
                }
                appendFileToStatus(md5, hostDirConf.getHost(), hostDirConf.getSrcDirectory(), srcFilePath);
                OutputStream out = new FileOutputStream(destFilePath);
                sftp.get(srcFilePath, out);
                out.flush();
                out.close();
                //unCompressArchiveGz(destFilePath);
                logger.info("下载主机: {}上的文件{} 结束", hostDirConf.getHost(), srcFilePath);
            }
        } catch (Exception e) {
            if (sftp != null && !sftp.isConnected()) {
                sftp.disconnect();
                sftp = null;
                map.put(mapkey, null);
            }
            logger.error("导出数据文件异常! 主机:" + hostDirConf.getHost() + ", 目录:" + hostDirConf.getSrcDirectory(), e);
        }
    }

    /**
     * 获取历史状态信息.
     * @return
     */
    private static synchronized List<HostDirStatus> getHostStatus() {
        String statusPath = rootDir + File.separator + status;
        File statusFile = new File(statusPath);
        if (!statusFile.exists()) {
            return new ArrayList<>();
        }
        List<HostDirStatus> statuses = new ArrayList<>();
        try {
            statuses = objectMapper.readValue(statusFile, new TypeReference<List<HostDirStatus>>(){});
        } catch (IOException e) {
            logger.error("读取历史状态文件异常! 请检查文件:" + statusPath, e);
        }
        return statuses;
    }


    /**
     * 向md5历史状态中追加fileName
     * @param md5
     * @param fileName
     */
    private static synchronized void appendFileToStatus(String md5, String host, String srcDir, String fileName) {
        String statusPath = rootDir + File.separator + status;
        List<HostDirStatus> status = getHostStatus();
        HostDirStatus hostDirStatus = status.stream().filter(s -> md5.equals(s.getId())).findFirst().orElse(null);
        if (hostDirStatus == null) {
            hostDirStatus = new HostDirStatus(md5, host, srcDir);
            status.add(hostDirStatus);
        }
        hostDirStatus.getFileNames().add(fileName);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(statusPath), status);
        } catch (IOException e) {
            logger.error("写入历史状态文件异常! 请检查文件:" + statusPath, e);
        }
    }

    /**
     * 生成32位md5码
     * @param content
     * @return
     */
    private static String md5Password(String content) {
        try {
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(content.getBytes());
            StringBuffer buffer = new StringBuffer();
            // 把每一个byte 做一个与运算 0xff;
            for (byte b : result) {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }

            // 标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("获取" + content + " md5值失败!", e);
            return "";
        }
    }

}
