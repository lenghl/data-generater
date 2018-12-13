package com.dbapp.data.generater;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 主机文件下载状态
 *
 * @author lenghl
 * @create 2018-12-12 10:54
 */
public class HostDirStatus {

    /** host, dir的md5 */
    private String id;

    /** 已经下载完成的文件名称列表 */
    private List<String> fileNames;

    /** 登陆主机地址 */
    private String host;

    /** 目的目录地址 */
    private String srcDirectory;

    private HostDirStatus(){}

    public HostDirStatus(String id, String host, String srcDirectory) {
        this.id = id;
        this.host = host;
        this.srcDirectory = srcDirectory;
        this.fileNames = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSrcDirectory() {
        return srcDirectory;
    }

    public void setSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
    }
}
