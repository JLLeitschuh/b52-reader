/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.main;

import java.lang.reflect.Constructor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the {@link B52Reader} class.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
public class B52ReaderTest {
    @Test
    public void testPrivateConstructor() throws ReflectiveOperationException {
        Constructor<B52Reader> constructor = B52Reader.class.getDeclaredConstructor();

        assertFalse(constructor.isAccessible());

        constructor.setAccessible(true);
        B52Reader instance = constructor.newInstance();

        assertNotNull(instance);
        assertEquals(B52Reader.class, instance.getClass());
    }
}
