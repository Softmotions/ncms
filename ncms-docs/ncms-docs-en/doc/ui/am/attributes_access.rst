
.. _attributes_access:

How attributes are searched in a page (inheritance of attributes)
=================================================================

To display a dynamic content in ηCMS we refer to :term:`attributes <attribute>`
available in the context of the particular :term:`page <page>`,
using: :any:`asm` and :any:`asmAny` HTTL methods. When accessing an attribute by its name,
ηCMS uses the following algorithm to find the :term:`attribute <attribute>`.

|

1. If the :term:`attribute` explicitly belongs to the current page
   (as a part of :term:`assembly <assembly>`) it will be used. Otherwise, go to **\2** step.
2. Check the :term:`inheritance tree of the current page <asm inheritance tree>`
   and look for the attribute in the parent :term:`assemblies <assembly>`.
   If the attribute is not found, go to step **\3**.
3. Check the :term:`navigation tree <navigation tree>` of the page
   and search for attributes in accordance with steps **\1** and **\2** for every page.
   If an attribute with the specified name was not found, go to step **\4**.
4. ηCMS determines a :term:`main page <main page>` depending on the current request locale
   and system settings and examines a desired attribute for the main page,
   using steps **\1** and **\2**.
   If the requested attribute was not found, HTTL directive
   :any:`asmAny` returns `null`, directive :any:`asm` also returns `null` and sends an error
   to the server console.

Despite of the many steps of this process, ηCMS fairly quickly solves the problem
using an optimized access to the database and caching algorithms.
