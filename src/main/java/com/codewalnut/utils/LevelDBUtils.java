package com.codewalnut.utils;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;

/**
 * LevelDB数据库工具类
 * Created by Weway-RG001 on 2018-02-12 16:56.
 */
public class LevelDBUtils {

    /**
     * 打开一个levelDB数据库，如果不存在则自动创建
     *
     * @param dbPath
     * @return
     * @throws IOException
     */
    public static DB openLevelDB(String dbPath) throws IOException {
        boolean cleanup = false;
        //init
        DBFactory factory = Iq80DBFactory.factory;
        File dir = new File(dbPath);
        //如果数据不需要reload，则每次重启，尝试清理磁盘中path下的旧数据。
        if (cleanup) {
            factory.destroy(dir, null);//清除文件夹内的所有文件。
        }
        Options options = new Options().createIfMissing(true).maxOpenFiles(1000*10).writeBufferSize(4194304*16).cacheSize(4194304);
        //Options options = new Options();
        //重新open新的db
        return factory.open(dir, options);
    }

}
