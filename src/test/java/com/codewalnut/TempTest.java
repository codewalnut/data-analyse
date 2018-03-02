package com.codewalnut;

import com.codewalnut.domain.Block;
import com.codewalnut.utils.Constants;
import com.saysth.commons.test.data.RandomData;
import com.saysth.commons.utils.json.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by Weway-RG001 on 2018-02-05.
 */
public class TempTest {
    private static Logger log = LoggerFactory.getLogger(TempTest.class);

    @Test
    public void test1() {
        List<Transaction> transactions = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        types.add(Transaction.GROCERY);
        types.add(Transaction.HOTEL);
        for (int i = 0; i < 10; i++) {
            Transaction t = new Transaction();
            t.setId(i + "");
            t.setType(RandomData.randomOne(types));
            t.setValue(RandomUtils.nextInt(1,100));
            transactions.add(t);
        }
        transactions.forEach(System.out::println);

        List<String> transactionsIds = transactions.parallelStream()
                .filter(t -> t.getType() == Transaction.GROCERY)
                .sorted(Comparator.comparingInt(Transaction::getValue).reversed())
                .map(Transaction::getId)
                .collect(toList());

        transactionsIds.forEach(System.out::println);
    }

    @Test
    public void test() throws Exception {
        long l = 234645645234l;
        System.out.println(String.valueOf(l));

        log.debug("begin");
        File file = new File("D:\\bitcoin_data\\507739.json");
        log.debug("file");
        String json = FileUtils.readFileToString(file, Constants.UTF8);
        log.debug("read");
        List<Block> blocks = JsonUtils.parseArray(json, "blocks", Block.class);
        log.debug("parse");
        System.out.println(blocks.get(0).toJson());
    }

}

class Transaction {
    public static final int GROCERY = 0;
    public static final int HOTEL = 1;
    private String id;
    private int type;
    private int value;

    public static int getGROCERY() {
        return GROCERY;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("value", value)
                .toString();
    }
}