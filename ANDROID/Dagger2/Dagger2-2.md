# Dagger2 (№2)

## Примерное описание аннотаций

 #### ```@Inject ```
 
 Этой аннотацией можно помечать:
 
  - конструкторы, тогда Dagger будет использовать их для создания экземпляров класса;
  - поля, тогда даггер как-то получит необходимые значения и присвоит их этим полям;
  - методы; 

По умолчанию даггер заполняет каждую зависимость новым экземпляром, но это можно изменить.

Эта аннотация не везде пригодна, например  нельзя использовать в интерфейсах. 

Классы в которых нет такой аннотации не могут быть созданы даггером.

### ```@Component```

Используется чтобы создать фабрику классов с встроенными зависимостями. 
Зависимости либо являются новыми экземплярами класса, либо как будет указано в ``` @Module```, если конечно он используется.

Для использования с конкретным модулем можно писать так

``` 
```@Component(modules = SomeModule.class)```
```

####  ```@Provides```

 Используется только(!) в модулях ``` @Module ``` на методах типа ``` fun provideSomething() {}```. 
 
 Через этот метод в результирующий класс встраиваются зависимости.

### ```@Module ```

 Аннотация для создания некоторого объекта который умеет возвращать зависимости посредством ``` @Provide ```.
Этот товарищ потом может использоваться для построения ``` @Component ``` (Но можно ктстаи и без него компонентом вполне себе пользоваться)


### ```@Named```

Помечает поток встраивания от ```@Provides -> @Inject ``` специальной строкой по которой можно ожидать возврат верного типа. (см. пример ниже)
 
 
 ### ```@Singleton```
 
 Ну тут понятно из названия, использовать единственный экземпляр объекта.
 
 
 ## Пример
 
 Будем делать небольшой MVP пример с RXJava2.
 
 Это активити с одной кнопкой, по клику на которую произойдет запрос на изменение ее текста, ну и собственно это и произойдет.
 
 1. Сначала добавляем зависимости в grandle.bundle
 
 ``` 
 
	 apply plugin: 'com.android.application'

	apply plugin: 'kotlin-android'

	apply plugin: 'kotlin-android-extensions'

	apply plugin: 'kotlin-kapt'

	android {
	   ...
	}

	dependencies {
		def daggerVersion = "2.19"
		def rxAndroidVersion = "2.1.0"

		...

		implementation "com.google.dagger:dagger:$daggerVersion"
		kapt "com.google.dagger:dagger-android-processor:$daggerVersion"
		kapt "com.google.dagger:dagger-compiler:$daggerVersion"

		implementation "io.reactivex.rxjava2:rxandroid:$rxAndroidVersion"
	}
	

 ```
 
 2. main_activity.xml - лейаут
 
 
 ``` 
 
 <?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".MainActivity">

    <Button
            android:text="Fire"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" android:id="@+id/fireBtn" app:layout_constraintEnd_toEndOf="parent"
            android:layout_marginEnd="8dp" app:layout_constraintStart_toStartOf="parent"
            android:layout_marginStart="8dp" android:layout_marginBottom="8dp"
            app:layout_constraintBottom_toBottomOf="parent" android:layout_marginTop="8dp"
            app:layout_constraintTop_toTopOf="parent"/>

</android.support.constraint.ConstraintLayout>

 
 ```
 
 3. Добавляем два вспомогательных интерфейса, которые будет реализовывать Activity.
 
 
 ``` 
 
	interface IBaseView {}

	interface IMainView : IBaseView {
		fun updateText(string: String)
	}
	
 
 ```
 
Пишем модель, которая создает rx объект ``` PublishSubject<String> ```, 
подписавышись на который, подписчик (масло-масленое) получит новое значение в основном (!) потоке.


``` 

	class Model() {
		val liveString = PublishSubject.create<String>()

		fun generateRandomString(len: Int = 20) {
			liveString.onNext(getRandomString(len))
		}

		private fun getRandomString(len: Int = 20): String {
			val bytes = ByteArray(len)
			Random().nextBytes(bytes)
			return String(bytes, StandardCharsets.UTF_8)
		}
	}
	
```
 
Теперь делаем Presenter, который получает данные от модели и заставляет обновиться вьюшку через ее интерфейс.

Логика такая: 

 - ```reloadString()``` вызывается с view, по нажатию на кнопку;
 
 - ```reloadString()``` приводит к вызову ```generateRandomString()``` в модели, которая генерит строчку и разошлет уведомления всем подписчикам ```PublishSubject```;
 
 - получив новое значение презентор попросит вьюшку обновиться с новой строкой;
 
Конструктор помечен аннотацией ```@Inject```, так что даггер встроит зависимости (см. ```@Provides``` в ```provideView``` и ``` provideModel ``` в модуле ниже) прямо в него.

```@Named("IMainView")``` - указывает имя провайдера, это нужно на случай наличия неоднозначности, тут впринципе не так важно, 
но если бы был метод возвращающий такой же тип, надо было бы их различать (это просто пояснение, чтоб знать).

``` 
	class Presenter @Inject constructor(
		@Named("IMainView") var view: IMainView,
		var model: Model
	) {
		init {
			model.liveString.subscribe({
				view.updateText(it)
			})
		}

		fun reloadString() {
			model.generateRandomString()
		}
	}

``` 
 
 Теперь добавим компонент и модуль
 
 Компонент будет возвращать Presenter, а Модуль предоставляет методы для создания зависимостей презентера.
 
 ``` 
 
@Component(modules = [PresenterModule::class])
interface PresenterComponent {
    fun getPresenter(): Presenter
}

@Module
class PresenterModule(var activity: IBaseView) {
    @Provides
    @Named("IMainView")
    fun provideView(): IMainView {
        return activity as IMainView
    }

    @Provides
    fun provideModel(): Model {
        return Model()
    }
}

 ```
 
 4. Теперь надо построить проект, чтобы статика дагера сгенерировалась и можно кодить активити.
 
 ``` 
 
 class MainActivity : AppCompatActivity(), IMainView {

    override fun updateText(string: String) {
        btn?.setText(string)
    }

    var btn : Button? = null

    var presenter : Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = DaggerPresenterComponent.builder()
            .presenterModule(PresenterModule(this))
            .build()
            .getPresenter()

        btn = findViewById<Button>(R.id.fireBtn)

        btn?.setOnClickListener {
            presenter?.reloadString()
        }
    }
}

 ```
