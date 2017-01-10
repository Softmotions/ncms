.. _cookbook:

.. contents::

.. _best_practices:

Recipes and Best Practices
==========================

.. _best_practices1:

Defining templates for pages having a common structure
------------------------------------------------------

Pages of your sites can be divided into groups having the same structure, for ex.:

* Standard pages with a content
* Pages of departments on the corporative site
* News
* Blog posts
* Pages describing products or services
* ...

Any group of pages may have its own *template* containing
shared markup and common :term:`attributes <attribute>` for all pages in the group
Note: templates are to be created in the :ref:`assemblies manager UI <amgr>`.
In this case, the creation of a standard page in the group comes
down to the choosing of available template and filling
the required attributes using :ref:`pages manager UI <pmgr>`
for a particular page instance.

**What do we do if we need a pages having free structure?** For example, it can be *a lending page*,
or advertising pages with unique structure. There is no need to define a separate template for
each page. We can define a basic template for all suchlike pages, where we have:

* attribute with a `core` type (page markup) allows us to set an individual page markup.
* optional - attributes with a `fileref` used to set additional css stylesheets.

After that we can change a page markup/css styles directly for a single page instance
in the pages manager.

.. _best_practices2:

Define only page attributes that can be really changed during a page lifetime
-----------------------------------------------------------------------------

There is no need to setup a separate editable attribute for every text block on a page,
because, there is a chances that such blocks will never be changed by a site editor during
a page lifetime.

**Example:** We add the wiki attribute with the name `copyright` containing
one single line::

    All rights reserved

This line will never be changed individually for every page, it may be changed
only for all site's pages. Therefore the best way is to set it directly in the template and instead of:

.. code-block:: html

    <footer>
        ${asm('copyright')}
    </footer>

You can write this line directly
in the template part shared by all site pages:

.. code-block:: html

    <footer>
        All rights reserved
    </footer>

There are many other examples where it is not necessary to move all page data parts
into  editable page attributes. The decision to define a page data part as
an attribute should be guided by a frequency of this data part changes during
a page lifetime. Also consider a :ref:`visual page editor <visual>` to modify a number of text blocks
within a page.
