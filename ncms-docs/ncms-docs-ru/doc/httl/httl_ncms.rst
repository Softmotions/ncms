.. _httl_ncms:

ηCMS расширение HTTL
====================

.. contents::

:ref:`Основы HTTL <httl_basics>`


ηCMS добавляет в контекст :term:`HTTL` разметки дополнительные методы,
которые позволяют использовать сервисы и полезный функционал CMS на ваших
страницах. Эти методы определены в двух классах: `com.softmotions.ncms.mhttl.HttlAsmMethods`
и `com.softmotions.ncms.mhttl.HttlUtilsMethods`. Вы можете изучить их реализацию
в случае необходимости.


com.softmotions.ncms.mhttl .HttlAsmMethods
------------------------------------------

Методы, предназначенные для доступа объектам ηCMS.


.. js:function:: page()

    Возвращает объект, соответствующий текущей :term:`сборке <сборка>` (странице).

    **Пример**:
    получение названия страницы в контексте `httl` шаблона::

        ${page().hname}

    :rtype: com.softmotions.ncms.asm.Asm



.. js:function:: asmHasAttribute(String name)

    Возвращает `true`, если текущая :term:`сборка` имеет атрибут
    с именем `name`, который может быть использован в `httl` разметке.

    **Пример**::

        #if(asmHasAttribute("title"))
            ${"title".asm}
        #end

.. js:function:: asmAny(String name)
.. js:function:: asmAny(Asm asm, String name)

    Возвращает rendered значение атрибута :term:`сборки <сборка>`.
    В случае, если атрибут не найден, возвращает `null`, но в этом случае
    система не будет сообщать в консоль(log), если атрибут не найден.

    **Пример**::

        ${asmAny("title")}

    :param com.softmotions.ncms.asm.Asm asm: Сборка, для которой
        будет осуществляться поиска атрибута  `name`.
    :rtype: java.lang.Object


.. js:function:: asm(String name)
.. js:function:: asm(Asm asm, String name)

    Возвращает rendered значение атрибута текущей :term:`сборки <сборка>`.

    :param String name: Название атрибута. Данный параметр может включать
        дополнительные опции рендеринга атрибута. Например: `${asm("title,option=value")}`.

    :param com.softmotions.ncms.asm.Asm asm: Сборка для которой
            будет осуществляться поиска атрибута  `name`.


**Ниже перечислены альтернативные формы получения значения атрибутов с опциями отображения:**

.. js:function:: asm(String name, String optionName, String optionValue)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)


    Возвращает rendered значение атрибута текущей :term:`сборки <сборка>`.
    С дополнительными опциями рендеринга значения атрибута.


.. js:function:: link(Asm asm)

    Возвращает URL ссылки на страницу идентифицируемую
    объектом :term:`сборки <сборка>`

    :rtype: java.lang.String


.. js:function:: link(String guidOrAlias)

    Возвращает URL ссылки на страницу, идентифицируемую
    :term:`строковым GUID <GUID страницы>` страницы
    или :term:`псевдонимом страницы <псевдоним страницы>`

    :rtype: java.lang.String


.. js:function:: link(RichRef ref)

    Возвращает URL для объекта :ref:`com.softmotions.ncms.mhttl.RichRef`.

    :rtype: java.lang.String


.. js:function:: linkHtml(Object ref, [Map<String, String> attrs])

    Возвращает `<a href="....">` HTML ссылку для переданных объектов,
    которые могут иметь следующие типы:

    * java.lang.String - в этом случае это может быть :term:`псевдоним страницы`
      или :term:`GUID страницы`.
    * :ref:`com.softmotions.ncms.mhttl.Tree` - объект.
    * :ref:`com.softmotions.ncms.mhttl.RichRef` - объект.


    **Пример:**
    Ссылка на страницу с GUID: `12d5c7a0c3167d3d21d30f1c43368b32` и классом `active` ::

        $!{linkHtml('12d5c7a0c3167d3d21d30f1c43368b32', ['class':'active'])}

    В результате:

    .. code-block:: html

        <a href="/siteroot/12d5c7a0c3167d3d21d30f1c43368b32"
           class='active'>
            Название страницы
        </a>

    :param Map<String, String> attrs: Опциональный параметр, позволяет задать
        произвольные атрибуты для тега ссылки `<a>`.

    :rtype: java.lang.String


.. js:function:: ogmeta([Map<String, String> params])

    `Open Graph <http://ogp.me>`_ - метаинформация о текущей
    странице. Более подробно в разделе: :ref:`ogmeta`.


Методы A/B тестирования
***********************

.. js:function:: abt(String name[, boolean def])

    Возвращает `true`, если в контексте
    текущей страницы включен режим `A/B`
    тестирования с именем `name`.

.. js:function:: abtA()
.. js:function:: abtB()
.. js:function:: abtC()
.. js:function:: abtD()

    Возвращает `true`, если для текущей страницы включен режим `A/B` тестирования
    с именем `A, B, C или D` в зависимости от имени метода.



Дополнительные методы (Advanced)
********************************

.. js:function:: asmNavChilds([String type], [Number skip], [Number limit])

    Возвращает коллекцию страниц, которые являются прямыми потомками в
    :term:`дереве навигации <дерево навигации>`
    для текущей страницы.

    :param String type: :term:`Тип страницы`
    :param Number skip: Количество страниц, которые будут пропущены при выборке.
    :param Number limit: Максимальное количество страниц в выборке.
    :rtype: Collection<Asm>


.. js:function:: asmPageQuery(PageCriteria critObj, [Number skip], [Number limit])

    Выполняет свободный запрос страниц сайта. Спецификация запроса задается объектом
    класса `com.softmotions.ncms.asm.PageCriteria`

    :param Number skip: Количество страниц, которые будут пропущены при выборке.
    :param Number limit: Максимальное количество страниц в выборке.
    :rtype: Collection<Asm>


com.softmotions.ncms.mhttl .HttlUtilsMethods
--------------------------------------------

Разнообразные утилиты для использования в контексте
:term:`HTTL` шаблонов.

.. todo::

    TODO

