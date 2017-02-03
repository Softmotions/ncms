.. _httl_basics:

.. contents::

HTTL Basics
===========

HTTL is a template language similar to `Apache Velocity <http://velocity.apache.org>`_,
that compiled to an optimized byte code. ηCMS uses the patched httl engine located at https://github.com/Softmotions/httl

Example of HTTL code:

.. code-block:: html

    #if(books)
        #for(Book book: books)
            <td>${book.title}</td>
        #end
    #end

By default HTTL includes 6 directives: `#set, #if, #else, #for, #break, #macro`.

Basic knowledge of the `Java` language greatly improves
understanding of HTTL constructions.

Output of HTTL statement
------------------------

Format::

    ${expression}

Example::

    ${user.name}

In this example, the result of expression `user.name` is printed, but this string
is escaped to not be a valid `html` markup. For example the result of the expression: `<b>text</ b>`
converted to `&lt;b&gt;text&lt;/b&gt;`. To disable escaping use `$!` before expression in curly braces::

  $!{expression}

But in this case, the developer should be sure that the result of the `$!{expression}`
will not cause security or layout problems inside html markup.

If the result of the expression is `null`, an empty string will be displayed::

    #set(String a = null)
    "${a}" == ""

    Displays: `"" == ""`

Comments in HTTL
----------------

Inline comments are marked by `##` in the beginning of a line ::

    ## It's a comment

Block comments start with `#*` and end with `*#`::

    #*
        It's
            a block comment in HTTL
    *#


Escape directives
-----------------

Escape directive #[...]#
************************

Format::

    #[this block is not HTTL]#

Example::

   #[This is no parse block: #if ${name}]#


Escaping $ and # chars
**********************

Format::

    \#
    \$
    \\

The symbol ``\`` before ``#``, ``$``, ``\`` displays these characters 'as is',
excluding them from HTTL markup.

Expressions
-----------

.. note::

    All httl expressions are based on the Java language expressions, therefore below we list only
    differences from standard Java expressions.

* If any item in a chain of calls ``${foo.bar.blabla}`` returns `null`,
  the full expression is interpreted as `null`, and output will be an empty string.
* The `==` operator is equal to a comparison of `Java` objects via `.equals`. In other words,
  `foo == bar` is equal to `foo.equals(bar)` in `Java`.
* An expression in single `\'` or double quotes `\"` is interpreted as a string.
  To use a single character (like `char`) conclude it to back quotes `\`\``.
* `\+` in expressions where the first argument is the number is interpreted as
  arithmetic addition. For example: `${1 + "2"}` displays `3` not 12.
  For string concatenation use a pair: `${s1}${s2}`.
* Access to a property values of the `Java` classes instances is carried out by a property name.
  For example, `${user.name}` is equivalent to calling `${user.getName()}`.
* The result of expression with logical 'OR' is the last nonzero/nonempty element of expression.
  For example, result of expression `${list1 || list2}` is `list1` while `list1` isn't empty,
  otherwise the result is `list2`.
* Numeric long literals can be specified as `<number>L` or `<number>l`.
  For example, `3L` or `3l`. If used `\L`, the result is  `java.lang.Long` object,
  and for a small `\l` the result is a primitive `long`.
* To access the data in the `java.util.List` lists or in `Java.util.Map` associated collections
  use the square brackets `[]` operator.
  For example, the expression `${mylist[0]}` is equal to `${mylist.get(0)}` and `${mymap['foo']}`
  is an equivalent of `${mymap.get("foo")}`.
* The result of the `${["a", "b", "c"]}` expression is `java.util.List` containing these elements::

    #for(color: ["red","yellow","blue"])
        ${color}
    #end

* The result of the expression: `${["foo":"bar", "foo2":"bar2"]}` is a `java.util.Map` with relationships
  `foo => bar` and `foo2 => bar2`::

    #for(entry: ["red":"# FF0000","yellow":"# 00FF00"])
        ${entry.key} = ${entry.value}
    #end

* Direct access to static methods using the prefix `\@`::

    ${@java.lang.Math.min(1,2)}
    ${@Math.min(1,2)}

