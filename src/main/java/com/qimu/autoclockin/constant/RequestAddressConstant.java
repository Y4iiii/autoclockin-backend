package com.qimu.autoclockin.constant;

/**
 * @Author: QiMu
 * @Date: 2023/08/14 10:40:48
 * @Version: 1.0
 * @Description: 请求地址常数
 */
public interface RequestAddressConstant {
    /**
     * 经度和纬度
     */
    String LONGITUDE_AND_LATITUDE = "https://apis.map.qq.com/jsapi?qt=geoc&addr=";

    /**
     * 标记url
     */
    String TOKEN_URL = "http://sxbaapp.zcj.jyt.henan.gov.cn/interface/token.ashx";

    /**
     * 登录网址
     */
    String LOGIN_URL = "http://sxbaapp.zcj.jyt.henan.gov.cn/interface/relog.ashx";

    /**
     * 标志url
     */
    String SIGN_URL = "http://sxbaapp.zcj.jyt.henan.gov.cn/interface/clockindaily20220827.ashx";

    /**
     * 报告界面
     */
    String REPORTING_INTERFACE = "https://sxbaapp.zcj.jyt.henan.gov.cn/interface/ReportHandler.ashx";
}