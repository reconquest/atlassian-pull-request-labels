package io.reconquest.bitbucket.labels;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.external.ActiveObjectsModuleMetaData;
import com.atlassian.activeobjects.internal.AbstractActiveObjectsMetaData;
import com.atlassian.activeobjects.internal.ActiveObjectsSqlException;
import com.atlassian.activeobjects.spi.DatabaseType;
import com.atlassian.sal.api.transaction.TransactionCallback;

import net.java.ao.DBParam;
import net.java.ao.DatabaseProvider;
import net.java.ao.DefaultPolymorphicTypeMapper;
import net.java.ao.EntityManager;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;
import net.java.ao.RawEntity;
import net.java.ao.SchemaConfiguration;
import net.java.ao.schema.NameConverters;
import net.java.ao.schema.helper.DatabaseMetaDataReader;
import net.java.ao.schema.helper.DatabaseMetaDataReaderImpl;

public class EntityManagedActiveObjects implements ActiveObjects {
  private final EntityManager entityManager;
  private final DatabaseType dbType;

  protected EntityManagedActiveObjects(EntityManager entityManager, DatabaseType dbType) {
    this.dbType = dbType;
    this.entityManager = checkNotNull(entityManager);
  }

  /// CLOVER:OFF

  public final void migrate(Class<? extends RawEntity<?>>... entities) {
    try {
      entityManager.setPolymorphicTypeMapper(new DefaultPolymorphicTypeMapper(entities));
      entityManager.migrate(entities);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public void migrateDestructively(Class<? extends RawEntity<?>>... entities) {
    try {
      entityManager.setPolymorphicTypeMapper(new DefaultPolymorphicTypeMapper(entities));
      entityManager.migrateDestructively(entities);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final void flushAll() {
    entityManager.flushAll();
  }

  public final void flush(RawEntity<?>... entities) {
    entityManager.flush(entities);
  }

  public final <T extends RawEntity<K>, K> T[] get(Class<T> type, K... keys) {
    try {
      return entityManager.get(type, keys);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T get(Class<T> type, K key) {
    try {
      return entityManager.get(type, key);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T create(Class<T> type, DBParam... params) {
    try {
      return entityManager.create(type, params);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T create(Class<T> type, Map<String, Object> params) {
    try {
      return entityManager.create(type, params);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final void delete(RawEntity<?>... entities) {
    try {
      entityManager.delete(entities);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public <K> int deleteWithSQL(
      Class<? extends RawEntity<K>> type, String criteria, Object... parameters) {
    try {
      return entityManager.deleteWithSQL(type, criteria, parameters);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T[] find(Class<T> type) {
    try {
      return entityManager.find(type);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T[] find(
      Class<T> type, String criteria, Object... parameters) {
    try {
      return entityManager.find(type, criteria, parameters);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T[] find(Class<T> type, Query query) {
    try {
      return entityManager.find(type, query);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T[] find(Class<T> type, String field, Query query) {
    try {
      return entityManager.find(type, field, query);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> T[] findWithSQL(
      Class<T> type, String keyField, String sql, Object... parameters) {
    try {
      return entityManager.findWithSQL(type, keyField, sql, parameters);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> void stream(
      Class<T> type, Query query, EntityStreamCallback<T, K> streamCallback) {
    try {
      entityManager.stream(type, query, streamCallback);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <T extends RawEntity<K>, K> void stream(
      Class<T> type, EntityStreamCallback<T, K> streamCallback) {
    try {
      entityManager.stream(type, streamCallback);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <K> int count(Class<? extends RawEntity<K>> type) {
    try {
      return entityManager.count(type);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <K> int count(
      Class<? extends RawEntity<K>> type, String criteria, Object... parameters) {
    try {
      return entityManager.count(type, criteria, parameters);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  public final <K> int count(Class<? extends RawEntity<K>> type, Query query) {
    try {
      return entityManager.count(type, query);
    } catch (SQLException e) {
      throw new ActiveObjectsSqlException(entityManager, e);
    }
  }

  /// CLOVER:ON
  public final <T> T executeInTransaction(final TransactionCallback<T> callback) {
    return callback.doInTransaction();
  }

  @Override
  public ActiveObjectsModuleMetaData moduleMetaData() {
    class EntityAOModuleMetaData extends AbstractActiveObjectsMetaData {
      EntityAOModuleMetaData() {
        super(dbType);
      }

      @Override
      public boolean isInitialized() {
        return false;
      }

      @Override
      public boolean isDataSourcePresent() {
        return false;
      }

      @Override
      public boolean isTablePresent(Class<? extends RawEntity<?>> type) {
        DatabaseProvider databaseProvider = entityManager.getProvider();
        NameConverters nameConverters = entityManager.getNameConverters();
        SchemaConfiguration schemaConfiguration = entityManager.getSchemaConfiguration();

        DatabaseMetaDataReader databaseMetaDataReader =
            new DatabaseMetaDataReaderImpl(databaseProvider, nameConverters, schemaConfiguration);

        try (Connection connection = databaseProvider.getConnection()) {
          return databaseMetaDataReader.isTablePresent(connection.getMetaData(), type);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void awaitInitialization() throws ExecutionException, InterruptedException {
        throw new UnsupportedOperationException(
            "Cannot call awaitModelInitialization directly on EntityManagedActiveObjects.\n"
                + "awaitModelInitialization should not be called from within an upgrade task");
      }

      @Override
      public void awaitInitialization(final long timeout, final TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException(
            "Cannot call awaitModelInitialization directly on EntityManagedActiveObjects.\n"
                + "awaitModelInitialization should not be called from within an upgrade task");
      }
    }

    return new EntityAOModuleMetaData();
  }
}
