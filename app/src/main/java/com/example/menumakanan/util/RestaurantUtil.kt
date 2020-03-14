package com.example.menumakanan.util

import android.content.Context
import com.example.menumakanan.R
import com.example.menumakanan.model.Restaurant
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RestaurantUtil {

    companion object {
        const val TAG = "RestaurantUtil"

        val EXECUTOR: ThreadPoolExecutor = ThreadPoolExecutor(
            2, 4, 60,
            TimeUnit.SECONDS, LinkedBlockingQueue()
        )

        const val RESTAURANT_URL_FMT =
            "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png"

        const val MAX_IMAGE_NUM = 22

        val NAME_FIRST_WORDS = arrayOf(
            "Foo",
            "Bar",
            "Baz",
            "Qux",
            "Fire",
            "Sam's",
            "World Famous",
            "Google",
            "The Best"
        )

        val NAME_SECONDS_WORDS = arrayOf(
            "Restaurant",
            "Cafe",
            "Spot",
            "Eatin' Place",
            "Eatery",
            "Drive Thru",
            "Diner"
        )

        fun getRandom(context: Context): Restaurant {
            val random: Random = Random()

            // Cities (first element is "Any")
            var cities = context.resources.getStringArray(R.array.cities)
            cities = cities.copyOfRange(1, cities.size)

            // Categories (first element is "Any")
            var categories = context.resources.getStringArray(R.array.categories)
            categories = categories.copyOfRange(1, categories.size)

            val prices = intArrayOf(1, 2, 3)

            return Restaurant(
                getRandomName(random),
                getRandomString(cities, random),
                getRandomString(categories, random),
                getRandomImageUrl(random),
                getRandomInt(prices, random),
                random.nextInt(20),
                getRandomRating(random)
            )

        }

        private fun getRandomString(
            array: Array<String>,
            random: Random
        ): String? {
            val ind = random.nextInt(array.size)
            return array[ind]
        }

        private fun getRandomInt(array: IntArray, random: Random): Int {
            val ind = random.nextInt(array.size)
            return array[ind]
        }

        fun getRandomName(random: Random): String {
            return getRandomString(NAME_FIRST_WORDS, random) + "" +
                    getRandomString(NAME_SECONDS_WORDS, random)
        }

        fun getPriceString(restaurant: Restaurant): String {
            return getPriceString(restaurant.price)
        }

        fun getPriceString(priceInt: Int?): String {
            when(priceInt) {
                1 -> {
                    return "$"
                }

                2 -> {
                    return "$$"
                }

                3 -> {
                    return "$$$"
                }

                else -> {
                    return "$$$"
                }

            }
        }

        fun getRandomImageUrl(random: Random): String {
            val id = random.nextInt(MAX_IMAGE_NUM) + 1
            return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id)
        }

        fun getRandomRating(random: Random): Double {
            val min = 1.0
            return min + (random.nextDouble() * 4.0)
        }

    }

}