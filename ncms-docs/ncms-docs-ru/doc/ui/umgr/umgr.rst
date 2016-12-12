.. _umgr:

Управление пользователями
=========================

Доступ к панели управления пользователями
имеют пользователи с правами доступа `admin` или `admin.users`.

Интерфейс управления пользователями
-----------------------------------

.. figure:: img/umgr_img1.png

    Редактирование пользователя/смена пароля

.. figure:: img/umgr_img2.png

    Распределение прав доступа для пользователей

Конфигурация
------------

По умолчанию в ηCMS используется база данных пользователей, хранящая данные в XML файле.

.. note::

    ηCMS позволяет подключать любую другую базу данных пользователей, реализующую
    интерфейс `com.softmotions.web.security.WSUserDatabase`.

Используются следующие параметры конфигурации базы данных
пользователей:

.. code-block:: xml

    <security>
        <!-- Расположение базы данных пользователей ηCMS в XML файле.
             placeTo: Опционально. Расположение, в которое будет скопирована
                      база данных пользователей, для последующего редактирования
                      через интерфейс управления пользователями ηCMS -->
        <xml-user-database placeTo="{home}/.myapp/mayapp-users.xml">
            <!-- Начальный путь в classpath для read-only
                 базы данных пользователей ηCMS.
                 Если указан атрибут placeTo,
                 то база данных будет скопирована в место указанное placeTo
                 в том случае если файл отсутствовал -->
            conf/mayapp-users.xml
        </xml-user-database>
        <!-- Алгоритм для генерации хешей для паролей в XML
             базе данных пользователей ηCMS.
             Возможные значения:
                - sha256
                - bcrypt
                - пустая строка или отсутствие элемента: пароли не шифруются
         -->
        <password-hash-algorithm>sha256</password-hash-algorithm>
        ...
    </security>

Данные в пользовательской XML базе
----------------------------------

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <database>
        <!-- Список прав доступа (ролей) -->
        <role description="Superuser"
              name="admin"/>
        <role description="User"
              name="user"/>
        <role description="Assemblies admin"
              name="admin.asm"/>
        <role description="Users admin"
              name="admin.users"/>
        <role description="Site structure admin"
              name="admin.structure"/>
        <role description="Department admin"
              name="dept"/>
        <role description="Transfer tools"
              name="mtt"/>

        <!-- Группы ролей, которые можно назначить пользователям -->
        <group description="Superusers"
               name="admins"
               roles="admin,admin.asm,admin.users,
                      user,admin.structure,dept,mtt">
        </group>
        <group description="Users"
               name="users"
               roles="user">
        </group>

        <!-- Пользователи -->
        <user email="ncms@example.com"
              fullName="John Doe"
              groups="admins"
              name="admin"
              password="{sha256}161cc9f549cc310f8f208bae4de ...">
        </user>
        <user email="adamansky@softmotions.com"
              ullName="Anton"
              groups="admins,users"
              name="adam"
              password="{sha256}169cc9f549cc322c8f208baee2 ..."/>
    </database>

