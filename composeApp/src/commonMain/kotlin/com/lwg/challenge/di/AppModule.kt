package com.lwg.challenge.di

import com.lwg.challenge.data.di.DataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DataModule::class])
@ComponentScan("com.lwg.challenge")
class AppModule
