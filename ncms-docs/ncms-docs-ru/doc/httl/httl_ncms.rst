.. _httl_ncms:

nCMS расширение HTTL
====================

.. contents::


:ref:`Основы HTTL <httl_basics>`


com.softmotions.ncms.mhttl .HttlAsmMethods
------------------------------------------

Методы, предназначенные для доступа


.. js:function:: page()

    Возвращает объект соответствующий текущей :term:`сборке <сборка>` (странице)

    **Пример**
    получение названия страницы в контексте `httl` шаблона::

        ${page().hname}

    :rtype: com.softmotions.ncms.asm.Asm



.. js:function:: asmHasAttribute(String name)

    Возвращает `true` если текущая :term:`сборка` имеет атрибут
    с именем `name` который может быть использован в `httl` разметке.

    **Пример**::

        #if(asmHasAttribute("title"))
            ${"title".asm}
        #end

.. js:function:: asmAny(String name)
.. js:function:: asmAny(Asm asm, String name)

    Возвращает rendered значение атрибута :term:`сборки <сборка>`.
    В случае если атрибут не найден возвращает `null`, в этом случае
    система не будет сообщать в консоль(log) если атрибут не найден.

    **Пример**::

        ${asmAny("title")}

    :param com.softmotions.ncms.asm.Asm asm: Сборка для которой
        будет осуществляться поиска атрибута  `name`
    :rtype: java.lang.Object


.. js:function:: asm(String name)
.. js:function:: asm(Asm asm, String name)

    Возвращает rendered значение атрибута текущей :term:`сборки <сборка>`.

    :param String name: Название атрибута. Данный параметр может включать
        дополнительные опции рендеринга атрибута. Например: `${asm("title,option=value")}`

    :param com.softmotions.ncms.asm.Asm asm: Сборка для которой
            будет осуществляться поиска атрибута  `name`


**Ниже перечислены альтернативные формы получения значения атрибутов с опциями отображения:**

.. js:function:: asm(String name, String optionName, String optionValue)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)


    Возвращает rendered значение атрибута текущей :term:`сборки <сборка>`.
    С дополнительными опциями рендеринга значения атрибута.



com.softmotions.ncms.mhttl .HttlUtilsMethods
--------------------------------------------

Разнообразные утилиты для использования в контексте
:term:`HTTL` шаблонов.

