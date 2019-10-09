package com.ntconcepts.gcpdemo1.models

import org.apache.avro.Schema
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.Serializable

data class TaxiTripOutput(
    var cash: Int?,
    var year: Int?,
    var start_time: String?,
    var start_time_epoch: Long?,
    var trip_miles: Double?,
    var company: String?,
    var ml_partition: String?,
    var pickup_latitude: Double?,
    var pickup_longitude: Double?,
    var pickup_lat_norm: Double?,
    var pickup_long_norm: Double?,
    var pickup_lat_std: Double?,
    var pickup_long_std: Double?,
    var daysOfWeekEncoded: HashMap<String, Int>?,
    var monthsEncoded: HashMap<String, Int>?,
    var companiesEncoded: HashMap<String, Int>?
) : Serializable {

    constructor() : this(
        0,
        0,
        "",
        0L,
        0.0,
        "",
        "",
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        HashMap<String, Int>(),
        HashMap<String, Int>(),
        HashMap<String, Int>()
    )

    fun toCSV(): String {
        val builder = StringBuilder()
        val printer = CSVPrinter(builder, CSVFormat.EXCEL)


        printer.print(cash)
        printer.print(year)
        printer.print(start_time_epoch)
        printer.print(trip_miles)
        printer.print(ml_partition)
        printer.print(pickup_latitude)
        printer.print(pickup_longitude)
        printer.print(pickup_lat_norm)
        printer.print(pickup_long_norm)
        printer.print(pickup_lat_std)
        printer.print(pickup_long_std)

        daysOfWeekEncoded?.toSortedMap()?.forEach {
            printer.print(it.value)
        }

        this.monthsEncoded?.toSortedMap()?.forEach {
            printer.print(it.value)
        }

        this.companiesEncoded?.toSortedMap()?.forEach {
            printer.print(it.value)
        }

        return builder.toString()
    }


    //Helper to generate schema for ParquetIO.sink()
    object SchemaGetter {
        fun schema(daysOfWeek: List<String>, months: List<String>): Schema {
            var wrapper = TaxiTripOutput()

            daysOfWeek.forEach {
                wrapper.daysOfWeekEncoded?.put(it, 0)
            }
            months.forEach {
                wrapper.monthsEncoded?.put(it, 0)
            }
            return wrapper.getSchema()
        }
    }

    fun getSchema(): Schema {

        val fields = ArrayList<Schema.Field>()
        fields.add(Schema.Field("cash", Schema.create(Schema.Type.INT), "cash", 0))
        fields.add(Schema.Field("year", Schema.create(Schema.Type.INT), "year", 0))
        fields.add(Schema.Field("start_time_epoch", Schema.create(Schema.Type.LONG), "start_time_epoch", 0L))
        fields.add(Schema.Field("trip_miles", Schema.create(Schema.Type.DOUBLE), "trip_miles", 0.0))
        fields.add(Schema.Field("ml_partition", Schema.create(Schema.Type.STRING), "ml_partition", ""))
        fields.add(Schema.Field("pickup_latitude", Schema.create(Schema.Type.DOUBLE), "pickup_latitude", 0.0))
        fields.add(Schema.Field("pickup_longitude", Schema.create(Schema.Type.DOUBLE), "pickup_longitude", 0.0))
        fields.add(Schema.Field("pickup_lat_norm", Schema.create(Schema.Type.DOUBLE), "pickup_lat_norm", 0.0))
        fields.add(Schema.Field("pickup_long_norm", Schema.create(Schema.Type.DOUBLE), "pickup_long_norm", 0.0))
        fields.add(Schema.Field("pickup_lat_std", Schema.create(Schema.Type.DOUBLE), "pickup_lat_std", 0.0))
        fields.add(Schema.Field("pickup_long_std", Schema.create(Schema.Type.DOUBLE), "pickup_long_std", 0.0))

        this.daysOfWeekEncoded?.toSortedMap()?.forEach {
            fields.add(Schema.Field(it.key, Schema.create(Schema.Type.INT), it.key, 0))
        }

        this.monthsEncoded?.toSortedMap()?.forEach {
            fields.add(Schema.Field(it.key, Schema.create(Schema.Type.INT), it.key, 0))
        }

        return Schema.createRecord(
            this::class.java.simpleName,
            this::class.java.canonicalName,
            this::class.java.`package`.name,
            false,
            fields
        )

    }
}