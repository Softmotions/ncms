.. _am_alias:

Alias
=====

The attribute allows to set a :term:`page alias`.
An alias is a set in a text field, the alias name is
allowed to contain a leading slash (`\/`)

.. note::

    The alias should be unique among all pages in ηCMS.


A page alias is an alternative page name which can be used for accessing the page.
For example, the page with the :term:`guid <page GUID>` is equal to `b3ac2985453bf87b6851e07bcf4cfadc`
available on address `http://<hostname>/b3ac2985453bf87b6851e07bcf4cfadc`.
However, if :ref:`alias <am_alias>` is presented in page’s assembly
this page can be also accessible on `http://<hostname>/mypage`.
Slash (`\/`) chars are allowed in page alias, for example, page with alias `/foo/bar`
will be available at `http://<hostname>/foo/bar`.
