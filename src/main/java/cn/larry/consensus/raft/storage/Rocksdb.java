package cn.larry.consensus.raft.storage;

import org.rocksdb.RocksDB;

public class Rocksdb {

    static {
        RocksDB.loadLibrary();
    }


}
