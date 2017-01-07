.. _am_image:

Image
=====

Attribute to specify an image file and display it on the page.
ηCMS supports automatic zooming of the image according to attribute parameters.


Attribute options
-----------------

.. figure:: img/image_img1.png

    Attribute options

=============================== =============
Option                          Description
=============================== =============
** Width **                     The desirable width of the image on the page.
** Height **                    The desirable height of the image on the page.
** Auto-scaling **              To enable/disable automatic scaling of the image
                                according to width (and/or) height.
** To fill area **              The intelligent scale mode provides the picture scaling
                                to fill the specified area with preserving of the aspect ratio.
** To check the size **         If this flag is enabled, the loaded by user image is checked
                                on compliance to the specified width and height.
** Not to scale small **        Do not scale an image, if its size is less than specified
                                restrictions. This mode is useful to avoid a grain at increase
                                small images.
=============================== =============

Edit mode
---------

.. figure:: img/image_img2.png

    Attribute on the edit page panel

By clicking the image selection button ηCMS
offers to select an image file:


.. figure:: img/image_img3.png

    Selecting an image file


.. figure:: img/image_img4.png

    Attribute after selecting the image

Using in the markup
------------------------

**Type of an attribute value:** :ref:`com.softmotions.ncms.mhttl.Image`


Example of the insertion of scaled image as a part of `<img>`:

.. code-block:: html

    #set(Image img = asm('image'))
    <img src="$!{img.link}"></img>

or:

.. code-block:: html

    <img src="$!{((Image) asm('image')).link}"></img>


.. _com.softmotions.ncms.mhttl.Image:

com.softmotions.ncms.mhttl.Image
--------------------------------

.. js:attribute:: Long Image.id

    Identifier of the media file in the ηCMS repository

.. js:function:: String getLink()

    Returns a reference to the properly scaled image.

    **Image in the в httl markup**::

    <img src="$!{img.link}" ...>


