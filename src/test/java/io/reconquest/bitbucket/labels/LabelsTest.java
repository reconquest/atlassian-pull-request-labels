package io.reconquest.bitbucket.labels;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reconquest.bitbucket.labels.ao.LabelEntity;
import io.reconquest.bitbucket.labels.ao.LabelItem;
import io.reconquest.bitbucket.labels.ao.LabelLegacy;
import io.reconquest.bitbucket.labels.ao.Migration;
import io.reconquest.bitbucket.labels.dao.LabelDao;
import io.reconquest.bitbucket.labels.dao.LabelLegacyDao;
import io.reconquest.bitbucket.labels.dao.MigrationDao;
import io.reconquest.bitbucket.labels.service.MigrationLabelsV5;
import net.java.ao.EntityManager;
import net.java.ao.test.converters.NameConverters;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.DatabaseUpdater;
import net.java.ao.test.jdbc.DynamicJdbcConfiguration;
import net.java.ao.test.jdbc.Jdbc;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

@Data(LabelsTest.LabelsTestDatabaseUpdater.class)
@RunWith(ActiveObjectsJUnitRunner.class)
@Jdbc(DynamicJdbcConfiguration.class)
@NameConverters
public final class LabelsTest extends Assert {
  private EntityManager ao;
  private LabelDao labelDao;
  private LabelLegacyDao legacyDao;
  private MigrationDao migrationDao;

  @Before
  public void setUp() {
    labelDao = new LabelDao(ao);
    legacyDao = new LabelLegacyDao(ao);
    migrationDao = new MigrationDao(ao);
  }

  @Test
  public void createDoesNotCreateDuplicateLabels() throws Exception {
    labelDao.create(1, 1, 1, "name", "#dddddd");
    labelDao.create(1, 1, 1, "name", "#dddddd");

    assertEquals(1, ao.find(LabelEntity.class).length);
    assertEquals(1, ao.find(LabelItem.class).length);

    labelDao.create(1, 1, 2, "name", "#dddddd");

    assertEquals(1, ao.find(LabelEntity.class).length);
    assertEquals(2, ao.find(LabelItem.class).length);
  }

  @Test
  public void legacyMigrationNoDuplicates() throws Exception {
    legacyDao.create(1, 1, 1, "feature");

    legacyDao.create(1, 1, 2, "bug");
    legacyDao.create(1, 1, 2, "hotfix");

    legacyDao.create(1, 1, 3, "docs");
    legacyDao.create(1, 1, 3, "tests");
    legacyDao.create(1, 1, 3, "legacy");
    legacyDao.create(1, 1, 3, "hotfix"); // the same label

    legacyDao.create(1, 1, 4, "feature"); // the same label
    legacyDao.create(1, 1, 4, "lgtm");

    MigrationLabelsV5 migration = new MigrationLabelsV5(ao, labelDao);
    migration.process();

    assertEquals(7, ao.find(LabelEntity.class).length);
    assertEquals(9, ao.find(LabelItem.class).length);
  }

  @Test
  public void migrationDaoCreateAndFind() throws Exception {
    Migration test = migrationDao.find("TEST_MIGRATION");
    assertNull(test);

    test = migrationDao.create("TEST_MIGRATION");
    assertNotNull(test);

    assertNotNull(test.getFinishedAt());
    assertEquals("TEST_MIGRATION", test.getKey());

    Migration fact = migrationDao.find("TEST_MIGRATION");
    assertNotNull(fact.getFinishedAt());
    assertEquals(test.getFinishedAt(), fact.getFinishedAt());
    assertEquals("TEST_MIGRATION", fact.getKey());

    SQLException thrown = null;
    try {
      migrationDao.create("TEST_MIGRATION");
    } catch (SQLException e) {
      thrown = e;
    }

    assertNotNull(thrown);
    assertTrue(thrown instanceof SQLException);
  }

  public static final class LabelsTestDatabaseUpdater implements DatabaseUpdater // (2)
  {
    @Override
    public void update(EntityManager entityManager) throws Exception {
      entityManager.migrate(LabelEntity.class, LabelItem.class, LabelLegacy.class, Migration.class);
    }
  }
}
