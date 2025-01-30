package com.ion606.workoutapp.dataObjects

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.ion606.workoutapp.screens.activeExercise.SuperSet
import kotlinx.coroutines.flow.Flow
import java.lang.reflect.Type
import java.util.UUID


enum class ExerciseMeasureType(val value: Int) {
    REP_BASED(0),
    TIME_BASED(1),
    DISTANCE_BASED(2);

    companion object {
        fun fromValue(value: Int): ExerciseMeasureType {
            return entries.find { it.value == value } ?: REP_BASED
        }

        fun useTime(o: ExerciseMeasureType): Boolean {
            return (o != REP_BASED)
        }
    }
}


@Entity
data class Exercise(
    @PrimaryKey val exerciseId: String,
    val title: String,
    val description: String,
    val type: String,
    val bodyPart: String,
    val equipment: String,
    val level: String,
    val rating: Float,
    val ratingDescription: String,
    val videoPath: String,
    val measureType: ExerciseMeasureType,
    val perSide: Boolean
)


@Entity
data class ExerciseSetDataObj(
    val value: Int,
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var isDone: Boolean = false,
    var distance: Int? = null,
    var restTime: Int = 0
)

class ExerciseMeasureTypeAdapter : JsonDeserializer<ExerciseMeasureType>, JsonSerializer<ExerciseMeasureType> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExerciseMeasureType? {
        return json?.asInt?.let { ExerciseMeasureType.fromValue(it) }
    }

    override fun serialize(src: ExerciseMeasureType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.value)
    }
}


class SetListConverter {
    @TypeConverter
    fun fromSetList(list: MutableList<ExerciseSetDataObj>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toSetList(data: String): MutableList<ExerciseSetDataObj>? {
        return Gson().fromJson(data, object : TypeToken<MutableList<ExerciseSetDataObj>>() {}.type)
    }
}

class ExerciseMeasureTypeConverter {

    @TypeConverter
    fun fromExerciseMeasureType(type: ExerciseMeasureType): Int {
        return type.value
    }

    @TypeConverter
    fun toExerciseMeasureType(value: Int): ExerciseMeasureType {
        return ExerciseMeasureType.fromValue(value)
    }
}

class ActiveExerciseListConverter {
    @TypeConverter
    fun fromList(exercises: MutableList<ActiveExercise>): String {
        val gson = Gson()
        return gson.toJson(exercises)
    }

    @TypeConverter
    fun toList(json: String): MutableList<ActiveExercise> {
        val gson = Gson()
        val type = object : TypeToken<List<ActiveExercise>>() {}.type
        return gson.fromJson(json, type)
    }
}


@Dao
interface ActiveExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ActiveExercise>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ActiveExercise)

    @Query("SELECT * FROM ActiveExercise")
    fun getAllAsFlow(): Flow<List<ActiveExercise>>

    @Update
    suspend fun update(exercise: ActiveExercise)

    @Delete
    suspend fun delete(exercise: ActiveExercise)

    @Query("SELECT * FROM ActiveExercise WHERE id = :id")
    suspend fun getById(id: String): ActiveExercise?

    @Query("SELECT * FROM ActiveExercise")
    suspend fun getAll(): List<ActiveExercise>

    @Query("SELECT COUNT(*) FROM ActiveExercise")
    suspend fun size(): Int
}


@Entity
data class ActiveExercise(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @Embedded val exercise: Exercise,
    var sets: Int,
    var setsDone: Int,
    var superset: SuperSet? = null,
    @TypeConverters(SetListConverter::class) var inset: MutableList<ExerciseSetDataObj>? = null,
    @TypeConverters(SetListConverter::class) var weight: MutableList<ExerciseSetDataObj>? = null,
    var isDone: Boolean = false,
    var restTime: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActiveExercise

        if (exercise != other.exercise) return false
        if (inset != other.inset) return false
        if (setsDone != other.setsDone) return false
        if (inset != other.inset) return false
        if (weight != other.weight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exercise.hashCode()
        result = 31 * result + sets
        result = 31 * result + setsDone
        result = 31 * result + (inset?.hashCode() ?: 0)
        result = 31 * result + weight.hashCode()
        return result
    }
}

data class ExerciseFilter(
    val muscleGroup: String? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val term: String? = null
)

data class CategoryData(
    val field: String,
    val categories: List<String>
)
