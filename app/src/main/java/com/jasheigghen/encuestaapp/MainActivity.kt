package com.jasheigghen.encuestaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.jasheigghen.encuestaapp.ui.theme.EncuestaAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository = SurveyRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncuestaAppTheme {
                MainScreen(repository, auth)
            }
        }
    }
}

val categories = listOf(
    Category("Medio Ambiente", "¿Cuánto amas el medio ambiente?", Icons.Default.Park),
    Category("Política", "Elecciones y partidos políticos", Icons.Default.HowToVote),
    Category("Ciencia", "Temas de Física y Química", Icons.Default.Science),
    Category("Social", "Temas de impacto social y comunidad", Icons.Default.Groups),
    Category("Clima", "Calentamiento global y cambio climático", Icons.Default.WbSunny)
)

@Composable
fun MainScreen(repository: SurveyRepository, auth: FirebaseAuth) {
    val navController = rememberNavController()
    val startDestination = if (auth.currentUser != null) "categories" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController, auth) }
        composable("register") { RegisterScreen(navController, auth) }
        composable("categories") { CategoriesScreen(navController, auth) }
        composable("surveys/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            UserSurveysScreen(navController, repository, auth, category)
        }
        composable("admin_surveys") { AdminSurveysScreen(navController, repository) }
        composable("create_survey") { CreateSurveyScreen(navController, repository) }
        composable("results/{surveyId}") { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId") ?: ""
            ResultsScreen(navController, repository, surveyId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController, auth: FirebaseAuth) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                actions = {
                    IconButton(onClick = { navController.navigate("admin_surveys") }) {
                        Icon(Icons.Default.Assessment, contentDescription = "Admin")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") { popUpTo(0) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        navController.navigate("surveys/${category.name}")
                    },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Column {
                            Text(category.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(category.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text("Bienvenido", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("categories") { popUpTo("login") { inclusive = true } }
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar Sesión")
            }
            TextButton(onClick = { navController.navigate("register") }) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("categories") { popUpTo("register") { inclusive = true } }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSurveysScreen(navController: NavController, repository: SurveyRepository, auth: FirebaseAuth, categoryName: String) {
    var surveys by remember { mutableStateOf<List<Survey>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val category = categories.find { it.name == categoryName }

    LaunchedEffect(categoryName) {
        surveys = repository.getActiveSurveys().filter { it.category == categoryName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(category?.description ?: "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Si quieres saber tu conocimiento respecto a ello, te invito a desarrollar la encuesta.", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(24.dp))

            if (surveys.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay encuestas para esta categoría.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(surveys) { survey ->
                        SurveyCard(survey) { selectedIndex ->
                            scope.launch { repository.submitVote(Vote(survey.id, selectedIndex)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurveyCard(survey: Survey, onVote: (Int) -> Unit) {
    var voted by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = survey.question, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (!voted) {
                survey.options.forEachIndexed { index, option ->
                    Button(
                        onClick = { voted = true; onVote(index) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) { Text(option) }
                }
            } else {
                Text("¡Gracias por votar!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSurveysScreen(navController: NavController, repository: SurveyRepository) {
    var surveys by remember { mutableStateOf<List<Survey>>(emptyList()) }
    LaunchedEffect(Unit) { surveys = repository.getActiveSurveys() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("create_survey") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(surveys) { survey ->
                Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("results/${survey.id}") }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = survey.question, fontWeight = FontWeight.Bold)
                        Text(text = "Categoría: ${survey.category}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(navController: NavController, repository: SurveyRepository) {
    var question by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0].name) }
    var options by remember { mutableStateOf(listOf("", "")) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("Nueva Encuesta") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Pregunta") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Text("Categoría:")
            categories.forEach { cat ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedCategory = cat.name }) {
                    RadioButton(selected = selectedCategory == cat.name, onClick = { selectedCategory = cat.name })
                    Text(cat.name)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            options.forEachIndexed { index, option ->
                OutlinedTextField(
                    value = option,
                    onValueChange = { nv -> val l = options.toMutableList(); l[index] = nv; options = l },
                    label = { Text("Opción ${index + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(onClick = {
                scope.launch {
                    repository.createSurvey(Survey(question = question, options = options.filter { it.isNotBlank() }, category = selectedCategory))
                    navController.popBackStack()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Crear") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavController, repository: SurveyRepository, surveyId: String) {
    var results by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var survey by remember { mutableStateOf<Survey?>(null) }
    LaunchedEffect(surveyId) {
        val all = repository.getActiveSurveys()
        survey = all.find { it.id == surveyId }
        results = repository.getResults(surveyId)
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Resultados") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            survey?.let { s ->
                Text(s.question, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                s.options.forEachIndexed { i, opt ->
                    val v = results[i] ?: 0
                    Text("$opt: $v votos")
                }
            }
        }
    }
}
