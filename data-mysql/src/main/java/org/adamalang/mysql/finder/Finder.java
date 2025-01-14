/*
 * This file is subject to the terms and conditions outlined in the file 'LICENSE' (hint: it's MIT); this file is located in the root directory near the README.md which you should also read.
 *
 * This file is part of the 'Adama' project which is a programming language and document store for board games; however, it can be so much more.
 *
 * See http://www.adama-lang.org/ for more information.
 *
 * (c) 2020 - 2022 by Jeffrey M. Barber (http://jeffrey.io)
 */
package org.adamalang.mysql.finder;

import org.adamalang.ErrorCodes;
import org.adamalang.common.Callback;
import org.adamalang.common.ErrorCodeException;
import org.adamalang.mysql.DataBase;
import org.adamalang.runtime.data.BackupResult;
import org.adamalang.runtime.data.FinderService;
import org.adamalang.runtime.data.Key;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Finder implements FinderService {
  private final DataBase dataBase;
  private final String region;

  public Finder(DataBase dataBase, String region) {
    this.dataBase = dataBase;
    this.region = region;
  }

  @Override
  public void find(Key key, Callback<Result> callback) {
    dataBase.transact((connection) -> {
      String selectSQL = new StringBuilder() //
          .append("SELECT `id`, `type`, `region`, `machine`, `archive` FROM `").append(dataBase.databaseName) //
          .append("`.`directory` WHERE `space`=? AND `key`=?") //
          .toString();
      try (PreparedStatement statementInsertIndex = connection.prepareStatement(selectSQL)) {
        statementInsertIndex.setString(1, key.space);
        statementInsertIndex.setString(2, key.key);
        try (ResultSet rs = statementInsertIndex.executeQuery()) {
          if (rs.next()) {
            long id = rs.getLong(1);
            int type = rs.getInt(2);
            String region = rs.getString(3);
            String machineValue = rs.getString(4);
            String archiveValue = rs.getString(5);
            Location location = Location.fromType(type);
            if (location != null) {
              return new Result(id, location, region, machineValue, archiveValue);
            }
          }
        }
      }
      throw new ErrorCodeException(ErrorCodes.UNIVERSAL_LOOKUP_FAILED);
    }, callback, ErrorCodes.FINDER_SERVICE_MYSQL_FIND_EXCEPTION);
  }

  @Override
  public void bind(Key key, String machine, Callback<Void> callback) {
    dataBase.transact((connection) -> {
      String updateIndexSQL = new StringBuilder() //
          .append("UPDATE `").append(dataBase.databaseName).append("`.`directory` ") //
          .append("SET `type`=").append(Location.Machine.type) //
          .append(", `region`=?")
          .append(", `machine`=?")
          .append(" WHERE `space`=? AND `key`=? AND ((`machine`=? AND `region`=?) OR `type`!=").append(Location.Machine.type).append(")").toString();
      try (PreparedStatement statementUpdate = connection.prepareStatement(updateIndexSQL)) {
        statementUpdate.setString(1, region);
        statementUpdate.setString(2, machine);
        statementUpdate.setString(3, key.space);
        statementUpdate.setString(4, key.key);
        statementUpdate.setString(5, machine);
        statementUpdate.setString(6, region);
        if (statementUpdate.executeUpdate() == 1) {
          return null;
        }
      }
      String insertSQL = new StringBuilder() //
          .append("INSERT INTO `").append(dataBase.databaseName).append("`.`directory` (") //
          .append("`space`, `key`, `type`, `head_seq`, `active`, `region`, `machine`, `archive`, `delta_bytes`, `asset_bytes`) VALUES (?, ?, ").append(Location.Machine.type).append(", 0, FALSE, ?, ?, '', 0, 0)") //
          .toString();
      try (PreparedStatement statementInsertIndex = connection.prepareStatement(insertSQL)) {
        statementInsertIndex.setString(1, key.space);
        statementInsertIndex.setString(2, key.key);
        statementInsertIndex.setString(3, region);
        statementInsertIndex.setString(4, machine);
        statementInsertIndex.execute();
        return null;
      }
    }, callback, ErrorCodes.UNIVERSAL_INITIALIZE_FAILURE);
  }

  @Override
  public void backup(Key key, BackupResult result, String machineOn, Callback<Void> callback) {
    dataBase.transact((connection) -> {
      String backupSQL = new StringBuilder() //
          .append("UPDATE `").append(dataBase.databaseName).append("`.`directory` ") //
          .append("SET `archive`=?")
          .append(", `head_seq`=").append(result.seq) //
          .append(", `delta_bytes`=").append(result.deltaBytes) //
          .append(", `asset_bytes`=").append(result.assetBytes)
          .append(" WHERE `space`=? AND `key`=? AND `machine`=? AND `region`=? AND `type`=").append(Location.Machine.type).toString();
      try (PreparedStatement statementUpdate = connection.prepareStatement(backupSQL)) {
        statementUpdate.setString(1, result.archiveKey);
        statementUpdate.setString(2, key.space);
        statementUpdate.setString(3, key.key);
        statementUpdate.setString(4, machineOn);
        statementUpdate.setString(5, region);
        if (statementUpdate.executeUpdate() == 1) {
          return null;
        }
      }
      throw new ErrorCodeException(ErrorCodes.FINDER_SERVICE_MYSQL_CANT_BACKUP);
    }, callback, ErrorCodes.FINDER_SERVICE_MYSQL_BACKUP_EXCEPTION);
  }

  @Override
  public void free(Key key, String machineOn, Callback<Void> callback) {
    dataBase.transact((connection) -> {
      String freeSQL = new StringBuilder() //
          .append("UPDATE `").append(dataBase.databaseName).append("`.`directory` ") //
          .append("SET `type`=").append(Location.Archive.type) //
          .append(", `region`=''")
          .append(", `machine`=''")
          .append(" WHERE `space`=? AND `key`=? AND `machine`=? AND `region`=? AND `type`=").append(Location.Machine.type).toString();
      try (PreparedStatement statementUpdate = connection.prepareStatement(freeSQL)) {
        statementUpdate.setString(1, key.space);
        statementUpdate.setString(2, key.key);
        statementUpdate.setString(3, machineOn);
        statementUpdate.setString(4, region);
        statementUpdate.executeUpdate();
      }
      return null;
    }, callback, ErrorCodes.FINDER_SERVICE_MYSQL_FREE_EXCEPTION);
  }

  @Override
  public void delete(Key key, String machineOn, Callback<Void> callback) {
    dataBase.transact((connection) -> {
      String deleteSQL = new StringBuilder() //
          .append("DELETE FROM `").append(dataBase.databaseName).append("`.`directory` ") //
          .append(" WHERE `space`=? AND `key`=? AND `machine`=? AND `region`=? AND `type`=").append(Location.Machine.type).toString();
      try (PreparedStatement statementDelete = connection.prepareStatement(deleteSQL)) {
        statementDelete.setString(1, key.space);
        statementDelete.setString(2, key.key);
        statementDelete.setString(3, machineOn);
        statementDelete.setString(4, region);
        if (statementDelete.executeUpdate() == 1) {
          return null;
        }
      }
      throw new ErrorCodeException(ErrorCodes.FINDER_SERVICE_MYSQL_CANT_DELETE);
    }, callback, ErrorCodes.FINDER_SERVICE_MYSQL_DELETE_EXCEPTION);
  }

  @Override
  public void list(String machine, Callback<List<Key>> callback) {
    dataBase.transact((connection) -> {
      String selectSQL = new StringBuilder() //
          .append("SELECT `space`, `key` FROM `").append(dataBase.databaseName) //
          .append("`.`directory` WHERE `region`=? AND `machine`=? AND `type`=") //
          .append(Location.Machine.type) //
          .toString();
      try (PreparedStatement statementInsertIndex = connection.prepareStatement(selectSQL)) {
        statementInsertIndex.setString(1, region);
        statementInsertIndex.setString(2, machine);
        try (ResultSet rs = statementInsertIndex.executeQuery()) {
          ArrayList<Key> results = new ArrayList<>();
          while (rs.next()) {
            String space = rs.getString(1);
            String key = rs.getString(2);
            results.add(new Key(space, key));
          }
          return results;
        }
      }
    }, callback, ErrorCodes.FINDER_SERVICE_MYSQL_LIST_EXCEPTION);
  }
}
