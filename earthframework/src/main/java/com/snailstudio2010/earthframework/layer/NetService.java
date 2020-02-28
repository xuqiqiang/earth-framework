/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * LiveEarth
 */
package com.snailstudio2010.earthframework.layer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xuqiqiang on 2019/07/16.
 */
public class NetService {

    public static String GetServerHeader() {
        return "xxx";
    }

    public static byte[] GetByteFromUrl(String url) {
        byte[] result = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            URL sjwurl = new URL(url);
            HttpURLConnection httpUrl = null;
            BufferedInputStream bis = null;
            byte[] buf = new byte[1024];
            httpUrl = (HttpURLConnection) sjwurl.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());

            while (true) {
                int bytes_read = bis.read(buf);
                if (bytes_read > 0) {
                    bos.write(buf, 0, bytes_read);
                } else {
                    break;
                }
            }
            bis.close();
            httpUrl.disconnect();

            result = bos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
