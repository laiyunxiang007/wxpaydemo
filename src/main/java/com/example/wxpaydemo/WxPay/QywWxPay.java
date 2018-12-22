package com.example.wxpaydemo.WxPay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class QywWxPay extends WXPayConfig {

    private byte[] certData;

    public QywWxPay() throws Exception {
        File file = new File("F:\\qianyuan\\front_end\\src\\main\\resources\\static\\apiclient_cert.p12");
//        File file = new File("/usr/local/apache-tomcat-8.5.33/webapps/qianyuan/WEB-INF/classes/static/apiclient_cert.p12");
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public String getAppID() {

        return "wx30777dc29d5463ed";
    }

    public String getMchID() {
        return "1516899191";
    }

    public String getKey() {
        return "6d42c3146a0d65fff8d191507b084ba1";
    }

    InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    @Override
    public IWXPayDomain getWXPayDomain() { // 这个方法需要这样实现, 否则无法正常初始化WXPay
        IWXPayDomain iwxPayDomain = new IWXPayDomain() {

            public void report(String domain, long elapsedTimeMillis, Exception ex) {

            }

            public DomainInfo getDomain(WXPayConfig config) {
                return new IWXPayDomain.DomainInfo(WXPayConstants.DOMAIN_API, true);
            }
        };
        return iwxPayDomain;
    }

}
