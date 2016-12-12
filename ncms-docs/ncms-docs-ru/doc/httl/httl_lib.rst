.. _httl_lib:

.. contents::

Основные утилитные методы HTTL разметки
=======================================

httl.spi.methods.FileMethod
---------------------------

.. todo::

    TODO


httl.spi.methods .CollectionMethod
----------------------------------


.. js:function:: toCycle(Collection<T> values)
.. js:function:: toCycle(T[] values)

    Преобразует переданный перечень элементов в
    бесконечную циклическую коллекцию, в которой
    будет постоянно повторятся переданная последовательность
    элементов.

    Возвращает циклический итератор со следующими свойствами:

    * *next* - следующий элемент
    * *value* - текущий элемент
    * *values* - массив всех элементов
    * *size* - размер циклического массива
    * *index* - текущий индекс элемента

    **Пример:**

    .. code-block:: html

        #set(colors = ["red","blue","green"].toCycle)
        <table>
        #for(item: list)
            <tr style="color:${colors.next}">
                <td>${item.name}</td>
            </tr>
        #end
        </table>

    :rtype: Циклический итератор в зависимости от переданного типа


.. js:function:: length(Map<?,?> values)
.. js:function:: length(Collection<T> values)
.. js:function:: length(T[] values)

    Возвращает длину переданной коллекции, массива.

    :rtype: int

.. js:function:: sort(List<T> values)
.. js:function:: sort(Set<T> values)
.. js:function:: sort(Collection<T> values)
.. js:function:: sort(T[] values)

    Создает новую копию переданной коллекции
    и сортирует элементы в этой коллекции

    :rtype: Тип переданной коллекции `values`



.. js:function:: recursive(Map<K, V> values)
.. js:function:: recursive(Collection<T> values)

    TODO

httl.spi.methods .EscapeMethod
------------------------------

.. js:function:: escapeString(String value)
.. js:function:: unescapeString(String value)

    Escape/unescape `"`, `\`, `\t`, `\n`, `\r`, `\b`, `\f` символов в java строке.

.. js:function:: escapeXml(String value)

    Escape XML в строке

.. js:function:: unescapeXml(String value)

    Unescape XML в строке

.. js:function:: escapeUrl(String value)

    Encode части URL в строке.

.. js:function:: unescapeUrl(String value)

    Decode части URL в строке.

.. js:function:: escapeBase64(String value)

    Encode строки в `Base64`

.. js:function:: unescapeBase64(String value)

    Decode строки из `Base64`

httl.spi.methods .StringMethod
------------------------------

.. js:function:: clip(String value, int max)

    Возвращает максимум `max` символов `value` заменяя остаток на `...`.

    **Пример**::

        ${"Привет мир".clip(6)}

    Выведет: `Привет...`

    :rtype: java.lang.String


.. js:function:: repeat(String value, int count)

    Повторяет вывод `value` `count` раз

    :rtype: java.lang.String


.. js:function:: split(String value, char separator)

    Переданное значение `value` разделяется на подстроки с разделителем `separator`
    и возвращает подстроки в виде массива строк.

    :rtype: String[]


.. js:function:: md5(String value)

    Преобразует переданное значение в  `MD5` хеш.

    :rtype: java.lang.String


.. js:function:: sha(String value)

    Преобразует переданное значение в  `SHA` хеш.

    :rtype: java.lang.String


.. js:function:: digest(String value, String digest)

    Преобразует переданное значение в хеш с алгоритмом `digest`.

    **Пример**::

        ${"abc".sha} эквивалентно ${"abc".digest("SHA")}

    :rtype: java.lang.String

.. js:function:: toCamelName(String name)

    TODO


httl.spi.methods .TypeMethod
----------------------------

.. js:function:: format([int,byte,short,long,float,double,Number] value, String format)

    Преобразует число в строку в соответствии с заданным форматом. См. `java.text.DecimalFormat`

    :rtype: java.lang.String

.. js:function:: toDate(String value, [String format])

    Преобразует строку в объект класса `java.util.Date`.

    **Пример**::

        ${"2016-05-27".toDate}


    :param String format: Формат переданной строки.
                          HTTL конфигурация: `date.format=yyyy-MM-dd HH:mm:ss`

    :rtype: java.util.Date



.. js:function:: toList(Object[] values)

    Преобразует массив значений в список `java.util.List`.


.. js:function:: toList(Collection<T> values)

    Преобразует массив значений в список `java.util.List<T>`.


.. js:function:: toArray(Collection<T> values)

    Преобразует коллекцию в массив значений  `T[]`.


.. js:function:: toBoolean(Object obj)

   Преобразует аргумент в `java.lang.Boolean`.

   **Пример**::

    ${"true".toBoolean}


.. note::

    Аналогично `toByte`, `toChar`, `toShort`,
    `toInt`, `toLong`, `toFloat`, `toDouble`,
    `toClass`.



httl.spi.methods .SystemMethod
------------------------------

.. js:function:: now()

    **Пример**::

        ${now()}

    :return: Текущая дата
        :rtype: java.util.Date


.. js:function:: random()

    :return: Нормально распределенное псевдослучайное число в промежутке: `[-2^31, 2^31-1]`

.. js:function:: uuid()

    :rtype: java.util.UUID