package com.dbapp.data.generater;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;

/**
 * 描述:
 * 工具类
 *
 * @author lenghl
 * @create 2018-12-12 13:37
 */
public class Util {

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
        unCompressTar(finalName);
    }

//    public static void main(String[] args) throws IOException {
//        unCompressArchiveGz("C:\\Users\\guazi\\Downloads\\dns_181206183724.tar.gz");
//    }
}
