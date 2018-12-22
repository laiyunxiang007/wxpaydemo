package com.example.wxpaydemo.WxPay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WXPayExample {
    public static void main(String[] args) throws Exception {

        QywWxPay config = new QywWxPay();
        WXPay wxpay = new WXPay(config);

        Map<String, String> data = new HashMap<String, String>();
        data.put("body", "充值详情");
        data.put("out_trade_no",new SimpleDateFormat("yyyMMDD").format(new Date())+ UUID.randomUUID().toString().substring(0,4));
        data.put("device_info", "WEB");
        data.put("fee_type", "CNY");
        data.put("total_fee", "10");
        data.put("spbill_create_ip", "47.107.136.229");
        data.put("notify_url", "http://wxlj.oopmind.com/payCallback");
        data.put("trade_type", "JSAPI");  // 此处指定为扫码支付
//        data.put("openid",);


        try {
            Map<String, String> resp = wxpay.unifiedOrder(data);
            System.out.println(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
