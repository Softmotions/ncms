.. _httl_ncms:

HTTL ηCMS extensions
====================

.. contents::

:ref:`HTTL basics <httl_basics>`

ηCMS provides additional :term:`HTTL` utility methods.
These extra methods allow using of services and ηCMS features on site pages.
Methods are defined in two java classes:
`com.softmotions.ncms.mhttl.HttlAsmMethods`
and `com.softmotions.ncms.mhttl.HttlUtilsMethods`.
You may explore their code as you deem necessary.


com.softmotions.ncms.mhttl .HttlAsmMethods
------------------------------------------

Methods designed to access ηCMS objects.

.. js:function:: page()

    Returns the object corresponding to the current :term:`assembly <assembly>` (page).

    **Example**:
    obtaining the current page name(title)::

        ${page().hname}

    :rtype: com.softmotions.ncms.asm.Asm


.. js:function:: asmHasAttribute(String name)

    Returns `true` if the current :term:`assembly` has the attribute
    called `name`, which can be used in the `httl` markup.

    **Example**::

        #if(asmHasAttribute("title"))
            ${"title".asm}
        #end

.. js:function:: asmAny(String name)
.. js:function:: asmAny(Asm asm, String name)

    Returns a rendered value of the :term:`assembly <assembly>` attribute.
    The `null` will be returned if the attribute is not found.

    **Example**::

        ${asmAny("title")}

    :param com.softmotions.ncms.asm.Asm asm: Assembly, for which
           a named attribute will be searched
    :rtype: java.lang.Object


.. js:function:: asm(String name)
.. js:function:: asm(Asm asm, String name)

    Returns rendered attribute value for the current :term:`assembly <assembly>`.

    :param String name: Attribute name. The parameter can include
            additional options of the attribute rendering.
            For example: `${asm("title,option=value")}`.

    :param com.softmotions.ncms.asm.Asm asm: Assembly for that a
           named attribute will be searched
    :rtype: java.lang.Object

**Below are alternative forms of obtaining attribute values with some display options:**

.. js:function:: asm(String name, String optionName, String optionValue)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2)
.. js:function:: asm(Asm asm, String name, String optionName, String optionValue, String optionName2, String optionValue2, String optionName3, String optionValue3)

    Returns the rendered value of the attribute of the current
    :term:`assembly <assembly>`. Contains additional options rendering attribute value.


.. js:function:: link(Asm asm)

    Returns URL to the page identified
    by the :term:`assembly <assembly>`

    :rtype: java.lang.String

.. js:function:: link(String guidOrAlias)

    Returns URL of the link to the page identified
    by :term:`string GUID <page GUID>` page
    or :term:`page alias <page alias>`

    :rtype: java.lang.String


.. js:function:: link(RichRef ref)

    Returns URL for the object :ref:`com.softmotions.ncms.mhttl.RichRef`.

    :rtype: java.lang.String


.. js:function:: linkHtml(Object ref, [Map<String, String> attrs])

    Returns `<a href="....">` HTML link for transferred objects,
    which may have the following forms:

    * java.lang.String - here it can be :term:`page alias`
      or :term:`page GUID`.
    * :ref:`com.softmotions.ncms.mhttl.Tree` - object.
    * :ref:`com.softmotions.ncms.mhttl.RichRef` - object.


    **Example:**
    Link to the page having GUID: `12d5c7a0c3167d3d21d30f1c43368b32` and class `active` ::

        $!{linkHtml('12d5c7a0c3167d3d21d30f1c43368b32', ['class':'active'])}

    As a result:

    .. code-block:: html

        <a href="/siteroot/12d5c7a0c3167d3d21d30f1c43368b32"
           class='active'>
           Page name
        </a>

    :param Map<String, String> attrs: Optional parameter to set
            arbitrary attributes for the link tag `<a>`.
    :rtype: java.lang.String


.. js:function:: ogmeta([Map<String, String> params])

    `Open Graph <http://ogp.me>`_ - meta-information about the current
    page. For more details refer to: :ref:`ogmeta`.


A/B testing
***********

.. js:function:: abt(String name[, boolean def])

    Returns `true`, if the `A/B` mode specified by the `name` argument is active

.. js:function:: abtA()
.. js:function:: abtB()
.. js:function:: abtC()
.. js:function:: abtD()

    Returns `true`, if the `A/B` mode corresponding to method name is enabled.


Additional methods (Advanced)
*****************************

.. js:function:: asmNavChilds([String type], [Number skip], [Number limit])

    Returns a collection of pages that are direct descendants of the current page
    within a :term:`navigation tree <navigation tree>`

    :param String type: :term:`Page type`
    :param Number skip: Number of pages which will be skipped while fetching.
    :param Number limit: The maximum number of pages in the selection.
    :rtype: Collection<Asm>


.. js:function:: asmPageQuery(PageCriteria critObj, [Number skip], [Number limit])

    Select a pages matched a quiery specified by `critObj` which is an instance
    of `com.softmotions.ncms.asm.PageCriteria`

    :param Number skip: Number of pages which will be skipped while fetching.
    :param Number limit: The maximum number of pages in the result set.
    :rtype: Collection<Asm>

com.softmotions.ncms.mhttl .HttlUtilsMethods
--------------------------------------------

A number of utility methods for use in the context of
:term:`HTTL` templates.

.. todo::

    TODO
