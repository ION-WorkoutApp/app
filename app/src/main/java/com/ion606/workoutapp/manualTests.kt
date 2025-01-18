package com.ion606.workoutapp

import com.ion606.workoutapp.managers.SyncManager
import kotlin.system.exitProcess

suspend fun main() {
    // Hardcoded input data for testing
    val o = mapOf(
        "name" to "Itamar Oren-Naftalovich",
        "email" to "ion606@protonmail.com",
        "password" to "password",
        "age" to "22",
        "gender" to "Male",
        "height" to "202",
        "weight" to "80",
        "fitnessGoal" to "Lose Weight",
        "preferredWorkoutType" to "Gym Workouts",
        "comfortLevel" to "Advanced"
    )

//    println("TESTING URL: ${URLHelpers.transformURL("http://testing")}");
//
    val sm = SyncManager()
    val r = sm.sendData(o, "http://129.161.153.4:1221/checkcredentials");
    println(r)

    exitProcess(0);

    // Sync the data
//    syncManager.sendData(testData, "http://192.168.0.131:1221",  )
//    val dm = DataManager(Mockito.mock(Context::class.java));
//    val credentialManager = CredentialManager("http://192.168.0.131:1221", dm);
//
//    val isValid = credentialManager.checkCredentials("user", "pass");
//    if (isValid) {
//        println("Credentials are valid!");
//    }
//    else println("Invalid credentials.");
}
