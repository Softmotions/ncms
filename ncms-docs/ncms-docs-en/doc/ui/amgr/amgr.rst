.. _amgr:

Assembly Management
===================

In this interface, the administrator can set a structure of ηCMS :term:`assemblies <assembly>` (pages),
determine the types of possible pages and edit :term:`attributes <attribute>`
of any assembly directly. With this interface you can create a structure of site pages.

Users having `admin` or `admin.asm` permissions have an access to this interface.


.. figure:: img/amgr_img1.png

    Overview of the assembly control interface

Search form
-----------

In the search form on the left, you can select:

* :term:`assembly <assembly>` with attributes,
* :term:`template` for a site page or news,
* :term:`copy of the page <page>`.

The icon opposite every item in the list of assemblies means the type of assembly:

.. image:: img/amgr_other.png
    :align: left

A simple assembly with attributes, which is not a :term:`template <template>` or a page instance.
Assemblies of this type may be present in the inheritance hierarchy of assemblies,
providing their attributes to heirs.

.. image:: img/amgr_template.png
    :align: left

Named :term:`template <template>` for site pages,
to create typical pages.

.. image:: img/amgr_page.png
    :align: left

A copy of the page, which is assembled with a unique
name in the :term:`GUID format <page GUID>`,
having a defined :term:`markup <core>`.

.. image:: img/amgr_news.png
    :align: left

A copy of the :ref:`news feed (events) <news>`.


Assembly management
-------------------

.. figure:: img/amgr_img2.png

    Page template called "Page with the content and the ability to specify the markup"


**Name** -- the name of the assembly. For page instances it is a :term:`unique GUID <page GUID>`.

**Markup** -- :ref:`HTTL <httl>` markup file for a page.

**Description** -- a brief description of the assembly. This field is displayed
in the search page form and also used to select the :term:`template <template>` for pages.

**Controller** -- name of a java class that implements
the `com.softmotions.ncms.asm.render.AsmController` interface
and is called when page is served. The controller can perform
additional actions, for example change a HTTP response.

**Published** -- Page publication status switch. When it is turned on,
the page will be displayed for site users. The ηCMS sends HTTP 404 code in the case
if this switch is off.

**Template** -- If it set to the `Page` and an assembly :term:`markup <core>` is defined,
then the assembly can be used as template for website pages. A value in the `Description` field
will set a template name. The `News` option value means that this assembly will be a template
the news feed pages.

**Roles** -- comma-separated list of user roles which have access to the template defined with this assembly.
Any user having at least one of declared roles can create pages based on this template. If the list is empty,
any user can create pages based on this template. This option has effect only if the **template** field is
set to either `Page` or `News` values.

**Parents** -- parent assemblies for the current assembly. It allows the current assembly
to inherit the attributes of the parent assembly(es).

**Attributes** -- attributes available in the assembly, including its own attributes and
inherited attributes.

.. figure:: img/amgr_img9.png

    The list of attributes associated with the page


**Feasible operations on attributes:**

.. image:: img/amgr_img3.png
    :align: left

To create a new attribute, the dialog box to select permissible
attributes is displayed:

.. figure:: img/amgr_img6.png

   Dialog to select a new :term:`attribute <attribute>`

:ref:`Description of permissible assembly attributes <am>`

After choosing the attribute, ηCMS offers to customize attribute parameters.

.. image:: img/amgr_img4.png
    :align: left

Deleting the selected attribute. Thus only attributes
belonging to the current page can be removed,
except attributes of the base assemblies
in the inheritance chain.

.. image:: img/amgr_img5.png
    :align: left

Opening attribute options for editing. It's possible to edit only attributes
belonging to the current assembly, except attributes in the base assemblies
in the inheritance chain. If the attribute of the base (parent) assembly is opened for editing,
ηCMS creates a copy of the attribute for the current page, and marks it by |img_star| icon.
In this context an attribute can override attribute with the same name in the parent assembly,
similar to the overriding of methods in object-oriented programming languages.
Inherited attributes of basic assemblies are displayed on a gray background: |img_grey|
and can not be removed or rearranged.

.. image:: img/amgr_img10.png
    :align: left

Moving the selected attribute **up**. In this case, the order of attributes
can be changed in :ref:`Edit page content interface <pmgr>`.

.. image:: img/amgr_img11.png
    :align: left

Moving the selected attribute **down**. In this case, the order of attributes
can be changed in :ref:`Edit page content interface <pmgr>`.


.. |img_star| image:: img/amgr_img7.png
.. |img_grey| image:: img/amgr_img8.png

