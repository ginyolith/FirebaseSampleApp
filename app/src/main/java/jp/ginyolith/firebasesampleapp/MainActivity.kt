package jp.ginyolith.firebasesampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel


class MainActivity : AppCompatActivity() {
    private lateinit var mWelcomeTextView : TextView
    private lateinit var button : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWelcomeTextView = findViewById(R.id.welcome_textview)
        button = findViewById(R.id.button)


        button.setOnClickListener{
            Fuel.get("https://us-central1-fir-test-10478.cloudfunctions.net/helloWorld").responseString { request, response, result ->


                val text = """
                ## Request
                - url:${request.url}

                ## response
                - status: ${response.statusCode}
                - body : $result
            """.trimIndent()


                mWelcomeTextView.text = text

            }
        }
    }
}
