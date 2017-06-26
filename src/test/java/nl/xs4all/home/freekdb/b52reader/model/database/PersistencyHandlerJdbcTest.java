/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersistencyHandlerJdbcTest {
    @Test
    public void testInitializeDatabaseConnection() {
        Connection mockDatabaseConnection = Mockito.mock(Connection.class);

        PersistencyHandler persistencyHandler = new PersistencyHandlerJdbc();

        assertFalse(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));
    }

    @Test
    public void testCreateTablesIfNeeded() throws SQLException {
        Connection mockDatabaseConnection = Mockito.mock(Connection.class);
        Statement mockStatement = Mockito.mock(Statement.class);
        DatabaseMetaData mockDatabaseMetaData = Mockito.mock(DatabaseMetaData.class);
        ResultSet mockResultSet = Mockito.mock(ResultSet.class);

        Mockito.when(mockDatabaseConnection.createStatement()).thenReturn(mockStatement);

        Mockito.when(mockDatabaseConnection.getMetaData()).thenReturn(mockDatabaseMetaData);
        Mockito.when(mockDatabaseMetaData.getTables(Mockito.isNull(), Mockito.isNull(),
                                                    Mockito.anyString(), Mockito.isNull()))
                .thenReturn(mockResultSet);

        PersistencyHandler persistencyHandler = new PersistencyHandlerJdbc();

        assertTrue(persistencyHandler.initializeDatabaseConnection(mockDatabaseConnection));

        persistencyHandler.createTablesIfNeeded();

        // todo: add more assert/verify statements.
    }
}
