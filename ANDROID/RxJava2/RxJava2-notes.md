# RxJava2 (Описания сущностей)

### PublishSubject

Оповещает всех кто подписался, и не важно когда слушаетль был подписан.

``` 
//Создаем 

val liveString = PublishSubject.create<String>()

//Подписываемся

liveString.subscribe({
     // do something with 'it' value
})

//Оповещаем слушателей о значений

liveString.onNext("new value")
```

### Observable

Обозреватель без backpressure. Имеет много крутых методов, вызываемых по цепочке. (может потом еще допишу)

``` 
			//Создаем
			myModel = Observable.create(ObservableOnSubscribe<List<User>> {

				// Передаем подписщикам значение
				it.onNext(listOf(User("TEST")))

				// Завершающее действие
				it.onComplete()

			})
            //задаем получение списка юзеров подписчиками в MainThread 
            .observeOn(AndroidSchedulers.mainThread())
            //задаем выполнение команды в create({...}) в отдельном потоке
            .subscribeOn(Schedulers.io())
			
			//Подписываемся
			myModel.getUsers().subscribe {
				//Делаем чтото со списком юзеров "it"
			}

```

