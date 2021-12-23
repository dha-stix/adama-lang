package org.adamalang.mysql;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class BaseConfigTests {

    @Test
    public void missing_jdbc() {
        try {
            new BaseConfig("{}");
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertTrue(ex.getMessage().contains("jdbc_url"));
        }
    }

    @Test
    public void missing_user() {
        try {
            new BaseConfig("{\"jdbc_url\":\"1\"}");
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertTrue(ex.getMessage().contains("user"));
        }
    }

    @Test
    public void missing_password() {
        try {
            new BaseConfig("{\"jdbc_url\":\"1\",\"user\":\"2\"}");
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertTrue(ex.getMessage().contains("password"));
        }
    }

    @Test
    public void missing_database_name() {
        try {
            new BaseConfig("{\"jdbc_url\":\"1\",\"user\":\"2\",\"password\":\"3\"}");
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof NullPointerException);
            Assert.assertTrue(ex.getMessage().contains("database_name"));
        }
    }

    @Test
    public void ok() throws Exception {
        BaseConfig c = new BaseConfig("{\"jdbc_url\":\"1\",\"user\":\"2\",\"password\":\"3\",\"database_name\":\"4\"}");
        Assert.assertEquals("1", c.jdbcUrl);
        Assert.assertEquals("2", c.user);
        Assert.assertEquals("3", c.password);
        Assert.assertEquals("4", c.databaseName);
    }

    public static BaseConfig getLocalIntegrationConfig() throws Exception {
        return new BaseConfig(new File("test.mysql.json"));
    }

    @Test
    public void localIntegration() throws Exception {
        BaseConfig baseConfig = getLocalIntegrationConfig();
        baseConfig.createComboPooledDataSource().close();
    }

}