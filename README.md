{
  "appName": "Edify",
  "version": "1.0.0",
  "themeColor": "#BB86FC",
  "categories": [
    { "id": "presets", "title": "Presets", "icon": "palette", "type": "grid" },
    { "id": "apks", "title": "APKs", "icon": "package", "type": "list" }
  ],
  "socialLinks": {
    "youtube": "https://youtube.com/...",
    "telegram": "https://t.me/..."
  },
  "resources": [
    {
      "id": "1",
      "categoryId": "presets",
      "title": "Cinematic Moody",
      "thumbnail": "https://link-to-image.jpg",
      "description": "Professional color grading for cinematic looks.",
      "downloadUrl": "https://link-to-file.xml",
      "tags": ["Lut", "Moody"]
    }
  ]
}private val DarkColorPalette = darkColors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC4),
    background = Color(0xFF121212), // Deep Dark
    surface = Color(0xFF1E1E1E),    // Elevated Dark
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)data class AppConfig(
    val appName: String,
    val categories: List<Category>,
    val resources: List<Resource>,
    val socialLinks: SocialLinks
)

data class Resource(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val thumbnail: String,
    val downloadUrl: String,
    val version: String? = null,
    val size: String? = null,
    val isFeatured: Boolean = false
)@Composable
fun EdifyApp() {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = { 
            AppDrawer(navController, scope, scaffoldState) 
        },
        topBar = {
            MainTopBar(onOpenDrawer = { scope.launch { scaffoldState.drawerState.open() } })
        }
    ) {
        NavHost(navController, startDestination = "home") {
            composable("home") { HomeScreen(navController) }
            composable("category/{catId}") { backStackEntry ->
                val catId = backStackEntry.arguments?.getString("catId")
                ResourceListScreen(catId, navController)
            }
            composable("favorites") { FavoritesScreen(navController) }
            composable("about") { AboutScreen() }
        }
    }
}@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val config by viewModel.appConfig.collectAsState()

    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(config.categories) { category ->
            CategoryCard(category) {
                navController.navigate("category/${category.id}")
            }
        }
    }
}@Composable
fun ResourceCard(resource: Resource, onDownload: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp,
        modifier = Modifier.padding(8.dp).fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = resource.thumbnail,
                contentDescription = null,
                modifier = Modifier.height(150.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(resource.title, style = MaterialTheme.typography.h6)
            Text(resource.description, maxLines = 2)
            Row {
                Button(onClick = onDownload) { Text("Download") }
                IconButton(onClick = { /* Favorite Logic */ }) {
                    Icon(Icons.Default.FavoriteBorder, null)
                }
            }
        }
    }
}
