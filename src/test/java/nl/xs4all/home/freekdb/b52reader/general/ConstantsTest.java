/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.general;

import java.lang.reflect.Constructor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the {@link Constants} class.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class ConstantsTest {
    @Test
    public void testPrivateConstructor() throws ReflectiveOperationException {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();

        assertFalse(constructor.isAccessible());

        constructor.setAccessible(true);
        Constants instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(Constants.class, instance.getClass());
    }
}
