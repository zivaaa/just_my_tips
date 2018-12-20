# Animator

Главный класс для создания анимаций. Существует несоклько подклассов для реализаций анимаций. 

### ValueAnimator

Анимирует какое-то значение ОТ и ДО, вызывая калбэк в котором можно применять это значение

``` 

			val smile = rootView.findViewById<ImageView>(R.id.smile1)
            rootView.findViewById<Button>(R.id.animFire).apply {
                setOnClickListener {
				
                    val valueAnimator = ValueAnimator.ofFloat(0F, rootView.height.toFloat())

                    valueAnimator.addUpdateListener { animation ->
                        val value = animation.animatedValue as Float
                      
                        smile.setTranslationY(value)
                    }

                    valueAnimator.interpolator = LinearInterpolator()
                    valueAnimator.setDuration(2000)
                    valueAnimator.start()
                }
            }

```

#### Interpolator (Интерполяция анимаций)

Указываем метод интерполяции для ValueAnimator. 

Следующий код динамически заполняет RadioGroup и по клику на кнопку Fire создает анимацию и ставит ей один из выбранных интерполяторов.

``` 

            val radioGroup = rootView.findViewById<RadioGroup>(R.id.radioGroup1)

            val strings = arrayListOf<String>(
                "AccelerateDecelerateInterpolator",
                "AccelerateInterpolator",
                "AnticipateInterpolator",
                "AnticipateOvershootInterpolator",

                "BounceInterpolator",
                "CycleInterpolator",
                "DecelerateInterpolator",
                "LinearInterpolator",
                "OvershootInterpolator"
            )

            for ((k, v) in strings.withIndex()) {
                RadioButton(radioGroup.context).apply {
                    id = k + 1000
                    setText(v)
                    radioGroup.addView(this)
                }
            }

            rootView.findViewById<Button>(R.id.animFire).apply {
                setOnClickListener {
                    val checkedId = radioGroup.checkedRadioButtonId

					// Анимируемый объект
                    val animated = rootView.findViewById<ImageView>(R.id.animImage);
					
					// Создаем анимацию

                    val vanim = ValueAnimator.ofObject(FloatEvaluator(), 1f, 2f)
                    
					// задаем значение которое будем менять
					
                    vanim.addUpdateListener {
                        animated.scaleX = it.animatedValue as Float
                        animated.scaleY = it.animatedValue as Float
                    }

                    vanim.duration = 2000

					// Задаем интерполятор
					
                    when (checkedId) {
                        1000 -> {
                            vanim.interpolator = AccelerateDecelerateInterpolator()
                        }
                        1001 -> {
                            vanim.interpolator = AccelerateInterpolator()
                        }
                        1002 -> {
                            vanim.interpolator = AnticipateInterpolator()
                        }
                        1003 -> {
                            vanim.interpolator = AnticipateOvershootInterpolator()
                        }
                        1004 -> {
                            vanim.interpolator = BounceInterpolator()
                        }
                        1005 -> {
                            vanim.interpolator = CycleInterpolator(2F)
                        }
                        1006 -> {
                            vanim.interpolator = DecelerateInterpolator()
                        }
                        1007 -> {
                            vanim.interpolator = LinearInterpolator()
                        }
                        1008 -> {
                            vanim.interpolator = OvershootInterpolator()
                        }
                        else ->  {
                            vanim.interpolator = LinearInterpolator()
                        }
                    }

                    vanim.start()

                }
            }
			
```

### ObjectAnimator

Анимирует указанное свойство, позволяет писать меньше кода

```

	ObjectAnimator.ofFloat(smile, View.SCALE_Y, 1F, 2F)
	
```


## Listener (Калбэки)

На аниматоры можно повесить слушатели и делать там свои вычисления. 

Следующий код задает анимацию ImageView по параметру scaleX от 1 до 2.
Вешает слушателя который в момент окончания анимации начинает новую, которая анимирует scaleX  от 2 до 1

``` 

					ObjectAnimator.ofFloat(smile, View.SCALE_Y, 1F, 2F).apply {
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                ObjectAnimator.ofFloat(smile, View.SCALE_Y, 2F, 1F).start()
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {

                            }
                        })
                        start()
                    }

```

### AnimatorSet

Позволяет проигрывать несколько анимаций или даже наборов анимаций. Как последовательно (.playSequentially)  так и параллельно (.playTogether) .

В примере создаются два сета анимация который как понятно из названий атрибутов меняют положение и масштаб.


``` 

                    val firstPart = AnimatorSet().apply {
                        playTogether(
                            ObjectAnimator.ofFloat(textAnim, View.TRANSLATION_X, 0f, 100f),
                            ObjectAnimator.ofFloat(textAnim, View.SCALE_Y, 1f, 1.5f),
                            ObjectAnimator.ofFloat(textAnim, View.SCALE_X, 1f, 1.5f)
                        )
                        setDuration(2000)
                    }


                    val secondPart = AnimatorSet().apply {
                        playTogether(
                            ObjectAnimator.ofFloat(textAnim, View.TRANSLATION_X, 100f, 0f),
                            ObjectAnimator.ofFloat(textAnim, View.SCALE_Y, 1.5f, 1f),
                            ObjectAnimator.ofFloat(textAnim, View.SCALE_X, 1.5f, 1f)
                        )
                        setDuration(2000)
                    }

                    val allSet = AnimatorSet().apply {
                        playSequentially(firstPart, secondPart)
                    }
                    
                    allSet.start()

```


