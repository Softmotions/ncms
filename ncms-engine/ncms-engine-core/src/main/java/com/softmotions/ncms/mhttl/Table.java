package com.softmotions.ncms.mhttl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Table implements Iterable<String[]>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(Table.class);

    private final String[][] table;

    public Table() {
        this(0, 0);
    }

    public Table(int rows, int cols) {
        this.table = new String[rows][cols];
    }

    public Table(String[][] table) {
        this.table = table;
    }

    public Table(JsonParser parser) {
        try {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                log.error("Invalid JSON for {}", getClass().getName());
                //noinspection ZeroLengthArrayAllocation
                table = new String[0][0];
                return;
            }
            int maxCols = 0;
            List<List<String>> rows = new ArrayList<>(32);
            while (parser.nextToken() == JsonToken.START_ARRAY) {
                List<String> cols = new ArrayList<>(8);
                rows.add(cols);
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    String v = parser.getText();
                    if (v != null) {
                        cols.add(v);
                    }
                }
                if (cols.size() > maxCols) {
                    maxCols = cols.size();
                }
            }
            table = new String[rows.size()][maxCols];
            for (int i = 0; i < table.length; ++i) {
                for (int j = 0; j < table[i].length; ++j) {
                    List<String> cols = rows.get(i);
                    table[i][j] = j < cols.size() ? cols.get(j) : null;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


    ///////////////////////////////////////////////////////////
    //                    Httl specific                      //
    ///////////////////////////////////////////////////////////


    public String toHtmlTable() {
        return toHtmlTable(Collections.emptyMap());
    }

    public String toHtmlTable(Map<String, ?> amap) {

        // TODO

        return "";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String sep = System.getProperty("line.separator");
        sb.append("Table{");
        for (int i = 0; i < table.length; ++i) {
            if (i > 0) sb.append(',');
            sb.append(sep).append(Arrays.toString(table[i]));
        }
        sb.append(sep).append('}');
        return sb.toString();
    }
}
