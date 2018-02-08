package com.codewalnut;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public class TempTest {

    @Test
    public void test() {
//        File dir = new File("D:\\bitcoin_data\\500000-507739-address");
//        Collection<File> files = FileUtils.listFiles(dir, new String[]{"txt"}, true);
//        for (File file : files) {
//            System.out.println(file.getName());
//        }

        Set<String> set = new HashSet<>();
        set.add("23234");
        set.add("23234");
        set.add("1134");
        set.add("23234");
        set.add("23234");
        set.add("1134");
        System.out.println(set.size());

//        Address address = new Address();
//        address.setAddr("1234");
//        Set<Address> set = new HashSet();
//        set.add(address);
//        address = new Address();
//        address.setAddr("4567");
//        set.add(address);
//        address = new Address();
//        address.setAddr("4567");
//        if (!set.contains(address)) {
//            set.add(address);
//        }
//        System.out.println(set.size());
    }

}
