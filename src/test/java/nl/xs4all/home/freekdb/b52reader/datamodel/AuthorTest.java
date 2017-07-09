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
        assertEquals(6, author.getRecordId());

        author.setRecordId(28);
        assertEquals(28, author.getRecordId());

        assertEquals("Freek", author.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Author.class)
                .withIgnoredFields("normalizedName", "recordId")
                .verify();
    }
}