Additionally, `instanceof` and `new` operators are supported::

    ${user instanceof httl.test.model.User}
    ${user instanceof User}
    ${new httl.test.model.User("a","b","c").name}
    ${new User("a","b","c").name}

You can use a type cast operator `()` in expressions::

    <img src="$!{((Image) asm('imageA')).link}"></img>

This is a result of the `asm` method calling to an instance of the class `Image` and calling its
`Java` method `.getLink()`

Setting Variables #set
----------------------

Format::

    #set(type name)
    #set(name = expression)
    #set(type name = expression)

Where `name` - variable name, and `type` - Java variable type


Example::

    #set(firstName = "John")
    #set(String lastName = "Doe")


	Here is a variable called `firstName` which is to be specified in the same template above the example::

    #set(String firstName)


Conditional expressions #if and #else
-------------------------------------

Format::

    #if(expression)
    ...
    #end

Example::

    #if(user.role == "admin")
        ...
    #else(user.role =="member")
            ... otherwise, if the role is 'member'
    #else
        ... otherwise this block will be executed
    #end

Every `#if` operator should be completed by the `#end`
operator placed after a set of optional `#else` directives.

Processing of a conditional expression
**************************************

* For any non-Boolean expression the following values are equivalents to the truth (true):
     * A number other than zero
     * Non-empty string
     * Non-empty collection
     * Object that is not `null`

* `#if(expression)` is equal to `#if(expression != null && expression != false && expression != "")`
* `#if(object)` is equal to `#if(object != null)`
* `#if(string)` is equal to `#if(string != null && string != "")`
* `#if(collection)` is equal to `#if(collection != null && collection.size > 0)`


Iterate through the collection #for
-----------------------------------

Format::

    #for(name: expression)

    #for(type name: expression)

Example::

    #for(books: books)
        ${for.index}
        ${for.size}
        ${for.first}
        ${for.last}
    #end

In the body of the `for` block there is a `for` object with the following permissions:

* `for.index` - the current iteration number, starting with ``0``
* `for.size` - size of the collection where the iteration is used
* `for.fist` - the first item in the collection
* `for.last` - the last item in the collection


Casting elements of the collection::

    #for(Book book: booklist)
        ${book.title}
    #end

In this example, there is an explicit identification of the type of item in the collection.
Every item will casted to the specified type: `Book`.

Run nine times ::

    #for(9)

Output from one to nine ::

    #for(i: 1..9)

Output ``10, 20, 30``, where the argument is defined as an array `[]` ::

    #for(i: [10, 20, 30])

Use the first the non-empty set `books1` or `books2` for iteration::

    #for(book: books1 || books2)

Iterations on the sum of two sets ::

    #for(book: books1 + books2)

Sort the collection, then make the iteration above it::

    #for(book: books.sort)

Recursive iteration, menu items have a method `getChildren`,
returning a collection of sub-items. Iteration over all
items in the hierarchy::

    #for(Menu menu: menus.recursive("getChildren"))


Cycle interruption by a #break
******************************

Format::

    #break
    #break (expression)

If the `expression` returns `true` or non-empty string,
the cycle will be interrupted.

.. note::

    Make a conditional `#break` directly in the body of the directive::

        #break (i ​​== j) ## correct

    This is significantly shorter and more productive than::

        #if (i == j) #break #end

Implementation of the action if the collection is empty #for #else
******************************************************************

Format::

    #else
    #else(expression)

Example::

    #for(book: books)
	    ...
    #else
	    ... # is run if the collection is empty
    #end


Libraries of functions in the context of HTTL patterns
------------------------------------------------------

Registration of methods library and  available methods
******************************************************

In the context of HTTL templates libraries of re-used methods are available.
A library of re-used methods is a `java` class having public static methods.
The library can be registered using configuration parameter HTTL `import.methods`.

Example of registering a new methods library in HTTL:

.. code-block:: properties

    import.methods+=com.mycompany.MyHttlMethods

After registering of a library all public static methods of the library class
become available in the context of the HTTL template and can be reused.

By default, the following libraries are defined in HTTL:

