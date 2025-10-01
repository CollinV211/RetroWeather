package edu.cofc.android.Retro_Weather
import java.net.URL
import org.json.JSONObject
import android.util.Log
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import edu.cofc.android.myapplication.R
import edu.cofc.android.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissions()

        binding.startButton.setOnClickListener {
            binding.startScreen.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction {
                    binding.startScreen.visibility = View.GONE
                }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(
                Manifest.permission.ACCESS_FINE_LOCATION, false
            ) -> {
            }

            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            ) -> {
            }

            else -> {
                binding.TodayTemp.text = "No permission"
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()
        } else if (shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
// show dialog with permission rationale
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {

        val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val cancellationTokenSource = CancellationTokenSource()
        resetUI()

        try {
            fusedLocationClient.getCurrentLocation(
                priority,
                cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        ioScope.launch {
                            val forecastList = fetchWeather(lat, lon)

                            runOnUiThread {
                                binding.TodayTextInfo.text=forecastList[0].first
                                binding.TodayTemp.text = forecastList[0].second
                                binding.TodayImage.setImageResource(forecastList[0].third)


                                binding.NextDay1.text = forecastList[1].first
                                binding.NextTemp1.text = forecastList[1].second
                                binding.NextImage1.setImageResource(forecastList[1].third)

                                binding.NextDay2.text = forecastList[2].first
                                binding.NextTemp2.text = forecastList[2].second
                                binding.NextImage2.setImageResource(forecastList[2].third)


                                binding.NextDay3.text = forecastList[3].first
                                binding.NextTemp3.text = forecastList[3].second
                                binding.NextImage3.setImageResource(forecastList[3].third)


                                binding.NextDay4.text = forecastList[4].first
                                binding.NextTemp4.text = forecastList[4].second
                                binding.NextImage4.setImageResource(forecastList[4].third)


                                binding.NextDay5.text = forecastList[5].first
                                binding.NextTemp5.text = forecastList[5].second
                                binding.NextImage5.setImageResource(forecastList[5].third)


                                binding.NextDay6.text = forecastList[6].first
                                binding.NextTemp6.text = forecastList[6].second
                                binding.NextImage6.setImageResource(forecastList[6].third)
                                when (forecastList[0].third) {
                                    R.drawable.sun -> {
                                        binding.TodayAnimation.setImageResource(R.drawable.sun)
                                        binding.TodayAnimation.visibility = View.VISIBLE
                                        val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.sunny)
                                        binding.TodayAnimation.startAnimation(animation)
                                    }
                                    R.drawable.cloudy1 -> {
                                        binding.TodayAnimation.setImageResource(R.drawable.cloudy1)
                                        binding.TodayAnimation.visibility = View.VISIBLE
                                        val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cloud)
                                        binding.TodayAnimation.startAnimation(animation)
                                    }
                                    R.drawable.raindrop -> {
                                        binding.TodayAnimation.setImageResource(R.drawable.raindrop)
                                        binding.TodayAnimation.visibility = View.VISIBLE
                                        val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.rain)
                                        binding.TodayAnimation.startAnimation(animation)
                                    }
                                    else -> {
                                        binding.TodayAnimation.clearAnimation()
                                        binding.TodayAnimation.visibility = View.GONE
                                    }
                                }



                            }
                        }
                    }else{
                        binding.TodayTemp.text = "No Data"
                    }
                }
                .addOnFailureListener { exception ->
                    binding.TodayTemp.text = "Failed to get location"
                }
        } catch (ex: SecurityException) {
            Log.e("Location", "SecurityException: ${ex.message}")
        }
    }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private suspend fun fetchWeather(lat: Double, lon: Double): MutableList<Triple<String, String,Int>> {
        val forecastList = mutableListOf<Triple<String, String,Int>>()
        val baseUrl = "https://api.open-meteo.com/v1/forecast"

        val urlString = "$baseUrl?latitude=$lat&longitude=$lon&daily=temperature_2m_max,temperature_2m_min,weathercode&timezone=auto"

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val response = url.readText()

                Log.d("WeatherFetch", "URL: $urlString")
                Log.d("WeatherFetch", "Response: $response")

                val jsonObject = JSONObject(response)

                val daily = jsonObject.getJSONObject("daily")
                val dates = daily.getJSONArray("time")
                val temps = daily.getJSONArray("temperature_2m_max")
                val tempsmin=daily.getJSONArray("temperature_2m_min")
                val weatherCodes = daily.getJSONArray("weathercode")

                for (i in 0 until minOf(7, dates.length())) {
                    val dateString = dates.getString(i)
                    //temps are given in Cel, this converts them to Far (average for the day)
                    val tempMax = (((temps.getDouble(i)+tempsmin.getDouble(i))/2)*9/5)+32
                    val weatherCode = weatherCodes.getInt(i)

                    val weatherIcon = when (weatherCode) {
                        in 0..1 -> R.drawable.sun   // clear sky
                        in 2..3 -> R.drawable.cloudy1  // partly sunny/ cloudy
                        in 45..48 -> R.drawable.fog   // fog
                        in 51..67, in 80..82 -> R.drawable.raindrop   // rain
                        else -> R.drawable.cloudy1   // fallback
                    }

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDate.parse(dateString, formatter)
                    val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEE"))

                    forecastList.add(Triple(dayOfWeek, "$tempMax°F",weatherIcon))
                }

            } catch (ex: Exception) {
                Log.e("WeatherFetch", ex.stackTraceToString())
                forecastList.add(Triple("Error", "",0))
            }

            forecastList
        }
    }
    private fun resetUI() {
        binding.TodayTextInfo.text = "Loading..."
        binding.TodayTemp.text = "--"

        val days = listOf(
            binding.NextDay1, binding.NextDay2, binding.NextDay3,
            binding.NextDay4, binding.NextDay5, binding.NextDay6
        )

        val temps = listOf(
            binding.NextTemp1, binding.NextTemp2, binding.NextTemp3,
            binding.NextTemp4, binding.NextTemp5, binding.NextTemp6
        )

        for (i in 0 until 6) {
            days[i].text = "Loading..."
            temps[i].text = "--"
        }
    }

}