package com.ntconcepts.gcpdemo1.models

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericRecordBuilder
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.Serializable

data class TaxiTripOutput(
    var unique_key: String?,
    var cash: Int?,
    var year: Int,
    var year_norm: Double,
    var start_time: String?,
    var start_time_epoch: Long?,
    var start_time_norm_midnight: Double?,
    var start_time_norm_noon: Double?,
    var trip_miles: Double?,
    var company: String?,
    var ml_partition: String?,
    var distance_from_center: Double?,
    var pickup_latitude: Double?,
    var pickup_longitude: Double?,
    var pickup_lat_centered: Double?,
    var pickup_long_centered: Double?,
    var pickup_lat_norm: Double?,
    var pickup_long_norm: Double?,
    var pickup_lat_std: Double?,
    var pickup_long_std: Double?,
    var daysOfWeekEncoded: HashMap<String, Int>?,
    var monthsEncoded: HashMap<String, Int>?,
    var companiesEncoded: HashMap<String, Int>?
) : Serializable {

    constructor() : this(
        "",
        0,
        0,
        0.0,
        "",
        0L,
        0.0,
        0.0,
        0.0,
        "",
        "",
        0.0,
        0.0,
        0.0,
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

    fun toCSVHeader(): String {
        val builder = StringBuilder()
        val printer = CSVPrinter(builder, CSVFormat.EXCEL)

        printer.print(::cash.name)
        printer.print(::year.name)
        printer.print(::year_norm.name)
        printer.print(::start_time_epoch.name)
        printer.print(::start_time_norm_midnight.name)
        printer.print(::start_time_norm_noon.name)
        printer.print(::trip_miles.name)
//        printer.print(::distance_from_center.name)
        printer.print(::pickup_lat_centered.name)
        printer.print(::pickup_long_centered.name)
        printer.print(::pickup_lat_norm.name)
        printer.print(::pickup_long_norm.name)
        printer.print(::pickup_lat_std.name)
        printer.print(::pickup_long_std.name)

        daysOfWeekEncoded?.toSortedMap()?.forEach {
            printer.print(it.key)
        }

        this.monthsEncoded?.toSortedMap()?.forEach {
            printer.print(it.key)
        }

        this.companiesEncoded?.toSortedMap()?.forEach {
            printer.print(it.key)
        }

        return builder.toString()
    }

    fun toCSV(): String {
        val builder = StringBuilder()
        val printer = CSVPrinter(builder, CSVFormat.EXCEL)

        printer.print(cash)
        printer.print(year)
        printer.print(year_norm)
        printer.print(start_time_epoch)
        printer.print(start_time_norm_midnight)
        printer.print(start_time_norm_noon)
        printer.print(trip_miles)
//        printer.print(distance_from_center)
        printer.print(pickup_lat_centered)
        printer.print(pickup_long_centered)
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

    fun toGenericRecord(): GenericRecord {
        val record = GenericRecordBuilder(getAvroSchema())
            .set("cash", cash)
            .set("year", year)
            .set("year_norm", year_norm)
            .set("start_time_epoch", start_time_epoch)
            .set("start_time_norm_midnight", start_time_norm_midnight)
            .set("start_time_norm_noon", start_time_norm_noon)
            .set("trip_miles", trip_miles)
//            .set("ml_partition", trip.ml_partition)
//            .set("distance_from_center", trip.distance_from_center)
            .set("pickup_latitude", pickup_latitude)
            .set("pickup_latitude", pickup_latitude)
            .set("pickup_lat_centered", pickup_lat_centered)
            .set("pickup_long_centered", pickup_long_centered)
            .set("pickup_lat_norm", pickup_lat_norm)
            .set("pickup_long_norm", pickup_long_norm)
            .set("pickup_lat_std", pickup_lat_std)
            .set("pickup_long_std", pickup_long_std)

        daysOfWeekEncoded?.toSortedMap()?.forEach {
            record.set(it.key, it.value)
        }

        monthsEncoded?.toSortedMap()?.forEach {
            record.set(it.key, it.value)
        }

        return record.build() as GenericRecord
    }


    //Helper to generate schema for ParquetIO.sink()
    object AvroSchemaGetter {
        fun schema(daysOfWeek: List<String>, months: List<String>): Schema {
            var wrapper = TaxiTripOutput()

            daysOfWeek.forEach {
                wrapper.daysOfWeekEncoded?.put(it, 0)
            }
            months.forEach {
                wrapper.monthsEncoded?.put(it, 0)
            }
            return wrapper.getAvroSchema()
        }
    }

    fun getAvroSchema(): Schema {

        val fields = ArrayList<Schema.Field>()
        fields.add(Schema.Field("cash", Schema.create(Schema.Type.INT), "cash", 0))
        fields.add(Schema.Field("year", Schema.create(Schema.Type.INT), "year", 0))
        fields.add(Schema.Field("year_norm", Schema.create(Schema.Type.DOUBLE), "year_norm", 0))
        fields.add(Schema.Field("start_time_epoch", Schema.create(Schema.Type.LONG), "start_time_epoch", 0L))
        fields.add(
            Schema.Field(
                "start_time_norm_midnight",
                Schema.create(Schema.Type.DOUBLE),
                "start_time_norm_midnight",
                0.0
            )
        )
        fields.add(Schema.Field("start_time_norm_noon", Schema.create(Schema.Type.DOUBLE), "start_time_norm_noon", 0.0))
        fields.add(Schema.Field("trip_miles", Schema.create(Schema.Type.DOUBLE), "trip_miles", 0.0))
        fields.add(Schema.Field("pickup_latitude", Schema.create(Schema.Type.DOUBLE), "pickup_latitude", 0.0))
        fields.add(Schema.Field("pickup_longitude", Schema.create(Schema.Type.DOUBLE), "pickup_longitude", 0.0))
        fields.add(Schema.Field("pickup_lat_centered", Schema.create(Schema.Type.DOUBLE), "pickup_lat_centered", 0.0))
        fields.add(Schema.Field("pickup_long_centered", Schema.create(Schema.Type.DOUBLE), "pickup_long_centered", 0.0))
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