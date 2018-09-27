package com.qindor.hsmobileservice.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;



 /**
  * 签名工具
  * SignUtils 
  * @date 2017年6月20日 下午3:05:11
  * @author liubin
  */
public class SignUtils {

	/**
	 * @param paramValues
	 * @param ignoreParamNames
	 * @param secret
	 * @return
	 */
	public static String sign(Map<String, String> paramValues,
			String[] ignoreParamNames, String secret) {
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append(secret);
			List<String> paramNames = new ArrayList<String>(paramValues.size());
			paramNames.addAll(paramValues.keySet());
			if (ignoreParamNames != null && ignoreParamNames.length > 0) {
				for (String ignoreParamName : ignoreParamNames) {
					paramNames.remove(ignoreParamName);
				}
			}
			Collections.sort(paramNames);
			for (String paramName : paramNames) {
				if(!StringUtils.isEmpty(paramValues.get(paramName))) {
					sb.append(paramName).append(paramValues.get(paramName));
				}
			}
			sb.append(secret);
			byte[] md5Digest = getMD5Digest(sb.toString());
			//String md5Str = MD5Utils.MD5(sb.toString());
			String sign = byte2hex(md5Digest);
			//return md5Str;
			return sign;
		} catch (Exception e) {
			throw new RuntimeException("加密签名计算错误", e);
		}

	}

	public static String utf8Encoding(String value, String sourceCharsetName) {
		try {
			return new String(value.getBytes(sourceCharsetName), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static byte[] getMD5Digest(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(data.getBytes("UTF-8"));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse);
		}
		return bytes;
	}

	private static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

}
