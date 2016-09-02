package com.softmotions.ncms.mhttl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.lang3.ArrayUtils;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Table implements Iterable<String[]>, Serializable {

    private final String[][] table;

    public Table(int rows, int cols) {
        this.table = new String[rows][cols];
    }

    public Table(String[][] table) {
        this.table = table;
    }

    public Table(ArrayNode node) {
        int rows = node.size();
        table = new String[rows][];
        for (int i = 0; i < rows; ++i) {
            JsonNode jsn = node.get(i);
            if (!jsn.isArray()) {
                table[i] = EMPTY_STRING_ARRAY;
                continue;
            }
            ArrayNode row = (ArrayNode) jsn;
            int size = row.size();
            table[i] = new String[size];
            for (int j = 0; j < size; ++j) {
                table[i][j] = row.get(j).asText();
            }
        }
    }

    @Nonnull
    public String[][] getTableArray() {
        return table;
    }

    public int size() {
        return table.length;
    }

    @Nonnull
    public String value(Number in, Number jn) {
        String ret = get(row(in), jn);
        return ret != null ? ret : "";
    }

    @Nonnull
    public String[] row(Number in) {
        String[] ret = get(table, in);
        return ret != null ? ret : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Nullable
    private <T> T get(T[] arr, Number in) {
        if (in == null) {
            return null;
        }
        int i = in.intValue();
        if (i < 0 || i >= arr.length) {
            return null;
        }
        return arr[i];
    }

    @Override
    public Iterator<String[]> iterator() {
        return new ArrayIterator<>(table);
    }

    @Override
    public Spliterator<String[]> spliterator() {
        return Spliterators.spliterator(iterator(),
                                        table.length,
                                        Spliterator.ORDERED |
                                        Spliterator.SIZED |
                                        Spliterator.SUBSIZED);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        sb.append("Table{");
        for (String[] row : table) {
            sb.append(sep).append(Arrays.toString(row));
        }
        sb.append(sep).append('}');
        return sb.toString();
    }
}
