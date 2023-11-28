package com.hexterlabs.listdetail.ui

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.hexterlabs.listdetail.R

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        if (imgUrl.isEmpty()) {
            imgView.setBackgroundColor(Color.parseColor("#dae0e3"))
        } else {
            Glide.with(imgView.context)
                .load(it)
                .error(R.drawable.ic_broken_image)
                .placeholder(R.drawable.loading_animation)
                .into(imgView)
        }
    }
}

@BindingAdapter("distanceText")
fun bindDistanceText(textView: TextView, distance: Int?) {
    distance?.let {
        if (distance < Int.MAX_VALUE) {
            textView.text = textView.context.getString(R.string.distance_away, distance)
        }
    }
}

@BindingAdapter("ratingText")
fun bindRatingText(textView: TextView, rating: Double?) {
    rating?.let {
        if (rating > 0.0 && rating < Double.MAX_VALUE) {
            textView.visibility = View.VISIBLE
            textView.text = textView.context.getString(R.string.venue_rating, rating)
        } else {
            textView.visibility = View.GONE
        }
    }
}

@BindingAdapter("visibilityOnText")
fun bindVisibilityOnText(view: View, text: String?) {
    text?.let {
        if (text.isEmpty()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("refreshDataState")
fun bindRefreshDataState(view: View, refreshDataState: ListDetailViewModel.RefreshDataStatus?) {
    refreshDataState?.let {
        when (view.id) {
            R.id.venues_progress -> {
                when (refreshDataState) {
                    ListDetailViewModel.RefreshDataStatus.LOADING -> {
                        view.visibility = View.VISIBLE
                    }

                    ListDetailViewModel.RefreshDataStatus.SUCCESS -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.NOT_FOUND -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.FAILURE -> {
                        view.visibility = View.GONE
                    }
                }
            }

            R.id.venues_error_image -> {
                when (refreshDataState) {
                    ListDetailViewModel.RefreshDataStatus.LOADING -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.SUCCESS -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.NOT_FOUND -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.FAILURE -> {
                        view.visibility = View.VISIBLE
                    }
                }
            }

            R.id.venues_no_result -> {
                when (refreshDataState) {
                    ListDetailViewModel.RefreshDataStatus.LOADING -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.SUCCESS -> {
                        view.visibility = View.GONE
                    }

                    ListDetailViewModel.RefreshDataStatus.NOT_FOUND -> {
                        view.visibility = View.VISIBLE
                    }

                    ListDetailViewModel.RefreshDataStatus.FAILURE -> {
                        view.visibility = View.GONE
                    }
                }
            }
        }
    }
}