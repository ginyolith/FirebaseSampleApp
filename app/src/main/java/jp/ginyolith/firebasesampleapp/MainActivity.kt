package jp.ginyolith.firebasesampleapp

import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import jp.ginyolith.firebasesampleapp.databinding.ActivityMainBinding
import jp.ginyolith.firebasesampleapp.databinding.ItemRowBinding


class MainActivity : AppCompatActivity() {

    private lateinit var mFirebaseRemoteConfig : FirebaseRemoteConfig
    private lateinit var mWelcomeTextView : TextView
    private lateinit var db : FirebaseFirestore
    private lateinit var binding : ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setUpFirestore()

        db.collection("messages").get().addOnCompleteListener {task ->

            binding.list.let {
                if (task.result?.toMutableList() == null) {
                    return@let
                }

                adapter.categories = task.result.toMutableList()
                it.adapter = adapter
                it.layoutManager = object : LinearLayoutManager(this) {
                    // 縦スクロール不可
                    override fun canScrollVertically(): Boolean = false
                }
            }
        }


        db.collection("messages").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            binding.list.let {
                if (querySnapshot?.documents?.toMutableList() == null) {
                    return@let
                }

                adapter.update(querySnapshot.documents.toMutableList())
            }

            Toast.makeText(this@MainActivity, querySnapshot?.documents.toString(), Toast.LENGTH_SHORT).show()
        }

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
        // キャッシュ時間の間は、リモートから取得ではなくキャッシュした値を受け取る。=リモートが更新されても反映されない
        // intervalが0の場合は都度リモートからフェッチするが、1時間に5回以上アクセスされた場合はその1時間はキャッシュから取得する

        // RemoteConfigの値をfetch
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
                }
        FirebaseAnalytics.getInstance(this).setUserProperty("device", Build.MODEL)


    }

    private fun setUpFirestore() {
        db = FirebaseFirestore.getInstance()
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

    private val adapter = object : RecyclerView.Adapter<BindingHolder>() {
        lateinit var categories : MutableList<DocumentSnapshot>

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRowBinding.inflate(inflater, parent, false)
            return BindingHolder(binding)
        }

        fun update(list : MutableList<DocumentSnapshot> ) {
            categories = list
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: BindingHolder, position: Int) {
            // Bind
            try {
                categories.get(position).let {
                    holder.binding.message.text = it.data?.getValue("message") as String
                    holder.binding.name.text = it.data?.getValue("name") as String
                    holder.binding.device.text = it.data?.getValue("device") as String
                }
            } catch (e: Exception) {
            }
        }
    }

    private class BindingHolder(var binding : ItemRowBinding) : RecyclerView.ViewHolder(binding.root)


}
