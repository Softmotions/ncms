.. _newproject:

Создание нового проекта
=======================

Предварительные требования
--------------------------

* JDK `1.8`
* Maven версии `3.3.x`

Использование Maven Archetype
-----------------------------

Лучший способ создать новый проект на базе Ncms это создать
его из архетипа maven (maven archetype).

.. code-block:: sh

    mvn archetype:generate \
        -DarchetypeGroupId=softmotions \
        -DarchetypeArtifactId=ncms-site-archetype \
        -DarchetypeVersion=1.0 \
        -DarchetypeRepository=http://jaxion.org/mvn




