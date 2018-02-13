package com.codewalnut;

import com.codewalnut.utils.LevelDBUtils;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Weway-RG001 on 2018-02-12 11:11.
 */
public class LevelDBTest {
    private Charset charset = Charset.forName("UTF-8");

    @Test
    public void testGetOneAddr() throws Exception {
        String path = "D:/bitcoin_data/levelDB/leveldb";
        //重新open新的db
        DB db = LevelDBUtils.openLevelDB(path);

        List<String> addrs = new ArrayList<>();
        addrs.add("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        addrs.add("12c6DSiU4Rq3P4ZxziKxzrL5LmMBrzjrJX");
        addrs.add("1HLoD9E4SDFFPDiYfNYnkBLQ85Y51J3Zb1");

        for (String addr : addrs) {
            System.out.println("Address: " + addr);
            System.out.println("余额=" + new String(db.get(("B." + addr).getBytes())));
            System.out.println("高度=" + new String(db.get(("H." + addr).getBytes())));
            System.out.println("=========================");
        }
        db.close();
    }

    @Test
    public void testDuplicatePut() throws Exception {
        String path = "D:/data/leveldb";
        //重新open新的db
        DB db = LevelDBUtils.openLevelDB(path);
        for (int i = 0; i < 1; i++) {
            db.put("key-01".getBytes(charset), "value-01".getBytes(charset));
            db.put("key-02".getBytes(charset), "value-02".getBytes(charset));
            db.put("key-03".getBytes(charset), "value-03".getBytes(charset));
        }
        System.out.println(db.get("kelvin".getBytes()));
        db.close();

//        printAll(path);
    }

    private void printAll(String path) throws Exception {
        DB db = LevelDBUtils.openLevelDB(path);

        //读取当前snapshot，快照，读取期间数据的变更，不会反应出来
        Snapshot snapshot = db.getSnapshot();
        //读选项
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
        readOptions.snapshot(snapshot);//默认snapshot为当前。
        DBIterator iterator = db.iterator(readOptions);
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> item = iterator.next();
            String key = new String(item.getKey(), charset);
            String value = new String(item.getValue(), charset);//null,check.
            System.out.println(key + ":" + value);
        }
        iterator.close();//must be

        //compaction，手动
//        db.compactRange("key-".getBytes(charset), null);

        //
        db.close();
    }

    @Test
    public void test() throws Exception {
        boolean cleanup = true;
        Charset charset = Charset.forName("utf-8");
        String path = "d:/data/leveldb";

        //init
        DBFactory factory = Iq80DBFactory.factory;
        File dir = new File(path);
        //如果数据不需要reload，则每次重启，尝试清理磁盘中path下的旧数据。
        if (cleanup) {
            factory.destroy(dir, null);//清除文件夹内的所有文件。
        }
        Options options = new Options().createIfMissing(true);
        //重新open新的db
        DB db = factory.open(dir, options);

        //write
        db.put("key-01".getBytes(charset), "value-01".getBytes(charset));

        //write后立即进行磁盘同步写
        WriteOptions writeOptions = new WriteOptions().sync(true);//线程安全
        db.put("key-02".getBytes(charset), "value-02".getBytes(charset), writeOptions);


        //batch write；
        WriteBatch writeBatch = db.createWriteBatch();
        writeBatch.put("key-03".getBytes(charset), "value-03".getBytes(charset));
        writeBatch.put("key-04".getBytes(charset), "value-04".getBytes(charset));
        writeBatch.delete("key-01".getBytes(charset));
        db.write(writeBatch);
        writeBatch.close();

        //read
        byte[] bv = db.get("key-02".getBytes(charset));
        if (bv != null && bv.length > 0) {
            String value = new String(bv, charset);
            System.out.println(value);
        }

        //iterator，遍历，顺序读

        //读取当前snapshot，快照，读取期间数据的变更，不会反应出来
        Snapshot snapshot = db.getSnapshot();
        //读选项
        ReadOptions readOptions = new ReadOptions();
        readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。
        readOptions.snapshot(snapshot);//默认snapshot为当前。
        DBIterator iterator = db.iterator(readOptions);
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> item = iterator.next();
            String key = new String(item.getKey(), charset);
            String value = new String(item.getValue(), charset);//null,check.
            System.out.println(key + ":" + value);
        }
        iterator.close();//must be

        //delete
        db.delete("key-01".getBytes(charset));

        //compaction，手动
        db.compactRange("key-".getBytes(charset), null);

        //
        db.close();
    }

}
