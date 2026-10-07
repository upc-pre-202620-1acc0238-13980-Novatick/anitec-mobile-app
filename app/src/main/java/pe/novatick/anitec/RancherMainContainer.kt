package pe.novatick.anitec

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.novatick.anitec.ui.components.RancherBottomBar
import pe.novatick.anitec.ui.navigation.RancherTab

@Composable
fun RancherMainContainer(
    onNavigateToLogin: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(RancherTab.HOME) }
    var currentScreen by remember { mutableStateOf("home") }
    var selectedAnimalId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            // BARRA DE NAVEGACIÓN ESTÁTICA ENTRE VISTAS
            RancherBottomBar(
                currentTab = activeTab,
                onTabSelected = { selectedTab ->
                    activeTab = selectedTab
                    currentScreen = when (selectedTab) {
                        RancherTab.HOME -> "home"
                        RancherTab.CATTLE -> "cattle"
                        RancherTab.VETS -> "vets"
                        RancherTab.PROFILE -> "profile"
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                // Vista Inicio
                "home" -> RancherHomeScreen(
                    onNavigateToCattle = {
                        activeTab = RancherTab.CATTLE
                        currentScreen = "cattle"
                    },
                    onNavigateToLinkedVets = {
                        activeTab = RancherTab.VETS
                        currentScreen = "linked_vets"
                    },
                    onNavigateToRegisterAnimal = {
                        activeTab = RancherTab.CATTLE
                        currentScreen = "register_animal"
                    },
                    onNavigateToInviteVets = {
                        activeTab = RancherTab.VETS
                        currentScreen = "invite_vets"
                    },
                    onNavigateToVisits = {
                        activeTab = RancherTab.HOME
                        currentScreen = "visits"
                    },
                    onNavigateToVisitDetail = {
                        activeTab = RancherTab.HOME
                        currentScreen = "visit_detail"
                    },
                    onNavigateToIndicationDetail = {
                        activeTab = RancherTab.CATTLE
                        currentScreen = "indication_detail"
                    }
                )

                // Vista Mi Ganado (Activa ic_cow.svg)
                "cattle" -> RancherCattleScreen(
                    onNavigateToRegisterAnimal = {
                        activeTab = RancherTab.CATTLE
                        currentScreen = "register_animal"
                    },
                    onNavigateToAnimalDetail = { animalId ->
                        selectedAnimalId = animalId
                        activeTab = RancherTab.CATTLE
                        currentScreen = "animal_detail"
                    }
                )

                // VISTA REGISTRAR ANIMAL (Mantiene activo ic_cow.svg)
                "register_animal" -> {
                    activeTab = RancherTab.CATTLE
                    RancherRegisterAnimalScreen(
                        onBackClick = {
                            currentScreen = "cattle"
                        },
                        onSaveSuccess = {
                            activeTab = RancherTab.CATTLE
                            currentScreen = "cattle"
                        }
                    )
                }

                // VISTA DETALLE ANIMAL (Mantiene activo ic_cow.svg)
                "animal_detail" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalDetailScreen(
                        animalId = selectedAnimalId,
                        onBackClick = {
                            currentScreen = "cattle"
                        }
                    )
                }

                "linked_vets" -> RancherLinkedVetsScreen()
                "invite_vets" -> RancherInviteVetsScreen()
                "visits" -> RancherVisitsScreen()
                "visit_detail" -> RancherVisitDetailScreen()
                "indication_detail" -> RancherIndicationDetailScreen()
                "vets" -> RancherVetsScreen()
                "profile" -> RancherProfileScreen()
            }
        }
    }
}