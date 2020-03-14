package com.example.menumakanan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.menumakanan.Filters

class MainActivityViewModel : ViewModel() {

    var isSigningIn = false
    var filters: Filters = Filters.default

}