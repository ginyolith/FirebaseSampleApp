package jp.ginyolith.firebasesampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


class MainActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig : FirebaseRemoteConfig
    private lateinit var mWelcomeTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mWelcomeTextView = findViewById(R.id.welcome_textview)

        /**
         * Remote Config オブジェクトのインスタンスを取得し、デベロッパー モードを有効にしてキャッシュ更新の頻度を増やせるようにします。
         */
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)

        // Fetchの間隔を設定。デフォルトは12時間
        val fetchInterval = 0L
        // キャッシュ時間の間は、リモートから取得ではなくキャッシュした値を受け取る。
        // intervalが0の場合は都度リモートからフェッチするが、1時間に5回以上アクセスされた場合はその1時間はキャッシュから取得する

        // Configの値をfetch
        mFirebaseRemoteConfig.fetch(fetchInterval)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Fetch Succeeded",
                                Toast.LENGTH_SHORT).show()

                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig.activateFetched()
                    } else {
                        Toast.makeText(this@MainActivity, "Fetch Failed",
                                Toast.LENGTH_SHORT).show()
                    }
                    displayWelcomeMessage()
                }
    }

    private fun displayWelcomeMessage() {
        val message = """
            ${RemoteConfigKeys.message.name}:${mFirebaseRemoteConfig.getString(RemoteConfigKeys.message.name)}
            ${RemoteConfigKeys.app_install_url.name}:${mFirebaseRemoteConfig.getString(RemoteConfigKeys.app_install_url.name)}
            ${RemoteConfigKeys.is_force_update.name}:${mFirebaseRemoteConfig.getBoolean(RemoteConfigKeys.is_force_update.name)}
            ${RemoteConfigKeys.version.name}:${mFirebaseRemoteConfig.getString(RemoteConfigKeys.version.name)}
        """.trimIndent()

        mWelcomeTextView.text = message
    }

    enum class RemoteConfigKeys{
        message
        ,app_install_url
        ,is_force_update
        ,version
    }
}
