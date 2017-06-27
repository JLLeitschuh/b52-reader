/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model.database;

import com.google.common.collect.ImmutableSet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import static org.junit.Assert.assertEquals;
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

        Collection<Invocation> statementInvocations = Mockito.mockingDetails(mockStatement).getInvocations();
        assertEquals(2, statementInvocations.size());

        Set<String> expectedTables = ImmutableSet.of("article", "author");
        assertEquals(expectedTables, getCreatedTables(statementInvocations, expectedTables));
    }

    private Set<String> getCreatedTables(Collection<Invocation> statementInvocations, Set<String> expectedTables) {
        Set<String> createdTables = new HashSet<>();

        statementInvocations.forEach(invocation -> {
            String invocationString = invocation.toString();

            if (invocationString.contains("execute")) {
                expectedTables.forEach(tableName -> {
                    if (invocationString.contains("create table " + tableName)) {
                        createdTables.add(tableName);
                    }
                });
            }
        });

        return createdTables;
    }
}
