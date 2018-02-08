package com.codewalnut;

import com.codewalnut.domain.Address;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Weway-RG001 on 2018-02-06.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressRepositoryTests {
    private Logger log = LoggerFactory.getLogger(ApplicationTests.class);

//    @Autowired
//    private AddressRepository addressRepository;

    @Test
    public void test() {
        Address address = new Address();
//        address.setAddr("dfsdf");
//        addressRepository.save(address);
    }

}
