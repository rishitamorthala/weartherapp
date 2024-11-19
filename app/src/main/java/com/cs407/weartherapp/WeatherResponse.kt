package com.cs407.weartherapp

data class WeatherResponse(
    val data: List<WeatherData>
)

data class WeatherData(
    val parameter: String,
    val coordinates: List<Coordinate>
)

data class Coordinate(
    val lat: Double,
    val lon: Double,
    val dates: List<DateValue>
)

data class DateValue(
    val date: String,
    val value: Double
)