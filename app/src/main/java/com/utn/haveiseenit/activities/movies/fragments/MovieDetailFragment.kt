package com.utn.haveiseenit.activities.movies.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import com.utn.haveiseenit.R
import com.utn.haveiseenit.activities.movies.viewModels.MovieView
import com.utn.haveiseenit.activities.movies.viewModels.MovieViewModel
import com.utn.haveiseenit.entities.MovieStatuses

class MovieDetailFragment(private val position: Int) : Fragment() {
    private lateinit var v: View
    private lateinit var textView: TextView
    private lateinit var movieView: MovieView
    private val movieViewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        v = inflater.inflate(R.layout.fragment_movie_detail, container, false)
        movieViewModel.getMovies().observe(requireActivity(), Observer<List<MovieView>> { movies ->
            movieView = movies[position]
            v.findViewById<TextView>(R.id.detail_title_text).text = movieView.movie.title
            v.findViewById<TextView>(R.id.detail_year_text).text = movieView.movie.year.toString()
            v.findViewById<TextView>(R.id.detail_director_text).text = movieView.movie.director
            v.findViewById<TextView>(R.id.detail_duration_text).text =
                movieView.movie.durationMin.toString() + " min"
            v.findViewById<TextView>(R.id.detail_score_text).text =
                if (movieView.movie.rating == null) "-" else movieView.movie.rating.toString()
            textView = v.findViewById(R.id.detail_status_text)
            setStatus(movieView.movie.status)
            movieView.loadPoster(v.context) { drawable ->
                v.findViewById<ImageView>(R.id.detail_poster_image).setImageDrawable(drawable)
            }
            v.findViewById<TextView>(R.id.detail_status_text).setOnClickListener {
                changeMovieState()
            }
            v.findViewById<ImageView>(R.id.edit_state).setOnClickListener {
                changeMovieState()
            }
            v.findViewById<ImageView>(R.id.edit_score).setOnClickListener {
                changeMovieScore()
            }
        })
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.empty_toolbar, menu)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun changeMovieState() {
        // TODO: fix this ugliness
        val statuses = arrayOf(
            MovieStatuses.pending,
            MovieStatuses.started,
            MovieStatuses.seen,
            MovieStatuses.inReview
        )
        val displayStatuses = arrayOf(
            getString(R.string.movie_status_to_watch),
            getString(R.string.movie_status_started),
            getString(R.string.movie_status_seen),
            getString(R.string.movie_status_in_review)
        )
        val builder = AlertDialog.Builder(requireContext(), R.style.Dialog)
        var index: Int? = null
        builder.setTitle(getString(R.string.dialog_title))
        builder.setSingleChoiceItems(displayStatuses, -1) { _, i ->
            index = i
        }

        builder.setPositiveButton(getString(R.string.dialog_close)) { _, _ ->
            if (index != null) {
                movieViewModel.updateMovieStatus(position, statuses[index!!])
                setStatus(movieView.movie.status)
            }
        }
        builder.show()
    }

    private fun changeMovieScore() {
        val editText = v.findViewById<EditText>(R.id.detail_score_text)
        editText.setText("")
        editText.filters = arrayOf<InputFilter>(InputFilterMinMax(0.0F, 10.0F))
        editText.isEnabled = true
        editText.requestFocus()
    }

    private fun setStatus(status: String) {
        when (status) {
            MovieStatuses.pending -> {
                textView.text =
                    getString(R.string.movie_status_to_watch)
                val drawable = textView.background as GradientDrawable
                drawable.setColor(v.context.getColor(R.color.yellowColor));
            }
            MovieStatuses.started -> {
                textView.text =
                    getString(R.string.movie_status_started)
                val drawable = textView.background as GradientDrawable
                drawable.setColor(v.context.getColor(R.color.orangeColor));
            }
            MovieStatuses.seen -> {
                textView.text =
                    getString(R.string.movie_status_seen)
                val drawable = textView.background as GradientDrawable
                drawable.setColor(v.context.getColor(R.color.greenColor));
            }
            MovieStatuses.inReview -> {
                textView.text =
                    getString(R.string.movie_status_in_review)
                val drawable = textView.background as GradientDrawable
                drawable.setColor(v.context.getColor(R.color.chillRedColor));
            }
            MovieStatuses.reviewed -> {
                textView.text =
                    getString(R.string.movie_status_reviewed)
                val drawable = textView.background as GradientDrawable
                drawable.setColor(v.context.getColor(R.color.blueColor));
            }
        }
    }

    class InputFilterMinMax(min: Float, max: Float) : InputFilter {
        private var min: Float = 0.0F
        private var max: Float = 0.0F

        init {
            this.min = min
            this.max = max
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            try {
                val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(
                    dend,
                    dest.length
                ))
                if (isInRange(min, max, input.toFloat()) && validDecimalPlace(input))
                    return null
            } catch (nfe: NumberFormatException) {
            }
            return ""
        }

        private fun isInRange(a: Float, b: Float, c: Float): Boolean {
            return if (b > a) c in a..b else c in b..a
        }

        private fun validDecimalPlace(a: String): Boolean {
            val decimalPlace = a.split('.').getOrNull(index = 1)
            return if (decimalPlace == null) true else decimalPlace.length < 2
        }
    }
}