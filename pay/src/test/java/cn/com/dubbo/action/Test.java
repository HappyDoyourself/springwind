package cn.com.dubbo.action;

import java.util.*;

/**
 * Created by fanhongtao
 * Date 2017-02-24 13:01
 */
public class Test {
    public static void main(String[] args) {
        Map<String,String> map = new LinkedHashMap<String, String>();
        map.put("abc","test1");
        map.put("sdbc","test3");
        map.put("csa", "test2");

        for (String set :map.keySet()){
            System.out.println(set);
        }



        String url ="http://localhost:8080?param=value#afss";
        System.out.println(url);
        if (url.contains("#")){
            System.out.println(url.substring(0,url.indexOf("#")));
        }
    }
}
