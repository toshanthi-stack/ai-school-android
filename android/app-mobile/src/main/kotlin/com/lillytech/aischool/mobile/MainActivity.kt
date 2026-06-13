package com.lillytech.aischool.mobile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lillytech.aischool.mobile.ui.SyllabusUiState
import com.lillytech.aischool.mobile.ui.SyllabusViewModel
import com.lillytech.aischool.mobile.ui.screens.CategoryListScreen
import com.lillytech.aischool.mobile.ui.screens.CourseDetailScreen
import com.lillytech.aischool.mobile.ui.screens.CourseListScreen
import com.lillytech.aischool.mobile.ui.screens.LessonScreen
import com.lillytech.aischool.mobile.ui.theme.AiSchoolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiSchoolTheme {
                AiSchoolNavHost()
            }
        }
    }
}

private object Routes {
    const val CATEGORIES = "categories"
    const val CATEGORY = "category/{category}"
    const val COURSE = "course/{courseId}"
    const val LESSON = "lesson/{lessonId}"

    fun category(category: String) = "category/${Uri.encode(category)}"
    fun course(courseId: String) = "course/$courseId"
    fun lesson(lessonId: String) = "lesson/$lessonId"
}

@Composable
fun AiSchoolNavHost(viewModel: SyllabusViewModel = viewModel()) {
    val navController = rememberNavController()
    // Recompose route content once the syllabus arrives.
    val state by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = Routes.CATEGORIES) {
        composable(Routes.CATEGORIES) {
            CategoryListScreen(
                viewModel = viewModel,
                onCategoryClick = { navController.navigate(Routes.category(it)) },
            )
        }
        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
        ) { backStackEntry ->
            val category = Uri.decode(backStackEntry.arguments?.getString("category").orEmpty())
            CourseListScreen(
                viewModel = viewModel,
                category = category,
                onCourseClick = { navController.navigate(Routes.course(it)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.COURSE,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val course = (state as? SyllabusUiState.Ready)
                ?.courses
                ?.firstOrNull { it.id == courseId }
            if (course != null) {
                CourseDetailScreen(
                    course = course,
                    onLessonClick = { navController.navigate(Routes.lesson(it)) },
                    onBack = { navController.popBackStack() },
                )
            } else {
                LoadingBox()
            }
        }
        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            val found = viewModel.lesson(lessonId)
            if (found != null) {
                LessonScreen(
                    course = found.first,
                    lesson = found.second,
                    onBack = { navController.popBackStack() },
                )
            } else {
                LoadingBox()
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
