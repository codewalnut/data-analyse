package com.codewalnut.utils;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Weway-RG001 on 2018-02-12 16:56.
 */
public class LevelDBUtils {
    public static DB openLevelDB(String dbPath) throws IOException {
        boolean cleanup = false;
        //init
        DBFactory factory = Iq80DBFactory.factory;
        File dir = new File(dbPath);
        //如果数据不需要reload，则每次重启，尝试清理磁盘中path下的旧数据。
        if (cleanup) {
            factory.destroy(dir, null);//清除文件夹内的所有文件。
        }
        Options options = new Options().createIfMissing(true);
        //重新open新的db
        return factory.open(dir, options);
    }

}