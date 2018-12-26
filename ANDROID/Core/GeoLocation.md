# GeoLocation

Гео данные можно нужно получать с помощью LocationManager.
У него есть несколько провайдеров, которые могут предоставлять необходимую инфу.

 - Network provider, получает данные из сети
 - GPS provider, получает данные со спутников

Делать это нужно как описано в [официальном руководстве](https://developer.android.com/guide/topics/location/strategies)

### Разрешения

Надо задать разрешения в манифесте

```
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-feature android:name="android.hardware.location.gps" />
    <uses-feature android:name="android.hardware.location.network" />

```

можно юзать ACCESS_COARSE_LOCATION если не нужен gps.

Еще надо запросить разрешение у юзера

```

activity.requestPermissions(
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
    LOCATION_PERMISSION_CODE
)

```

После чего можно запрашивать данные.

### Получение данных

```
var locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

//...

var innerListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location?) {

        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }

        override fun onProviderEnabled(provider: String?) {

        }

        override fun onProviderDisabled(provider: String?) {

        }
    }

//...

//for network provider

var minUpdateTimeInMillisec = 1000 * 10;
var minUpdateDistanceInMeters = 5f;

locationManager.requestLocationUpdates(
    LocationManager.NETWORK_PROVIDER,
    minUpdateTimeInMillisec,
    minUpdateDistanceInMeters,
    innerListener
)

//for gps provider

locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    minUpdateTimeInMillisec,
    minUpdateDistanceInMeters,
    innerListener
)

```

Можно также запросить последнее местоположение у провайдеров

```
val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//or
val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

```

Задаем для провайдера минимальное значение обновления данных и дистанцию обновления и все.
Правда еще есть сложности с точностью и актуальностью данных, так что приедтся написать проверялку данных, как в руководстве например.

### PS

Примерчик гдето [тут](https://github.com/zivaaa/just_my_tips/blob/master/ANDROID/_SAMPLE_APPS/GeoLocationGoogleMap) лежал.