.. code-block:: properties

    import.methods=httl.spi.methods.LangMethod,\
                   java.lang.Math,\
                   httl.spi.methods.SystemMethod,\
                   httl.spi.methods.StringMethod,\
                   httl.spi.methods.MathMethod,\
                   httl.spi.methods.TypeMethod,\
                   httl.spi.methods.CollectionMethod,\
                   httl.spi.methods.CodecMethod,\
                   httl.spi.methods.EscapeMethod,\
                   httl.spi.methods.FileMethod,\
                   httl.spi.methods.MessageMethod

You can open the code of these classes in the HTTL project and learn
the functionality available in HTTL templates.

:ref:`Description of some methods of the standard HTTL library. <httl_lib>`

Call HTTL library methods
*************************

Method invocation format ::

    ${name(arg1, arg2, ...)}
    ${name()}
    ${arg1.name}
    ${arg1.name()}
    ${arg1.name(arg2, ...)}

Where `name` - the method name, and `arg1, arg2, ...` - possible arguments of the method.

Suppose we have registered the library `MyHttlMethods`,
as described above. In our library - the one simple method,
it adds `Hello\ ` to the beginning of passed string argument:

.. code-block:: java

    package com.mycompany;

    public class MyHttlMethods {

        public static String hello(String name) {
            return "Hello " + name + "!";
        }
    }

This method can be called by the following equivalent ways:

1. `${hello("Andy")}`
2. `${"Andy".hello}`
3. `${'Andy'.hello}`
4. ::

    #set(String name = "Andy")
    ${hello(name)}
    ${name.hello}

Every one of them outputs::

    Hello Andy!

As you can see, the first argument of the method can be either argument of an explicit
method call `${hello(name)}`, or to be a context for call of this method without the first argument: `${name.hello}`.

Let's add another method to our library to expand the functionality of the former one
and allow adding an arbitrary string to the end of a greeting message:

.. code-block:: java

   package com.mycompany;

    public class MyHttlMethods {

        public static String hello(String name) {
            return "Hello " + name + "!";
        }

        public static String hello(String name, String msg) {
            return hello(name) + " " + msg;
        }
    }

Then, we will be able to print `Hello Andy! Great to see u!`
by any of the following ways::

    ${hello("Andy", "Great to see u!")}

    ${"Andy".hello("Great to see u!")}


**An example of using the method :js:func:`toCycle` from `httl.spi.methods.CollectionMethod`**

Output of the product list with cyclically changing colors of rows from a set of `colors`:

.. code-block:: html

    #set(colors = ["red","blue","green"].toCycle)
    <table>
    #for(item: list)
        <tr style="color:${colors.next}">
            <td>${item.name}</td>
        </tr>
    #end
    </table>


Macros #macro
-------------

Macro is a HTTL markup unit which can be reused.
Macro can use a set of parameters similar to parameters in a `Java` function.
When you call a macro HTTL the markup defined in the macro
is inserted to the place of a macro call.

The format of the macro definition::

    #macro(name)
    #macro(name(arg1, arg2, ...))
    #macro(name(type1 arg1, type1 arg2, ...))

Where `name` is a macro name,
`arg1, arg2, ...` are possible arguments of the macro,
`type1, type2, ...` are optional types of macro arguments.


The format of the macro definition::

    ${name(arg1, arg2)}


Where `name` is macro name, `arg1, arg2, ...` are possible arguments of macro.

:ref:`Macros can be used for inheritance HTTL patterns <httl_inheritance>`.


Inclusion of other files to the markup
--------------------------------------

The set of `include` methods from `httl.spi.methods.FileMethod`
allows to include other files into the current markup.

**Example:** inclusion of `template.html` content to the markup::

    ${include("/template.httl")}

Passing an additional arguments to the context of included file::

    ${include("/template.httl", ["arg":"value"])}

Use of a relative path to the file ::

    ${include("../template.httl")}

.. note::

    The file, included by the `include` method, is interpreted as HTTL markup.

The inclusion of the file's contents to the current markup place::

    ${read("/text.txt")}

.. note::

    The file, included by the method `read`, is not interpreted as HTTL markup.
