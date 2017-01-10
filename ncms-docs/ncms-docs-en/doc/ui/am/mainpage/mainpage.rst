.. _am_mainpage:

Main page marker
================

This attribute marks the ηCMS page as the main (home)
for the specified set of virtual hosts and languages.

.. note::

    This attribute can not be inherited and should be explicitly
    specified to the page instance in the :ref:`assemblies management UI<amgr>`.

Attribute options
-----------------

.. figure:: img/mainpage_img1.png

    Main page marker options

**Language Codes:** List (separated by commas) `of two-letter language codes <https://en.wikipedia.org/wiki/ISO_639-1>`_
of browser (useragent), to show this page. The **\*** symbol is allowed and means any language.

**Virtual Hosts:** The list of virtual hosts (comma-separated) to show this page.
Allowed symbol **\*** means any virtual host.

**Active (checkbox):** It shows if this attribute is active.
 The rules for languages and virtual hosts will be applied when clients access to ηCMS.
