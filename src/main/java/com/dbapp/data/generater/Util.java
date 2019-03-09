package com.dbapp.data.generater;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 描述:
 * 工具类
 *
 * @author lenghl
 * @create 2018-12-12 13:37
 */
public class Util {
    private static final Logger logger = LogManager.getLogger(Util.class);

    /**
     * 解压tar
     * @param finalName
     * @author yutao
     * @throws IOException
     * @date 2017年5月27日下午4:34:41
     */
    public static void unCompressTar(String finalName) throws IOException {
        File file = new File(finalName);
        String parentPath = file.getParent();
        TarArchiveInputStream tais = new TarArchiveInputStream(new FileInputStream(file));
        TarArchiveEntry tarArchiveEntry = null;
        while((tarArchiveEntry = tais.getNextTarEntry()) != null){
            String name = tarArchiveEntry.getName();
            File tarFile = new File(parentPath, name);
            if(!tarFile.getParentFile().exists()){
                tarFile.getParentFile().mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tarFile));

            int read = -1;
            byte[] buffer = new byte[1024];
            while((read = tais.read(buffer)) != -1){
                bos.write(buffer, 0, read);
            }
            bos.close();
        }
        tais.close();
        logger.info("删除已被解压缩的文件: {}", finalName);
        file.delete();//删除tar文件
    }

    /**
     * 解压
     * @param archive
     * @author yutao
     * @throws IOException
     * @date 2017年5月27日下午4:03:29
     */
    public static void unCompressArchiveGz(String archive) throws IOException {
        File file = new File(archive);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        String finalName = file.getParent() + File.separator + fileName;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(finalName));
        GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis);

        byte[] buffer = new byte[1024];
        int read = -1;
        while((read = gcis.read(buffer)) != -1){
            bos.write(buffer, 0, read);
        }
        gcis.close();
        bos.close();
        file.delete();//删除tar.gz文件
        logger.info("删除已被解压缩的文件: {}", archive);
        unCompressTar(finalName);
    }

    /**
     * 返回多级目录下的文件
     * @param sftp
     * @param srcDirectory
     * @return
     * @throws SftpException
     */
    public static List<String> getFileList(ChannelSftp sftp, String srcDirectory) throws SftpException {
        List<String> fileNameList = new ArrayList<>();
        logger.debug("遍历{}目录", srcDirectory);
        Vector fileList = sftp.ls(srcDirectory);
        Iterator it = fileList.iterator();
        while(it.hasNext()) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry)it.next();
            String fileName = entry.getFilename();
            if(".".equals(fileName) || "..".equals(fileName)){
                continue;
            }
            if (isDirectory(entry)) {
                fileNameList.addAll((getFileList(sftp, srcDirectory + "/" + fileName)));
            } else {
                fileNameList.add(srcDirectory + "/" + fileName);
            }
        }
        return fileNameList;
    }

    private static boolean isDirectory(ChannelSftp.LsEntry entry) {
        return !entry.getLongname().trim().startsWith("-");
    }

//    public static void main(String[] args) throws IOException {
//        String s = "//Output_msg/Event_detection/event_detection_190308142132.tar.gz";
//        System.out.println(s.contains(".tar.gz"));
//        System.out.println(s.contains("\\.ta\\r.gz"));
//        System.out.println(System.currentTimeMillis());
//    }
}
