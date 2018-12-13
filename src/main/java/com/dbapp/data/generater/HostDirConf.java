package com.dbapp.data.generater;

/**
 * 描述:
 * 主机目录配置
 *
 * @author lenghl
 * @create 2018-12-12 12:38
 */
public class HostDirConf {

    /** 登陆用户名 */
    private String userName;

    /** 登陆主机地址 */
    private String host;

    /** 登陆密码 */
    private String passWord;

    /** 源目录地址 */
    private String srcDirectory;

    /** 目的目录地址 */
    private String destDirectory;

    /** 睡眠时间 s */
    private String sleepTime = "0";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getSrcDirectory() {
        return srcDirectory;
    }

    public void setSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
    }

    public String getDestDirectory() {
        return destDirectory;
    }

    public void setDestDirectory(String destDirectory) {
        this.destDirectory = destDirectory;
    }

    public String getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(String sleepTime) {
        this.sleepTime = sleepTime;
    }
}
