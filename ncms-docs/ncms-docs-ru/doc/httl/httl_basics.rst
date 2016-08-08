.. httl_basics:


.. contents::


Основы HTTL
===========

HTTL это язык шаблонов подобный `Apache Velocity <http://velocity.apache.org>`  который
компилируется в быстрый исполняемый байт код.

Пример HTTL кода

.. code-block:: html

    #if(books)
        #for(Book book: books)
            <td>${book.title}</td>
        #end
    #end

По умолчанию в HTTL включены 6 директив: `#set, #if, #else, #for, #break, #macro`



Вывод результата выражений HTTL
-------------------------------

Формат::

    ${Expression}

Пример::

    ${User.name}

В данном случае результат выражений фильтруется так чтобы это не