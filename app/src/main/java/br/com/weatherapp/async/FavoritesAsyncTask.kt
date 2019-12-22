package br.com.weatherapp.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import br.com.weatherapp.R
import br.com.weatherapp.data.RoomManager
import br.com.weatherapp.entity.Favorite

class FavoritesAsyncTask (private val mContext: Context, private val mlistener: TaskListener): AsyncTask<Void, Void, List<Int>?>(){

    val db = RoomManager.getInstance(mContext)
    private val mDialog: ProgressDialog

    init {
        mDialog = ProgressDialog(mContext)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog.setTitle(R.string.app_name)
        mDialog.setMessage("Retrieving data...")
        mDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): List<Int>? {
        val favorites: List<Int>? = db?.getCityDao()?.allIdsFavorites();
        return favorites
    }

    override fun onPostExecute(favorites: List<Int>?) {
        super.onPostExecute(favorites)
        mDialog.dismiss()
        mlistener.onTaskComplete(favorites)
    }
}

interface TaskListener{
    fun onTaskComplete(favorites: List<Int>?)
}