.. _httl_lib:

.. contents::

Utility methods of HTTL markup
==============================

httl.spi.methods.FileMethod
---------------------------

.. todo::

    TODO


httl.spi.methods .CollectionMethod
----------------------------------

.. js:function:: toCycle(Collection<T> values)
.. js:function:: toCycle(T[] values)

    It converts a passed sequence of items into single endless cyclic collection
    where that sequence will be repeated constantly.

    Returns a cyclic iterator having the following properties:

    * *next* - the next item
    * *value* - the current item
    * *values* - an array of all elements
    * *size* - the size of the cyclic array
    * *index* - the current index of element

    **Example:**

    .. code-block:: html

        #set(colors = ["red","blue","green"].toCycle)
        <table>
        #for(item: list)
            <tr style="color:${colors.next}">
                <td>${item.name}</td>
            </tr>
        #end
        </table>

    :rtype: Cyclic iterator, depending on the transferred type


.. js:function:: length(Map<?,?> values)
.. js:function:: length(Collection<T> values)
.. js:function:: length(T[] values)

    It returns the length of the passed collection or array.

    :rtype: int

.. js:function:: sort(List<T> values)
.. js:function:: sort(Set<T> values)
.. js:function:: sort(Collection<T> values)
.. js:function:: sort(T[] values)

    Creates a new copy of the specified collection
    and sorts its elements.

    :rtype: Type of transferred `values` collection


.. js:function:: recursive(Map<K, V> values)
.. js:function:: recursive(Collection<T> values)

    TODO

httl.spi.methods .EscapeMethod
------------------------------

.. js:function:: escapeString(String value)
.. js:function:: unescapeString(String value)

    Escape/unescape `"`, `\`, `\t`, `\n`, `\r`, `\b`, `\f` characters in the Java string.

.. js:function:: escapeXml(String value)

    Escape XML in the string.

.. js:function:: unescapeXml(String value)

    Unescape XML in the string.

.. js:function:: escapeUrl(String value)

    Encode a part of URL in the string.

.. js:function:: unescapeUrl(String value)

    Decode the part of URL in the string.

.. js:function:: escapeBase64(String value)

    Encode the string in the `Base64`.

.. js:function:: unescapeBase64(String value)

    Decode the string from the `Base64`.

httl.spi.methods .StringMethod
------------------------------

.. js:function:: clip(String value, int max)

   It returns the specified `value` limiting its length up to the `max` characters
   substituting the rest to `...`.

   **Example**::

        ${"Hi word".clip(6)}

   Displays: `Hi...`

   :rtype: java.lang.String


.. js:function:: repeat(String value, int count)

    Repeats an output `value` `count` times

    :rtype: java.lang.String


.. js:function:: split(String value, char separator)

    The transmitted `value` is divided into substrings delimited by `separator`
    and returns substrings in the form of string array.

    :rtype: String[]

.. js:function:: md5(String value)

    Converts a transmitted value to `MD5` hash.

    :rtype: java.lang.String


.. js:function:: sha(String value)

    Converts a transmitted value to `SHA` hash.

    :rtype: java.lang.String


.. js:function:: digest(String value, String digest)

    Converts a transmitted value to hash with `digest` algorithm.

    **Example**::

        ${"abc".sha} equal to ${"abc".digest("SHA")}

    :rtype: java.lang.String

.. js:function:: toCamelName(String name)

    TODO


httl.spi.methods .TypeMethod
----------------------------

.. js:function:: format([int,byte,short,long,float,double,Number] value, String format)

    Converts a number to a string according
    to a predetermined format. Refer to `java.text.DecimalFormat`.

    :rtype: java.lang.String

.. js:function:: toDate(String value, [String format])

    Converts a string to an object of `java.util.Date` class.

    **Example**::

        ${"2016-05-27".toDate}


    :param String format: Format of transmitted string.
                          HTTL configuration: `date.format=yyyy-MM-dd HH:mm:ss`

    :rtype: java.util.Date


.. js:function:: toList(Object[] values)

    Converts an array of values to a `java.util.List` list.


.. js:function:: toList(Collection<T> values)

    Converts an array of values to a `java.util.List<T>` list.


.. js:function:: toArray(Collection<T> values)

    Converts a collection to an array of values `T[]`.


.. js:function:: toBoolean(Object obj)

   Converts an argument to `java.lang.Boolean`.

   **Example**::

    ${"true".toBoolean}


.. note::

    Similarly `toByte`, `toChar`, `toShort`,
    `toInt`, `toLong`, `toFloat`, `toDouble`,
    `toClass`.


httl.spi.methods .SystemMethod
------------------------------

.. js:function:: now()

    **Example**::

        ${now()}

    :return: current date
            :rtype: java.util.Date


.. js:function:: random()

    :return: Normally distributed pseudo-random number in the interval: `[-2^31, 2^31-1]`

.. js:function:: uuid()

    :rtype: java.util.UUID
