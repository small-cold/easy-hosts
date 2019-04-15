package com.smallcold.hosts.operate;

import com.smallcold.hosts.conf.Config;
import com.smallcold.hosts.conf.RemoteHostsFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/*
 * Created by smallcold on 2017/9/3.
 */
public class Downloader {

    private static final Logger LOGGER = Logger.getLogger(Downloader.class);

    public static boolean clearDownloadFolder() throws IOException {
        File remoteFile = Config.getHostsFileRemote();
        if (remoteFile.listFiles() != null){
            for (File subFile: remoteFile.listFiles()){
                subFile.delete();
            }
        }
        return true;
    }

    /**
     * 下载全部文件，首先清空下载文件夹
     * @throws IOException 创建文件异常
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    public static void downloadAndWriteAll() throws IOException, KeyManagementException, NoSuchAlgorithmException {
        for (RemoteHostsFile remoteHostsFile: Config.getConfigBean().getRemoteHostsFileList()){
            downloadAndWrite(remoteHostsFile);
        }
    }

    public static boolean downloadAndWrite(RemoteHostsFile remoteHostsFile)
            throws NoSuchAlgorithmException, IOException, KeyManagementException {
        String result = download(remoteHostsFile.getUrl());
        if (StringUtils.isNotBlank(result)){
            File file = new File(Config.getHostsFileRemote(), remoteHostsFile.getName());
            try (FileWriter fileWriter = new FileWriter(file)){
                fileWriter.write(result);
            }
            return true;
        }
        return false;
    }

    /**
     * 下载单个文件
     *
     * @param remoteHostsFile
     */
    private static String download(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        try (CloseableHttpClient httpclient = getHttpClient(url)) {
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader(HTTP.USER_AGENT,
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.44 Safari/537.36");
            httpget.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    LOGGER.warn(String.format("请求失败 url = %s, statusCode = %s", url,
                            statusCode));
                } else {
                    // 获取请求内容
                    return EntityUtils.toString(response.getEntity());
                }
            } finally {
                httpget.abort();
            }
        }
        return null;
    }

    private static CloseableHttpClient getHttpClient(String url)
            throws KeyManagementException, NoSuchAlgorithmException {
        if (url.startsWith("https")) {
            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom().build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[] { "TLSv1" },
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            return HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
        }
        return HttpClients.createDefault();
    }
}
