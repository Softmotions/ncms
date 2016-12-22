package com.softmotions.ncms.mhttl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class Table implements Iterable<String[]>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(Table.class);

    private final String[][] table;

    // first column value => row mapping
    private volatile Map<String, String[]> lookupTable;

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

    /**
     * Lookup a row by specified `firstColumn` value and return a row's
     * second column value.
     *
     * @param firstCol First column value
     * @param def      Default value if not matched row found.
     */
    @Nullable
    public String find(String firstCol, @Nullable String def) {
        return find(firstCol, 1, def);
    }

    @Nullable
    public String find(String firstCol) {
        return find(firstCol, 1, null);
    }

    @Nullable
    public String find0(String firstCol) {
        return find(firstCol, 0, null);
    }

    @Nullable
    public String find0(String firstCol, @Nullable String def) {
        return find(firstCol, 0, def);
    }

    @Nullable
    public String find2(String secondCol, @Nullable String def) {
        return find(secondCol, 2, def);
    }

    @Nullable
    public String find2(String secondCol) {
        return find(secondCol, 2, null);
    }

    @Nullable
    public String find3(String thirdCol, @Nullable String def) {
        return find(thirdCol, 3, def);
    }

    @Nullable
    public String find3(String thirdCol) {
        return find(thirdCol, 3, null);
    }

    @Nullable
    public String find(String firstCol, int colIndex) {
        return find(firstCol, colIndex, null);
    }

    @Nullable
    public String find(String firstCol, int colIndex, @Nullable String def) {
        return find(firstCol, Integer.valueOf(colIndex), def);
    }

    @Nullable
    public String find(String firstCol, Number colIndex, @Nullable String def) {
        if (firstCol == null || colIndex == null) {
            return def;
        }
        if (lookupTable == null) {
            //noinspection SynchronizeOnThis
            synchronized (this) {
                if (lookupTable == null) {
                    lookupTable = new HashMap<>();
                    for (String[] row : table) {
                        if (row.length < 1) break;
                        lookupTable.put(row[0], row);
                    }
                }
            }
        }
        int idx = colIndex.intValue();
        String[] row = lookupTable.get(firstCol);
        if (row == null || idx < 0 || idx >= row.length) {
            return def;
        }
        return row[idx] != null ? row[idx] : def;
    }

    public String toHtml() {
        return toHtml(Collections.emptyMap());
    }

    /**
     * Map keys:
     * <p/>
     * - noEscape {Boolean|String} Does not HTML escaping of table cell values
     * - noHeader {Boolean|String} Does not redender first row as table header
     * - tableAttrs {String} Optional table attributes
     *
     * @param amap
     * @return
     */
    public String toHtml(Map<String, ?> amap) {
        if (amap == null) {
            amap = Collections.emptyMap();
        }
        String sep = System.getProperty("line.separator");
        int pos = 0;
        boolean noescape = BooleanUtils.toBoolean(String.valueOf(amap.get("noEscape")));
        boolean noheader = BooleanUtils.toBoolean(String.valueOf(amap.get("noHeader")));
        String tattrs = (String) amap.get("tableAttrs");

        StringBuilder sb = new StringBuilder(512);
        sb.append("<table");
        if (tattrs != null) {
            sb.append(' ').append(tattrs);
        }
        sb.append('>');
        if (table.length > pos && !noheader) {
            sb.append(sep).append("<thead>").append(sep).append("<tr>");
            for (String v : table[pos++]) {
                sb.append(sep).append("<th>").append(noescape ? v : StringEscapeUtils.escapeHtml4(v)).append("</th>");
            }
            sb.append(sep).append("</tr>").append(sep).append("</thead>");
        }
        sb.append(sep).append("<tbody>");
        for (; pos < table.length; ++pos) {
            sb.append(sep).append("<tr>");
            for (String v : table[pos]) {
                sb.append(sep).append("<td>").append(noescape ? v : StringEscapeUtils.escapeHtml4(v)).append("</td>");
            }
            sb.append(sep).append("</tr>");
        }
        sb.append(sep).append("</tbody>");
        sb.append(sep).append("</table>");
        return sb.toString();
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
