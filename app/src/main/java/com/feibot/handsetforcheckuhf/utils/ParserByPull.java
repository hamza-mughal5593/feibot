package com.feibot.handsetforcheckuhf.utils;

import android.os.Environment;
import android.util.Xml;

import com.feibot.handsetforcheckuhf.bean.Device;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ParserByPull {

    //采用XmlPullParser来解析文件
    public static List<Device> getDevices(InputStream inputStream) throws Throwable {
        List<Device> devices = null;
        try {
            Device mDevice = null;
            //创建XmlPullParser
            XmlPullParser parser = Xml.newPullParser();
            //解析文件输入流
            parser.setInput(inputStream, "UTF-8");
            //得到当前的解析对象
            int eventType = parser.getEventType();
            //当解析工作还没完成时，调用next（）方法得到下一个解析事件
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        //解析开始的时候初始化list
                        devices = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        //获得解析器当前指向的元素的名字
                        //当指向元素的名字和id，name，sex这些属性重合时可以返回他们的值
                        String XPPname = parser.getName();
//                        Log.d("cxx", "XPPname:" + XPPname);
                        if ("Device".equals(XPPname)) {
                            //通过解析器获取id的元素值，并设置一个新的Student对象的id
                            mDevice = new Device("","");
//                            mDevice.setmMacAddress(parser.getAttributeValue(0));
                        }
                        if (mDevice != null) {
                            if ("MacAddress".equals(XPPname)) {
                                //得到当前指向元素的值并赋值给name
                                mDevice.setMac(parser.nextText());
                            }
                            if ("Id".equals(XPPname)) {
                                //得到当前指向元素的值并赋值给age
                                mDevice.setId(parser.nextText());
                            }
                        }
                        break;
                    //出发结束元素事件
                    case XmlPullParser.END_TAG:
                        if ("Device".equals(parser.getName())) {
                            devices.add(mDevice);
                            mDevice = null;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            return devices;

        } catch (Exception e) {
//            Log.d("cxx", "getDevices() e=" + e.getMessage());
        }
        return devices;
    }

/*
* @Author: nick
* @Description: 设置一个写入XML的方法
* @DateTime: 2021-08-12 10:05
* @Params: []
* @Return java.lang.String
*/
    public static Boolean setDeviceMacToXml(String macAddress,String machineId) throws Throwable {
        XmlSerializer serializer = Xml.newSerializer();
        File file = new File(Environment.getExternalStorageDirectory(),"device_info.xml");
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            serializer.setOutput(fos,"UTF-8");
            serializer.startDocument("UTF-8",true);
            serializer.startTag("","Device");

            serializer.startTag("","MacAddress");
            serializer.text(macAddress);
            serializer.endTag("","MacAddress");

            serializer.startTag("","machineId");
            serializer.text(machineId);
            serializer.endTag("","machineId");

            serializer.endTag("","Device");
            serializer.endDocument();
        }catch (Exception e){
            e.getStackTrace();
            return false;
        }finally {
            if(fos != null){
                fos.close();
            }
        }
        return true;
    }

}
