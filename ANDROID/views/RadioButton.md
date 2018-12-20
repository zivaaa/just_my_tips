# RadioButton (Радио кнопки)

Пример лейаута

``` 
    <RadioGroup
            android:layout_width="wrap_content"
            android:layout_height="60dp"
            app:layout_constraintEnd_toEndOf="parent" android:layout_marginEnd="8dp" android:layout_marginTop="8dp"
            app:layout_constraintTop_toTopOf="parent">

        <RadioButton
                android:text="RadioButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:id="@+id/radioButton2" android:layout_weight="1"/>

        <RadioButton
                android:text="RadioButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" android:id="@+id/radioButton3" android:layout_weight="1"/>
    </RadioGroup>

```

Динамическое добавление radioButton в RadioGroup примерно так

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

```

Доступ к текущему выбранному значению по Id в любой момент

``` 
	val checkedId = radioGroup.checkedRadioButtonId
	//... SOME CODE

	when (checkedId) {
                        1000 -> {
                            //SOME CODE
                        }
                        1001 -> {
                            //SOME CODE
                        }
						else -> {
							//SOME CODE
						}
	}
```

Получить RadioButton который был выбран в RadioGroup 

``` 
                    radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                            Toast.makeText(group?.context, "Checked id : ${checkedId}", Toast.LENGTH_SHORT).show()
                        }
                    })

```