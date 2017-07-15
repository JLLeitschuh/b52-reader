/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.datamodel;

import nl.xs4all.home.freekdb.b52reader.general.Utilities;

import lombok.Data;

/**
 * Author that writes articles.
 *
 * @author <a href="mailto:fdbdbr@gmail.com">Freek de Bruijn</a>
 */
@Data
public class Author {
    /**
     * Full name.
     */
    private final String name;

    /**
     * Database record id where this object is stored.
     */
    private final Integer recordId;

    /**
     * Get normalized name (by stripping all accents and converting it to lowercase), for example for filtering.
     *
     * @return normalized name.
     */
    public String getNormalizedName() {
        return Utilities.normalize(name);
    }
}
