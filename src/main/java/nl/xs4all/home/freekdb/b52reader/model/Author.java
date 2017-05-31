/*
 * Project: B52 reader (https://github.com/FreekDB/b52-reader).
 * License: Apache version 2 (https://www.apache.org/licenses/LICENSE-2.0).
 */


package nl.xs4all.home.freekdb.b52reader.model;

import java.util.Objects;

import nl.xs4all.home.freekdb.b52reader.utilities.Utilities;

public class Author {
    private final String name;
    private final String normalizedName;

    private Integer recordId;

    public Author(String name, int recordId) {
        this.name = name;
        this.normalizedName = Utilities.normalize(name);
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Author)) {
            return false;
        }

        Author other = (Author) obj;

        return Objects.equals(name, other.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name);
    }
}
