package com.ion606.workoutapp.dataObjects

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity
data class SuperSet(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @TypeConverters(ActiveExerciseListConverter::class)
    var exercises: MutableList<ActiveExercise> = mutableListOf(),
    var isDone: Boolean = false,
    var isSingleExercise: Boolean = false,
    var currentExerciseIndex: Int = 0
) {
    init {
        isSingleExercise = exercises.size == 1
    }

    fun updateExercise(exercise: ActiveExercise) {
        val index = exercises.indexOfFirst { it.id == exercise.id }
        if (index != -1) {
            exercises[index] = exercise.copy()
        }
    }

    // returns true if the superset is done
    fun removeExercise(exercise: ActiveExercise): Boolean {
        exercises.removeIf { it.id == exercise.id }
        this.isSingleExercise = (exercises.size == 1)
        return exercises.isEmpty()
    }

    fun getCurrentExercise(): ActiveExercise? {
        return exercises.getOrNull(currentExerciseIndex)
    }

    fun setCurrentExercise(exercise: ActiveExercise): Boolean {
        val tempind = this.exercises.indexOfFirst { it.id == exercise.id }
        if (tempind == -1) {
            Log.d(
                "EXERCISE",
                "exercise with id ${exercise.id} not found in superset with id ${this.id}"
            )
            return false
        }
        this.currentExerciseIndex = tempind
        return true
    }

    fun addExercise(exercise: ActiveExercise, index: Int = -1) {
        if (index == -1) this.exercises.add(exercise)
        else this.exercises.add(index, exercise);
        this.isSingleExercise = (this.exercises.size == 1)
    }

    fun isOnLastExercise(): Boolean {
        return (this.isSingleExercise || (this.currentExerciseIndex == this.exercises.size - 1))
    }

    fun isOnFirstExercise(): Boolean {
        return (this.isSingleExercise || (this.currentExerciseIndex == 0))
    }

    private fun advanceExercisePointer() {
        if (this.currentExerciseIndex >= this.exercises.size - 1) this.currentExerciseIndex = 0
        else this.currentExerciseIndex++
    }

    // the only case where this would return null is if the SUPERSET is done
    fun goToNextExercise(): ActiveExercise? {
        if (this.isSingleExercise) {
            return if (this.isDone) null else this.getCurrentExercise()
        }

        var foundNotDone = false
        for (i in 0 until this.exercises.size) {
            advanceExercisePointer()
            if (getCurrentExercise()?.isDone == false) {
                foundNotDone = true
                break
            }
        }

        // done handling happens in caller
        if (!foundNotDone) return null

        // if we are on the last exercise, go to the first exercise
        this.getCurrentExercise()?.let { this.setCurrentExercise(it) }
        return this.getCurrentExercise()
    }

    // export all exercises without the `exercise` object (swap with ID)
    fun export() {

    }
}


@Dao
interface SuperSetDao {
    // insert a single superset
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(superSet: SuperSet)

    // insert multiple supersets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(superSets: List<SuperSet>)

    // get all supersets as a flow
    @Query("SELECT * FROM SuperSet")
    fun getAllAsFlow(): Flow<List<SuperSet>>

    @Query("SELECT * FROM SuperSet WHERE id = :id")
    suspend fun getSuperSetById(id: String): SuperSet?

    // get all supersets
    @Query("SELECT * FROM SuperSet")
    suspend fun getAll(): List<SuperSet>

    // update a superset
    @Update
    suspend fun update(superSet: SuperSet)

    // delete a single superset
    @Delete
    suspend fun delete(superSet: SuperSet)

    @Query("DELETE FROM SuperSet")
    suspend fun deleteAll()

    // get the total count of supersets
    @Query("SELECT COUNT(*) FROM SuperSet")
    suspend fun size(): Int
}


@Database(
    entities = [ActiveExercise::class, Exercise::class, ExerciseSetDataObj::class, SuperSet::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(SetListConverter::class, SuperSetConverter::class, ActiveExerciseListConverter::class, ExerciseMeasureTypeConverter::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun activeExerciseDao(): ActiveExerciseDao
    abstract fun superSetDao(): SuperSetDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getInstance(context: Context): WorkoutDatabase? {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun saveOrUpdateSuperSet(
    superSet: SuperSet,
    dao: SuperSetDao
) {
    val existingSuperSet = dao.getSuperSetById(superSet.id)
    if (existingSuperSet != null && superSet != existingSuperSet) {
        dao.update(superSet) // update the existing superSet
    } else {
        dao.insert(superSet) // insert a new superSet
    }
}

suspend fun saveAllSuperSets(superSets: List<SuperSet>, dao: SuperSetDao) {
    for (superSet in superSets) {
        saveOrUpdateSuperSet(superSet, dao)
    }
}

class SuperSetConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromSuperSet(superSet: SuperSet?): String? {
        return gson.toJson(superSet)
    }

    @TypeConverter
    fun toSuperSet(data: String?): SuperSet? {
        return gson.fromJson(data, SuperSet::class.java)
    }
}

