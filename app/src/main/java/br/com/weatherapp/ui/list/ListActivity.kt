package br.com.weatherapp.ui.list

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.weatherapp.R
import br.com.weatherapp.api.RetrofitManager
import br.com.weatherapp.async.FavoritesAsyncTask
import br.com.weatherapp.async.TaskListener
import br.com.weatherapp.common.Constants
import br.com.weatherapp.data.RoomManager
import br.com.weatherapp.entity.City
import br.com.weatherapp.entity.Favorite
import br.com.weatherapp.entity.FindResult
import br.com.weatherapp.entity.enum.Lang
import br.com.weatherapp.ui.setting.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import br.com.weatherapp.entity.enum.Unit
import java.net.URL

class ListActivity : AppCompatActivity(), Callback<FindResult> {

    private lateinit var asyncTask: FavoritesAsyncTask

    private val sp : SharedPreferences by lazy {
        getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
    }

    val db: RoomManager? by lazy {
        RoomManager.getInstance(this)
    }

    private val adapter: ListAdapter by lazy {
        ListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        configureBtnSearch()
        configureSearchTextWatch()
        getFavoritesCities()
    }

    private fun initRecyclerView() {
        rvWeather.adapter = adapter
    }

    private fun configureBtnSearch(){
        btnSearch.setOnClickListener {
            if (isDeviceConnected()) {
                getCities()
            }
        }
    }

    private fun configureSearchTextWatch(){
        edtCity.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().length == 0) {
                    btnSearch.setEnabled(false)
                    getFavoritesCities()
                } else {
                    btnSearch.setEnabled(true)
                }
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getCities() {
        progressBar.visibility = View.VISIBLE
        val isCelsius = sp.getBoolean(Constants.PREFS_TEMP, true)
        val isEnglish = sp.getBoolean(Constants.PREFS_LANG, true)
        val unit = if(isCelsius) Unit.CELSIUS.unit else Unit.FAHRENHEIT.unit
        val lang = if(isEnglish) Lang.EN.toString() else Lang.PT.toString()

        val call = RetrofitManager.getWeatherService()
            .find(unit, lang, edtCity.text.toString(), Constants.API_KEY)
        call.enqueue(this)
    }

    private fun getCitiesFavorites(favorites: List<Int>?) {
        if(favorites != null && favorites.isNotEmpty()){
            progressBar.visibility = View.VISIBLE
            val isCelsius = sp.getBoolean(Constants.PREFS_TEMP, true)
            val isEnglish = sp.getBoolean(Constants.PREFS_LANG, true)
            val unit = if(isCelsius) Unit.CELSIUS.unit else Unit.FAHRENHEIT.unit
            val lang = if(isEnglish) Lang.EN.toString() else Lang.PT.toString()
            val ids = TextUtils.join(",", favorites)
            val call = RetrofitManager.getWeatherService()
                .findFavorites(unit, lang, ids, Constants.API_KEY)
            call.enqueue(this)
        }else{
            adapter.updateData(null)
        }
    }

    fun toggleFavorite(city: City){
        ToggleFavoriteAsync(this, city).execute()
    }

    private fun getFavorites(){
        asyncTask = FavoritesAsyncTask(this, object: TaskListener {
            override fun onTaskComplete(favorites: List<Int>?) {
                favorites?.let { adapter.updateFavorites(it) }
            }
        });
        asyncTask.execute()
    }
    private fun getFavoritesCities(){
        asyncTask = FavoritesAsyncTask(this, object: TaskListener {
            override fun onTaskComplete(favorites: List<Int>?) {
                getCitiesFavorites(favorites)
            }
        });
        asyncTask.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.setting_item) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return true
    }

    private fun isDeviceConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    override fun onFailure(call: Call<FindResult>, t: Throwable) {
        Log.e("WELL", "Error", t)
        progressBar.visibility = View.GONE
    }

    override fun onResponse(call: Call<FindResult>, response: Response<FindResult>) {
        if (response.isSuccessful) {
            adapter.updateData(response.body()?.list)
            getFavorites()
        }
        progressBar.visibility = View.GONE
    }

    class ToggleFavoriteAsync(val context: Context, val city: City)
        : AsyncTask<Void, Void, Boolean>() {

        val db = RoomManager.getInstance(context)

        override fun doInBackground(vararg p0: Void?): Boolean {
             var isSave = false
             var favorite: Favorite? = db?.getCityDao()?.favoriteById(city.id)
             if(favorite == null){
                 favorite = Favorite(city.id, city.name)
                 db?.getCityDao()?.insertFavorite(favorite)
                 isSave = true
             }else{
                 db?.getCityDao()?.deleteFavorite(favorite)
                 isSave = false
             }
            return isSave
        }

        override fun onPostExecute(isSave: Boolean) {
            super.onPostExecute(isSave)
            val activity = context as ListActivity
            activity.getFavorites()
            if(activity.edtCity.text.trim().isEmpty()){
                activity.getFavoritesCities()
            }
        }
    }
}
