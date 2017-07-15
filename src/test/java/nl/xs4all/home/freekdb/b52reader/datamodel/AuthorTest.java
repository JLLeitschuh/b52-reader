/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthorTest {
    @Test
    public void testGettersAndSetters() {
        Author author = new Author("Freek", 6);

        assertEquals("Freek", author.getName());
        assertEquals("freek", author.getNormalizedName());
        assertEquals(6, author.getRecordId().intValue());

        assertEquals("Author(name=Freek, recordId=6)", author.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Author.class)
                .withRedefinedSubclass(AuthorSubclass.class)
                .verify();
    }


    /**
     * https://groups.google.com/forum/#!topic/equalsverifier/tjPeLIiHgjQ
     *
     * Jan Ouwens (August 15th, 2015):
     * "If you want 100% coverage, you can do either of two things:
     *  1. Make the class final.
     *  2. Create a subclass and pass it into a call to EqualsVerifier's withRedefinedSubclass method. The simplest way
     *     to do that is to create one that is final and that overrides the canEquals method, always returning false. It
     *     can live as an inner class to your test class."
     */
    final class AuthorSubclass extends Author {
        public AuthorSubclass(String name, Integer recordId) {
            super(name, recordId);
        }

        @Override
        protected boolean canEqual(Object other) {
            return false;
        }
    }
}
