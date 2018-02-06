package com.codewalnut;

import com.codewalnut.domain.Address;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public class TempTest {

    @Test
    public void test() {
        Address address = new Address();
        address.setAddr("1234");
        Set<Address> set = new HashSet();
        set.add(address);
        address = new Address();
        address.setAddr("4567");
        set.add(address);
        address = new Address();
        address.setAddr("4567");
        if (!set.contains(address)) {
            set.add(address);
        }
        System.out.println(set.size());
    }

}