## Transition

Transition педлагает новый вид анимаций, основанный на сценах. Это работает по принципу 
 - создаем Transition для ViewGroup который запомить текущее состояние
 - меняем свойста контролов внутри ViewGroup.
 - вуа-ля анимация пошла
 

 ``` 
 
			rootView.findViewById<Button>(R.id.anim1_btn).apply {
                setOnClickListener {
                    val sceneRoot = rootView as ViewGroup
                    val transtionable = rootView.findViewById<View>(R.id.transtionable)
                    val newSquareSize = resources.getDimensionPixelSize(R.dimen.square_size_expanded)

                    // вызываем метод, говорящий о том, что мы хотим анимировать следующие изменения внутри sceneRoot
                    TransitionManager.beginDelayedTransition(sceneRoot)

                    // и применим сами изменения
                    val params = transtionable.getLayoutParams()
                    params.width = newSquareSize
                    params.height = newSquareSize
                    transtionable.setLayoutParams(params)
                }
            }
			
 
 ```
 
  Второй вариант это использовать разные лейауты.
 Например в первом делаем лейаут с View - id - firstItem, и во втором с View - id - firstItem

 - фиксируем сцену с лейаутом 1
 - запускаем переход на сцену 2
 
 
 ``` 
 
			val rootView = inflater.inflate(R.layout.sample_2, container, false)
           
            rootView.findViewById<Button>(R.id.anim2_btn).apply {
                setOnClickListener {
                    val scene2 = Scene.getSceneForLayout(rootView as ViewGroup, R.layout.sample_1, rootView.context);
                    // опишем свой аналог AutoTransition
                    val set = TransitionSet()
                    set.addTransition(Fade())
                    set.addTransition(ChangeBounds())
                    // выполняться они будут одновременно
                    set.setOrdering(TransitionSet.ORDERING_TOGETHER)
                    // уставим свою длительность анимации
                    set.setDuration(500)
                    // и изменим Interpolator
                    set.setInterpolator(AccelerateInterpolator())
                    TransitionManager.go(scene2, set)
                }
            }
			
 ```
 
### LayoutTransition (Анимация в ViewGroup)

Этот вид анимации работает для групп контролов (ViewGroup) при их изменениии.

Простейший пример для LinarLayout который настраиваем плавное скрытие / появление дочернего с плавным изменением размера самого контейнера: 

```

//  1.XML
<LinearLayout
            android:id="@+id/layoutForTransition"
            android:animateLayoutChanges="true"
            android:orientation="vertical">
        <ImageView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" 
				app:srcCompat="@drawable/ic_launcher_background"
                android:id="@+id/animImage1"/>
				
        <ImageButton
                android:layout_width="match_parent"
                android:layout_height="wrap_content" 
				app:srcCompat="@mipmap/ic_launcher_round"
                android:id="@+id/imageButton"/>
</LinearLayout>

// 2.астройка
			
            val rootView = inflater.inflate(R.layout.sample_5, container, false) //встраиваем лейаут

            val transition = LayoutTransition() // создаем анимацию
            val layout = rootView.findViewById<LinearLayout>(R.id.layoutForTransition) // ищем анимируемый контейнер
            layout.layoutTransition = transition // задаем ему анимацию
            transition.enableTransitionType(LayoutTransition.CHANGING) // устанавливаем метод - по изменению


            val viewToShow = rootView.findViewById<ImageView>(R.id.animImage1) // ищем дочерный контрол для изменения состояния
			
			// Кодим еще что-нибудь ....

// 3.Меняем эти состояния по клику на кнопки

            showBtn.apply {
                setOnClickListener {
                    viewToShow.visibility = View.VISIBLE

                }
            }

            hideBtn.apply {
                setOnClickListener {
                    viewToShow.visibility = View.GONE

                }
            }
			
``` 

Можно задавать кастомные анимаций, следующий пример показывает как задать анимацию разворота элементе при появлении его в группе,а также анимацию поворта элемента
на 360 с последующим исчезновением при отключении вдимимости контрола:

``` 

            //Создаем анимацию
            val transition = LayoutTransition()
            
            //Создаем анимацию повяления элемента в группе с помощью ObjectAnimator.
            val animatorAppear = ObjectAnimator.ofFloat(null, View.ROTATION_Y, 0f, 90f, 180f, 270f, 360f)
            
            //Создаем комплексную анимацию исчезновения элемента.
            
            val animatorDisappear =
                ObjectAnimator.ofPropertyValuesHolder(null as Any?,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                    PropertyValuesHolder.ofFloat(View.ROTATION_Y, 0f, 360f)
                )
            
            // устанавливаем анимации по событиям в группе
            transition.setAnimator(LayoutTransition.APPEARING, animatorAppear)
            transition.setAnimator(LayoutTransition.DISAPPEARING, animatorDisappear)
            
            transition.setDuration(2000)

```