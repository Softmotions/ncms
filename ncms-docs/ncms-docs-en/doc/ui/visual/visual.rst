.. _visual:

The visual editor of pages
==========================

Sometimes you need to manage a quite large number of text data blocks within a particular page.
If every data block will be defined as single assembly :term:`attribute`, it may make the
:ref:`page management UI <pmgr>` to be too heavy and complicated. Also, for many users it can be
more convenient to edit a page content with visual editor.

In ηCMS we have an ability to create visually editable content blocks. In order to do it, add the HTML block (eg, `<div>`)
to the markup along with `ncms-block` attribute, where its value should be a unique identifier of the block.

.. code-block:: html

    <div ncms-block="block name">
        The contents of the block by default.
    </div>

After that, on the page preview pane, a site editor
can edit a stylized HTML text in the defined blocks,
using  `Medium Editor <https://yabwe.github.io/medium-editor/>` _.

Example of use
--------------

Then we create a page called `VisualEditor`, choose the template with the ability
to explicitly specify the :term:`markup <core>` in the `content editing UI <pmgr>`
(In order to do that the page must contain the attribute having a type :ref:`core <am_core>`)
and add the following markup:

.. code-block:: html

    <html>
      <head>
        <title>${asm('title')}</title>
      </head>
      <body style="width:50%;padding:2em;">
        <div ncms-block="main" style="background-color:#EEEEEE;min-height:2em;">
            This is a first content block
        </div>
        <footer>
            <div ncms-block="footer">
              Default footer text
            </div>
        </footer>
      </body>
    </html>

There are two visual blocks here: with identifiers `main` and` footer`,
respectively. This `main` block is highlighted with gray for clarity.

In the preview page we get:

.. image:: img/visual_img1.png

When the mouse pointer is over the block, a visual block is highlighted with a frame.
Click the block to open its edit mode where you can change the content and style the text:

.. image:: img/visual_img2.png


The result we get a simple and intuitive ability of editing
ηCMS pages blocks directly in the preview interface:

.. figure:: img/visual_img3.png

    Page after changes
