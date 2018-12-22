package com.example.wxpaydemo;

import com.example.wxpaydemo.WxPay.QywWxPay;
import com.example.wxpaydemo.WxPay.WXPay;
import com.example.wxpaydemo.WxPay.WXPayConstants;
import com.example.wxpaydemo.WxPay.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service

public class LoveMoneyServiceImpl  {








    @Override
    public Map<String, String> UserRecharge(Integer loveId, HttpServletRequest request, String openid) throws Exception {
        QywWxPay config = new QywWxPay();
        WXPay wxpay = new WXPay(config);
        String out_trade_no =
                new SimpleDateFormat("yyyMMDD").format(new Date()) + UUID.randomUUID().toString().substring(0, 4);
        String nonce_str = WXPayUtil.generateNonceStr();

        Map<String, String> packageParams = new HashMap<String, String>();
        packageParams.put("appid", config.getAppID());
        packageParams.put("mch_id", config.getMchID());
        packageParams.put("nonce_str", nonce_str);
        packageParams.put("body", "充值10元");
        packageParams.put("out_trade_no", UUID.randomUUID().toString().substring(0,20));//商户订单号
        packageParams.put("total_fee", "1");//支付金额，这边需要转成字符串类型，否则后面的签名会失败
        packageParams.put("spbill_create_ip", "47.107.136.229");
        packageParams.put("notify_url", "https://qianyuan66.com/qianyuan/loveMoney/payRollback");//支付成功后的回调地址
        packageParams.put("trade_type", "JSAPI");//支付方式
        packageParams.put("device_info", "1000");
        packageParams.put("openid", openid);
        packageParams.put("sign_type", "MD5");
       String xml=WXPayUtil.generateSignedXml(packageParams,config.getKey());

        Map<String, String> repData = wxpay.unifiedOrder(WXPayUtil.xmlToMap(xml));


        Map<String, String> repData2 = new HashMap<String, String>();
        if (repData.get("result_code").equals("SUCCESS")) {
            String prepay_id = repData.get("prepay_id");
            long time = System.currentTimeMillis() / 1000;
            String timeStamp=String.valueOf(time);
            String nonce_str1 = WXPayUtil.generateNonceStr();
            String paySign =
                    "appId=" + config.getAppID() + "&nonceStr=" + nonce_str1 + "&package=prepay_id=" + prepay_id +
                            "&signType=" + WXPayConstants.MD5 + "&timeStamp=" + timeStamp + "&key=" + config.getKey();
            paySign = WXPayUtil.MD5(paySign);
            repData2.put("appId", config.getAppID());
            repData2.put("timeStamp", timeStamp);
            repData2.put("nonceStr", nonce_str1);
            repData2.put("package", "prepay_id=" + prepay_id);
            repData2.put("signType", "MD5");
            repData2.put("paySign", paySign);

            PayRecord payRecord = new PayRecord();
            payRecord.setLoveId(loveId);
            payRecord.setAmount(10);
            payRecord.setType("WX");
            payRecord.setExplains("微信充值");
            payRecord.setOrderId(out_trade_no);
            payRecord.setCreatedTime(new Date());
            payRecord.setMoney(BigDecimal.valueOf(10));
            payRecord.setStatus("正在支付");
            payRecord.setOpenid(openid);
            payRecordMapper.insertSelective(payRecord);
        }
        return repData2;
    }


    public AjaxJson payRollback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("微信支付回调");
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> params = WXPayUtil.xmlToMap(resultxml);
        outSteam.close();
        inStream.close();
        QywWxPay config = new QywWxPay();
        AjaxJson ajaxJson = new AjaxJson();
        Map<String, String> return_data = new HashMap<String, String>();
        if (params.get("result_code").equalsIgnoreCase("SUCCESS")) {
            if (WXPayUtil.isSignatureValid(params, config.getKey())) {
                logger.info("微信支付-签名验证成功");
                return_data.put("openid",params.get("openid"));
                return_data.put("status","充值成功");
                if(payRecordMapper.updateByOpenId(return_data)>=1){
                 String loveid=payRecordMapper.selectByOpenId(params.get("openid"));
                if(walletMapper.updateByLoveId(loveid)>=1) {
                    return_data.clear();
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "充值成功");
                    ajaxJson.setObj(return_data);
                }
                }
            } else {
                return_data.put("openid",params.get("openid"));
                return_data.put("status","充值失败");
                if(payRecordMapper.updateByOpenId(return_data)>=1){
                    return_data.put("return_code", "Fail");
                    return_data.put("return_msg", "充值失败");
                    ajaxJson.setObj(return_data);
                }
            }
        }
        return ajaxJson;
    }




    }

}

