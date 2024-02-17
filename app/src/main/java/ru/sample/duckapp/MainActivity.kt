package ru.sample.duckapp

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.sample.duckapp.data.DucksApi
import ru.sample.duckapp.domain.Duck
import ru.sample.duckapp.data.RetrofitInstance
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var duckImageView: ImageView
    private lateinit var nextDuckButton: Button
    private lateinit var httpCodeEditText: EditText

    private val ducksApi: DucksApi = RetrofitInstance.retrofitInstance.create(DucksApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        duckImageView = findViewById(R.id.duckImageView)
        nextDuckButton = findViewById(R.id.nextDuckButton)
        httpCodeEditText = findViewById(R.id.httpCodeEditText)

        loadRandomDuck()

        nextDuckButton.setOnClickListener {
            if (httpCodeEditText.text.isNullOrEmpty()) {
                loadRandomDuck()
            } else {
                val httpCode = httpCodeEditText.text.toString().trim()
                loadDuckWithHttpCode(httpCode)
            }
        }
    }

    private fun loadRandomDuck() {
        val call: Call<Duck> = ducksApi.getRandomDuck()

        call.enqueue(object : Callback<Duck> {
            override fun onResponse(call: Call<Duck>, response: Response<Duck>) {
                if (response.isSuccessful) {
                    val duck: Duck? = response.body()
                    duck?.let {
                        Picasso.get().load(it.url).into(duckImageView)
                    }
                }
            }

            override fun onFailure(call: Call<Duck>, t: Throwable) {
                Log.e("DuckApp", "Failed to fetch duck image: ${t.message}", t)
            }
        })
    }
    private fun loadDuckWithHttpCode(httpCode: String) {
        if (httpCode.toInt() < 100 || httpCode.toInt() > 599) {
            Toast.makeText(this, "Неверный HTTP код", Toast.LENGTH_SHORT).show()
            return
        }
        val call: Call<ResponseBody> = ducksApi.getDuckByHttpCode(httpCode)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val inputStream: InputStream? = response.body()?.byteStream()
                    inputStream?.let {
                        val bitmap = BitmapFactory.decodeStream(it)
                        duckImageView.setImageBitmap(bitmap)
                    }
                } else {
                    Toast.makeText(applicationContext, "Такой утошки нет :(", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DuckApp", "Failed to fetch duck image: ${t.message}", t)
            }
        })
    }

}
