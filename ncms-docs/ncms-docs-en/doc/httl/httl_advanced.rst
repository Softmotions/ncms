.. _httl_advanced:

Дополнительные возможности HTTL
===============================


Использование HTTL в CSS
------------------------

Иногда возникает необходимость включать в CSS условную логику в зависимости от контекста запроса.
Это можно сделать так, как показано в следующем примере.

HTTL шаблон:

.. code-block:: html

    <html>
    <head>
        ...
        <link href="css/site.httl.css?style=black" rel="stylesheet" type="text/css"/>
    </head>
    ...
    </html>

Здесь css ресурс вызывается с параметром запроса `?style=black`, который обрабатывается при
генерации файла `site.httl.css` как шаблона.


Файл `css/site.httl.css`:

.. code-block:: css

    .main {
        #if(ifRequestParameter('style', 'black'))
          color: black;
        #else
          color: blue;
        #end
    }

Здесь используется условное выражение HTTL `#if` и ηCMS метод проверки значения параметра запроса `ifRequestParameter`.
Для того, чтобы `css` файл был обработан как шаблон, необходимо указать его расширение как `.httl.css`.

.. note::

    Для правильного просессинга `*.httl.css`, включаемых из html, в ηCMS
    должен быть корректно определен `mainpage` атрибут главной страницы,
    соответствующей запросу.

.. warning::

    Использование такого метода включения динамической логики в `css` не является
    рекомендуемым способом условной стилизации страниц. Во-первых, становится невозможным использование
    сжатых css файлов, во-вторых, разметка css становится смешанной с HTTL и более сложной.
    Более простым и, возможно, более подходящим методом будет использование разных `css` файлов для разных стилей страниц
    и их условное включение в страницы.



.. _httl_inheritance:

Наследование HTTL шаблонов
--------------------------

.. todo::

    TODO