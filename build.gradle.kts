plugins {
    application
    id("ecbuild.java-conventions")
}

application {
    mainClass = "net.easecation.playeractionrecorder.PlayerActionRecorder"
}

dependencies {
    implementation(project(":easechat-client-j"))
    implementation(libs.mysql.connector)
    implementation(libs.c3p0)
    implementation(libs.fastutil)
}

description = "player-action-recorder"
