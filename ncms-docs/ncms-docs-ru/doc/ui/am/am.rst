.. _am:

Атрибуты сборок
===============

Как было показано в :ref:`описании архитектуры ηCMS <arch>`,
контент страниц формируется из набора :term:`атрибутов <атрибут>`, принадлежащих :term:`сборкам <сборка>`.
Задать опции и начальные значения атрибутов можно
в интерфейсе :ref:`управления сборками <amgr>`. Содержимое некоторых
атрибутов страниц можно менять в :ref:`интерфейсе редактирования содержимого страницы <pmgr>`.
В данном документе представлены описания возможных типов атрибутов, используемых в сборках.

.. note::
    Рекомендуется ознакомиться с разделом :ref:`attributes_access`
    для понимания того, как ηCMS находит значения атрибутов в :ref:`HTTL <HTTL>` разметке страниц.

.. toctree::
    :maxdepth: 1

    attributes_access
    string/string
    wiki/wiki
    boolean/boolean
    date/date
    pageref/pageref
    webref/webref
    fileref/fileref
    asmref/asmref
    image/image
    select/select
    table/table
    richref/richref
    tree/tree
    core/core
    breadcrumbs/breadcrumbs
    alias/alias
    mainpage/mainpage



