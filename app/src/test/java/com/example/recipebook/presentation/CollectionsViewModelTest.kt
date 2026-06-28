package com.example.recipebook.presentation

import androidx.annotation.OptIn
import com.example.recipebook.data.local.UserEntities.UserColletcions.CollectionRepository
import com.example.recipebook.utils.MainDispatcherRule
import com.example.recipebook.viewmodel.localRecipes.CollectionsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: CollectionRepository
    private lateinit var viewModel: CollectionsViewModel

    @Before
    fun setUp() {
        repository = mockk()
        coEvery { repository.observeAllCollections() } returns flowOf(emptyList())
        viewModel = CollectionsViewModel(repository)
    }

    @Test
    fun `initial uiState has isLoading true`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `createCollection calls repository`() = runTest {
        coEvery { repository.createCollection(any(), any(), any()) } returns 1L

        viewModel.updateNewCollectionName("My Collection")
        viewModel.createCollection()

        coVerify { repository.createCollection("My Collection", null, "📖") }
    }

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addRecipeToCollection calls repository and shows error on duplicate`() = runTest {
        coEvery { repository.addRecipeToCollection(any(), any()) } returns false

        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect {}
        }

        viewModel.addRecipeToCollection(1, 100)

        coVerify { repository.addRecipeToCollection(1, 100) }

        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        job.cancel()
    }
}