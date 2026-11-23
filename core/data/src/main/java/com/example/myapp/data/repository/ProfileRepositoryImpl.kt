package com.example.myapp.data.repository

import com.example.myapp.data.local.profile.AvatarStorage
import com.example.myapp.data.local.profile.ProfileFileDataSource
import com.example.myapp.domain.model.Profile
import com.example.myapp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

/** Реализация хранилища профиля: JSON-файл + копирование аватарки. */
class ProfileRepositoryImpl(
    private val dataSource: ProfileFileDataSource,
    private val avatarStorage: AvatarStorage,
) : ProfileRepository {

    override fun observeProfile(): Flow<Profile> = dataSource.profile

    override suspend fun saveProfile(profile: Profile) = dataSource.save(profile)

    override suspend fun saveAvatar(sourceUri: String): String = avatarStorage.saveFromUri(sourceUri)
}
