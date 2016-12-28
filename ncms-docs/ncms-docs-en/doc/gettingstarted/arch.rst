.. _arch:

.. contents::

Introduction to ηCMS
====================

Basic terms
-----------

Page in ηCMS is a set of **data** and **HTML markup**.
A data set of the page can be described  as a set of :term:`attributes <attribute>`,
where every attribute is a pair: `attribute name` and `attribute value`.
For readers who are familiar with `OOP <https://en.wikipedia.org/wiki/Object-oriented_programming>`_
principles, the page can be represented as an object of a certain class
with a set of attributes and the data stored there.

.. image:: img/ncms_arch1.png

Lets introduce some concepts used in the documentation to understand ηCMS better.

.. glossary::

    attribute
        Attribute - is a named block of data belonging to the :term:`assembly <assembly>`.
        It can be a simple string or a complex object, such as a link to another page or file, list, tree, etc.
        To refer to an attribute, use its name.
        These attributes have their own representation in the HTML page code.
        :ref:`Documentation on assembly attributes. <am>`

    assembly
        Assembly is a named set of :term:`attributes <attribute>`.
        Attributes are used to display the page data in the context of the ηCMS pages.
        In other words, the assembly is named set of attributes, and it can be referenced by the assembly name.

    HTTL
        HTTL is a template markup language (http://httl.github.io),
        on which ηCMS pages :term:`markup <core>` is defined.
        HTTL is fairly similar to the popular markup language:
        Apache Velocity. :ref:`Manual on how to use HTTL markup in ηCMS. <httl>`

    core
        Assembly core is a :term:`HTTL` markup file used to represent
        a :term:`page assembly <assembly>`  data as an HTML page.

    template
        Template is a parent :term:`assembly` (in the sense of inheritance), for a
        actually visible :term:`web page <page>`. Template defines a set of pages having
        the same structure but different contents.

    page
        Page is an :term:`assembly`, linked with its :term:`core <core>`
        to be viewed as a complete HTML page.

`Assemblies` can be inherited from each other. They can override attribute values of parent assemblies
or add new attributes. `Assemblies` can be inherited from multiple parents. Here is a direct analogy
with a inheritance of classes in Java, but we are considering `assemblies` to be as class instances.

Sample
------

Let us illustrate the statements above on the example - make a simple website.
To follow the steps described below, initially :ref:`create a new project <newproject>`.

Let the most of our website pages have the following common parts:

* Page title
* Page footer

Let's assume that the `title` is a string which is placed in the markup inside the tag `head`:

.. code-block:: html

    <head>
        <title>The page title here</title>
    </head>

And `footer` is a part of HTML markup stored in a file in the ηCMS media-repository.

In the said majority we select pages displaying only a single content block,
and unite them as :term:`template` called `Simple page`.

All pages based on `Simple page` template contain `title` and `footer` attributes
among additional attributes:

* Content
* Page markup (:term:`core`)

Website editor using ηCMS UI can create an instance of the page
called `mypage` having type `Simple page` and unique page specific contents
stored in :ref:`wiki attribute <am_wiki>`.

.. figure:: img/ncms_arch2.png
    :align: center

    Hierarchy of assemblies inheritance for `mypage` page having `Simple page` as template.

While accessing the `mypage` page, ηCMS gets the markup file for the template `Simple page`,
pushes the set of attributes pertaining to an instance of the `mypage`
:term:`assembly <assembly>` in the context of the :term:`HTTL` markup,
and finally generates the HTML response to the client.
This process describes a simple but powerful idea underlying ηCMS.

Let's implement the structure described above in the ηCMS GUI.

Using :ref:`assembly management interface <amgr>` we create an assembly called `base`.

.. figure:: img/step1.png

    New `base` assembly

.. figure:: img/step2.png

    New `base` assembly

Creating attributes common for all pages.

.. figure:: img/step3.png

    Creating new attribute for `base`

.. figure:: img/step4.png

    Creating new attribute `title` for `base`

    Similarly, adding the attribute `footer`.

.. image:: img/step5.png

Creating a new page type: "Simple page"

.. figure:: img/step6.png

    :term:`Template <template>`: "Simple page"

Creating markup file for the type "Simple page": `/site/httl/simple_core.httl`
in the :ref:`media content management interface <mmgr>`.

.. code-block:: html

    <html>
    <head>
      <title>${asm('title')}</title>
    </head>
    $!{asm('content')}
    <footer>
      $!{asm('footer')}
    </footer>
    </html>

Here we can see the output of attribute values `title`, `content`, `footer`.
:ref:`Manual on the HTTL markup in ηCMS. <httl>`


After the basic :term:`assemblies <assembly>` and page :term:`template` are defined,
site editors can create page instances via :ref:`page management UI <pmgr>`
based on the template described above:

.. image:: img/step7.png

Choose a page template:

.. figure:: img/step8.png

    Button to select the template

.. image:: img/step9.png

When the page is created an interface of a page content editor switches on.

.. figure:: img/step10.png

    Interface of a page content editor


Pressing the key `Preview` displays the result of our work:


.. figure:: img/step11.png

    Showing the created page `mypage`


Platform architecture
---------------------

ΗCMS Platform is a web application based on `Java servlet API 3.1`.
The application uses `IoC` container` Google Guice <https://github.com/google/guice> `_.
For the communication with the database, use SQL library `MyBatis <http://www.mybatis.org/mybatis-3/>` _.

Structure of the :ref:`new ηCMS project <newproject>` allows developer
to have an opportunity to both expand the functionality of the ηCMS platforms in context of the project,
or create modules specific to the project. More details can be found in the
section :ref:`extending`.


Additional definitions
----------------------

.. glossary::

    main page
        Home (start) page for a particular virtual host and language.
        To create a home page we use an attribute :ref:`front page marker <am_mainpage>` added
        to the page assembly.

    asm inheritance tree
        Assemblies can be inherited from each other.
        Here is used a semantics similar to a class inheritance in
        object-oriented programming languages. But here an assembly
        is to be treated as an object storing the data (attributes),
        and inheritance - as an inheritance of data objects.

    navigation tree
        If you create a page having the type `Container`, this page can have embedded pages (sub-pages).
        This page is a parent for nested pages. Nested page also can be a container for other pages.
        Combining page similarly, the site editor creates a `navigation tree` of the site.

        .. note::

            Beside the nesting relationship, pages can inherit
            from each other, thus forming a `Inheritance tree`. Not to be confused
            inheritance assemblies and `Navigation tree`. :ref:`attributes_access`

    page type
        There are the following acceptable types of pages

        * Standard page
        * News feed
        * :term:`assembly <assembly>` - page-prototype for another pages (parent in `Inheritance tree`).


    page GUID
         Unique 32-symbolical identifier of the page,
         used for access to the page by the address: 'http://hostname / <guid>'.</guid>

    page alias
        Alternative unique page name which can be used to display the page.
        For example, the page with the :term:`guid <page GUID>` equal to `b3ac2985453bf87b6851e07bcf4cfadc`
        acceptable by thу address `http://<hostname>/b3ac2985453bf87b6851e07bcf4cfadc`.
        However, if the attribute with the type :ref:`alias <am_alias>` is registered
        in the context of the page and has the value of `mypage`, then this page
        will be available at the following address:`http://<hostname>/mypage`.
        Allowed to use the `/` in the alias name, for example, for the alias `/foo/bar`
        the page can be available at `http://<hostname>/foo/bar`.

    glob
    glob
        Notation of a search pattern, where you can set a simple rule for compliance of pattern and data.

        * The symbol `\*` denotes zero or some characters in a line of the desired data.
        * The symbol  `\?` matches any single character of the desired data.

        `refer to a Glob notation for more details <https://en.wikipedia.org/wiki/Glob_(programming)>`_

    mediawiki
        The popular language for wiki pages markup . For example, mediawiki markup
        describes pages of the site `wikipedia.org <https://www.wikipedia.org/>` _.
        Mediawiki markup can be used to create ηCMS pages
        using :ref:`wiki attribute <am_wiki>`.

