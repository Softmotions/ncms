.. _am_selectbox:

Select box
==========

The attribute allows to use select box
via the `pages management UI <pmgr>` and display
select box data on the website page.

Attribute options
-----------------

.. figure:: img/select_img1.png

    Options of a select box

Display as
**********

* **List** -- in this mode the select box is expanded allowing to select multiple items
              (holding `Ctrl` key)
* **Selectbox** -- in this mode the select box displayed as drop-down list,
    allowing to select only one item.


Selecting a multiple items
**************************

This option can be enabled for *list* display mode only
and allows to select multiple items.


Items
*****

The table, where items of the list can be specified as
`name`, `value` pairs. The `value` is tied to the element and can
be used in the application logic.

Edit mode
---------

.. figure:: img/select_img2.png

    The selection box with the `list` option in the `pages management UI <pmgr>`

Using in the markup
-------------------

In the context of httl markup the value of this attribute
is a collection of objects: :ref:`com.softmotions.ncms.mhttl.SelectNode`,
where for every item the following properties are defined:

* **key** - item name (java.lang.String),
* **value** - value tied to the item (java.lang.String),
* **selected** - whether this item is selected (boolean).


**Example**::

    <select>
    #foreach(SelectNode node in asm('select'))
        <option #if(node.selected) selected #end
                value="${node.value}">
                ${node.key}
        </option>
    #end
    </select>

.. _com.softmotions.ncms.mhttl.SelectNode:

com.softmotions.ncms.mhttl.SelectNode
-------------------------------------

.. js:attribute:: String SelectNode.key

    Select box pair name (label displayed to site users)

.. js:attribute:: String SelectNode.value

    Select box pair value

.. js:attribute:: boolean SelectNode.selected

   If ``true`` the current option is selected (active)

