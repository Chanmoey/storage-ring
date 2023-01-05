package com.moon.storagering.service.impl;

import com.moon.storagering.exception.bussness.FileSystemException;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;


/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class HBaseService {

    private HBaseService() {
    }

    private static final Logger LOGGER = Logger.getLogger(HBaseService.class);

    public static boolean createTable(Connection connection, String tableName, String[] cfs, byte[][] splitKeys) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            if (admin.tableExists(tableName)) {
                return false;
            }

            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            Arrays.stream(cfs).forEach(cf -> {
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
                hColumnDescriptor.setMaxVersions(1);
                tableDescriptor.addFamily(hColumnDescriptor);
            });

            admin.createTable(tableDescriptor, splitKeys);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20001);
        }

        return true;
    }

    public static boolean deleteTable(Connection connection, String tableName) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20002);
        }
        return true;
    }

    public static boolean deleteColumnFamily(Connection connection, String tableName, String cf) {
        try (HBaseAdmin admin = (HBaseAdmin) connection.getAdmin()) {
            admin.deleteColumn(tableName, cf);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20003);
        }
        return true;
    }

    public static boolean deleteRow(Connection connection, String tableName, String rowKey, String cf, String column) {
        Delete delete = new Delete(rowKey.getBytes());
        delete.addColumn(cf.getBytes(), column.getBytes());
        return deleteRow(connection, tableName, delete);
    }

    public static boolean deleteRow(Connection connection, String tableName, Delete delete) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            table.delete(delete);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20004);
        }
        return true;
    }

    public static boolean deleteRow(Connection connection, String tableName, String rowKey) {
        Delete delete = new Delete(rowKey.getBytes());
        return deleteRow(connection, tableName, delete);
    }

    public static Result getRow(Connection connection, String tableName, Get get) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            return table.get(get);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20005);
        }
    }

    public static Result getRow(Connection connection, String tableName, String rowKey) {
        Get get = new Get(rowKey.getBytes());
        return getRow(connection, tableName, get);
    }

    public static ResultScanner getScanner(Connection connection, String tableName, Scan scan) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            return table.getScanner(scan);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20006);
        }
    }

    public static ResultScanner getScanner(Connection connection, String tableName,
                                           String startKey, String endKey,
                                           FilterList filterList) {
        Scan scan = new Scan();
        scan.setStartRow(startKey.getBytes());
        scan.setStopRow(endKey.getBytes());
        scan.setFilter(filterList);
        return getScanner(connection, tableName, scan);
    }

    public static boolean putRow(Connection connection, String tableName, Put put) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            table.put(put);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20007);
        }
        return true;
    }

    public static boolean putRow(Connection connection, String tableName, List<Put> puts) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            table.put(puts);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20007);
        }
        return true;
    }

    /**
     * 生成目录的seqid，利用hbase提供的计数器
     */
    public static long incrementColumnValue(Connection connection, String tableName,
                                            String row, byte[] cf, byte[] qualifier, int num) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            return table.incrementColumnValue(row.getBytes(), cf, qualifier, num);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20007);
        }
    }

    public static boolean existsRow(Connection connection, String tableName, String keyRow) {
        try (Table table = connection.getTable(TableName.valueOf(tableName))) {
            Get get = new Get(keyRow.getBytes());
            return table.exists(get);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new FileSystemException(20000);
        }
    }

    public static boolean deleteColumnQualifier(Connection connection, String tableName, String rowKey,
                                                String cf, String qualifier) {
        Delete delete = new Delete(rowKey.getBytes());
        delete.addColumn(cf.getBytes(), qualifier.getBytes());
        return deleteRow(connection, tableName, delete);
    }
}
