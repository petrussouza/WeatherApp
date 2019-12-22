package br.com.weatherapp.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import br.com.weatherapp.R
import br.com.weatherapp.entity.City
import br.com.weatherapp.entity.Favorite
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_weather_layout.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private var list: List<City>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_weather_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = list?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list?.let {
            holder.bind(it[position])
        }
    }

    fun updateData(list: List<City>?) {
        this.list = list
        notifyDataSetChanged()
    }

    public fun updateFavorites(favorites: List<Int>) {
        if(this.list != null){
            this.list?.forEach(){
                if(favorites.contains(it.id)){
                    it.isFavorite = true
                }
            }
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(city: City) {
            itemView.tvCity.text = "${city.name}, ${city.sys.country}"
            itemView.tvWeatherValue.text = city.main.temp.toInt().toString()
            itemView.tvWindSpeed.text = itemView.context.getString(R.string.wind_speed, city.wind.speed)
            itemView.tvCloudPercent.text = itemView.context.getString(R.string.clouds_percent, city.clouds.all)
            itemView.tvPressure.text = itemView.context.getString(R.string.pressure_hpa, city.main.pressure)
            if (city.weather.isNotEmpty()) {
                itemView.tvWeatherDescription.text = city.weather[0].description
                Glide.with(itemView.context)
                    .load("http://openweathermap.org/img/w/${city.weather[0].icon}.png")
//                    .placeholder(R.drawable.ic_launcher_background)
                    .into(itemView.imgWeatherIcon)
            }
            if(city.isFavorite){
                itemView.btnFavorite.setImageResource(android.R.drawable.star_big_on)
            }else{
                itemView.btnFavorite.setImageResource(android.R.drawable.star_big_off)
            }
            itemView.btnFavorite.setOnClickListener {
                city.isFavorite = !city.isFavorite
                if(city.isFavorite){
                    itemView.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                }else{
                    itemView.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                }
                (itemView.context as ListActivity).toggleFavorite(city)
            }
        }
    }

}