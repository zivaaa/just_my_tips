# Dagger2 (№3)

Простой, как два пальца, пример использования ``` @Component(...) @Subcomponent(...) @Module(...) ```

Если с Component и Module все понятно, то Subcomponent чуть сложнее.
Он нужен для создания сгенерированного, повязанного всеми зависимостями, инстанса нужного нам класса.

Далее будет расписан пример того, как его задействовать.

### Поехали!

Сначала сделаем несколько базовых классов и свяжем их через Dagger

```


import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Inject

/**
 * Just a simple wrapper to use
 */
class SomeAKeeper(var v: String = "null") {
    val a: A = A(v)
}

/**
 * Simple class A, contains String to check
 */
class A(var name: String)

/**
 * Not so simple class B, contains A
 */
class B(var a: A)

/**
 * Complicated class C, contains A, B
 */
class C @Inject constructor(var b: B, var a: A)

/**
 * C wrapper class D
 */
class D @Inject constructor() {
    @Inject
    lateinit var c: C
}

```

Имеем классы A,B,C,D с встроенными через ``` @Inject ``` запросами на внедрение зависимостей.

``` SomeKeeper ``` - только это как бы небольшая обертка для A, плюс хранит строку

``` A ``` - хранит строчку

``` B ``` - зависит от A (и не важно что там хранить то нечего)

``` C ``` - зависит от A и B (сложна!)

``` D ``` - хранит суперкомпозитный C

Теперь будем настраивать Dagger.
Первым делом сделаем пару модулей (чисто для примера того, как их друг в друга включать),
которые будут помогать генерировать более сложные классы.

```

@Module
class AModule(var keeper: SomeAKeeper) {

    @Provides
    fun provideA(): A {
        return keeper.a
    }
}

@Module
class BModule() {

    @Provides
    fun provideB(a: A): B {
        return B(a)
    }
}


@Module(
    includes = [
        AModule::class,
        BModule::class
    ]
)
class ModuleC


@Component(
    modules = [
        ModuleC::class
    ]
)
interface CComponent {
    fun getC(): C
}

```

Разбор полетов:

``` AModule ``` - Требует SomeKeeper для жизни, но может предоставить A по требованию

``` BModule ``` - Ничего не требует, но может предоставить (в данном случае создать) B,
используя предоставленный A (его Dagger возьмет из модуля ModuleA, ведь в нем то он точно есть)

``` СModule ``` - назовем его оберткой над ModuleA и ModuleB, в нем они и перечислены.
Их и надо будет предоставить для создания Component.

``` CComponent ``` -  это интерфейс компонента, его реализацию даггер и предоставит когда потребуется.
В компоненте перечислены требуемые модули. В данном случае ModuleC, который включает ModuleA и ModuleB.

##### Затестим!

Надо собрать билд, чтобы даггер сделал свое темное дело и у нас появились нужные классы.
Так как в этом примере все крайне простое, то я буду писать тесты.

Находим папку для тестов (test, не androidTest!), там надо сделать класс типа ``` ExampleUnitTest ``` (в студии он уже есть).

Пишем метод:

```
    //...

    @Test
    fun testC() {
        var keeper = SomeAKeeper("YES")

        val c = DaggerCComponent.builder()
            .aModule(AModule(keeper))
            .build()
            .getC()


        assertEquals(c.b.a.name, "YES")
    }

    //...

```

Теперь клик по классу клик ПКМ -> RUN. Все дожно пройти успешно.
Можно было бы вызвать еще ``` .bModule(BModule()) ``` , но это лишнее, его даггер и сам внутри создаст.

### Едем дальше

D класс хочет C для себя, но C это уже компонент, и генерируется с учетом A и B (сложна!).
Да и вдруг гдето еще понадобится, шибко полезный класс. Обозначим его как Subcomponent и заставим встраиваться в D.

```


@Subcomponent(
    modules = [
        ModuleC::class
    ]
)
interface CSubComponent {
    /* Do it like this*/
    fun inject(d : D)

    /* Or like this */
    fun getC() : C

    @Subcomponent.Builder
    interface Builder {
        fun aModule(module : AModule) : Builder
        fun bModule(module : BModule) : Builder
        fun build(): CSubComponent
    }
}

@Module(
    subcomponents = [
        CSubComponent::class
    ],
    includes = [
        ModuleC::class
    ]
)
class ModuleD


@Component(
    modules = [ModuleD::class]
)
interface DComponent {
    fun getD() : D
}

```

Разбираем:

``` CSubComponent ``` - встраиваемый компонент, он должен иметь методы для встраивания
(тут их 2 можно юзать любой, я разницы не заметил), а также Builder.
Builder должен иметь методы корые предоставляют модули (мы их описали в modules = [...]).
Тут 2 модуля их и пишем (bModule кстати необязателен, но я указал):

```

        fun aModule(module : AModule) : Builder
        fun bModule(module : BModule) : Builder

```

Еще нужен метод build() возвращающий реализацию компонента. Этого хватит.

``` ModuleD ``` - модуль который определяется субкомпонентом и молулем CModule.
Кстати хоть CModule и определен в субкомпоненте, но надо указать его и тут.
Иначе, видимо, даггер не понимат где брать реализацию классов. Будет ошибка:

``` B cannot be provided without an @Inject constructor or an @Provides-annotated method. ```


``` DComponent ``` - это интерфейс который нам реализует даггер. В нем перчислен только модуль D, который и сам с усам (все включает).

##### Тестим!

Пишем метод теста

```

    @Test
    fun testD() {
        var keeper = SomeAKeeper("YES")
        val d = DaggerDComponent.builder()
            .aModule(AModule(keeper))
            .build()
            .getD()
        assertEquals(d.c.b.a.name, "YES")
    }

```

Как видно надо только указать aModule(), кстати bModule уже не перечеркнут как ненужный, хотя он и не обязателен.


