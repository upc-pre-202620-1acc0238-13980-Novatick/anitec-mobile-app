package pe.novatick.anitec.ui.screens.veterinarian

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.novatick.anitec.ui.components.VeterinarianBottomBar
import pe.novatick.anitec.ui.navigation.VeterinarianTab

@Composable
fun VeterinarianMainContainer(
    onNavigateToLogin: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(VeterinarianTab.HOME) }
    var currentScreen by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            VeterinarianBottomBar(
                currentTab = activeTab,
                onTabSelected = { selectedTab ->
                    activeTab = selectedTab
                    currentScreen = when (selectedTab) {
                        VeterinarianTab.HOME -> "home"
                        VeterinarianTab.AGENDA -> "agenda"
                        VeterinarianTab.RANCHERS -> "ranchers"
                        VeterinarianTab.PROFILE -> "profile"
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                // PANTALLA PRINCIPAL INICIO
                "home" -> {
                    activeTab = VeterinarianTab.HOME
                    VeterinarianHomeScreen(
                        onNavigateToAgenda = {
                            activeTab = VeterinarianTab.AGENDA
                            currentScreen = "agenda"
                        },
                        onNavigateToRanchers = {
                            activeTab = VeterinarianTab.RANCHERS
                            currentScreen = "ranchers"
                        },
                        onNavigateToInvites = {
                            activeTab = VeterinarianTab.HOME
                            currentScreen = "invites"
                        },
                        onNavigateToScheduleVisit = {
                            activeTab = VeterinarianTab.AGENDA
                            currentScreen = "schedule_visit"
                        },
                        onNavigateToRegisterAttention = {
                            activeTab = VeterinarianTab.HOME
                            currentScreen = "register_attention"
                        },
                        onNavigateToVisitDetail = { visitId ->
                            activeTab = VeterinarianTab.AGENDA
                            currentScreen = "visit_detail"
                        }
                    )
                }

                // VISTAS ASOCIADAS A AGENDA
                "agenda" -> {
                    activeTab = VeterinarianTab.AGENDA
                    VeterinarianAgendaScreen(
                        onNavigateToScheduleVisit = {
                            currentScreen = "schedule_visit"
                        },
                        onNavigateToVisitDetail = { visitId ->
                            currentScreen = "visit_detail"
                        }
                    )
                }
                "schedule_visit" -> {
                    activeTab = VeterinarianTab.AGENDA
                    VeterinarianScheduleVisitScreen(
                        onBackClick = {
                            currentScreen = "agenda"
                        },
                        onSaveSuccess = {
                            currentScreen = "agenda"
                        }
                    )
                }
                "visit_detail" -> {
                    activeTab = VeterinarianTab.AGENDA
                    VeterinarianVisitDetailScreen(
                        onBackClick = {
                            currentScreen = "agenda"
                        },
                        onRegisterAttentionClick = {
                            currentScreen = "agenda"
                        }
                    )
                }

                // VISTA GANADEROS VINCULADOS
                "ranchers" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherScreen(
                        onNavigateToRancherDetail = { rancherId ->
                            currentScreen = "rancher_detail"
                        },
                        onNavigateToInvites = {
                            activeTab = VeterinarianTab.HOME
                            currentScreen = "invites"
                        }
                    )
                }
                "rancher_detail" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherDetailScreen(
                        onBackClick = {
                            currentScreen = "ranchers"
                        },
                        onUnlinkClick = {
                            currentScreen = "ranchers"
                        },
                        onNavigateToAnimals = {
                            currentScreen = "rancher_animals"
                        }
                    )
                }
                "rancher_animals" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalsScreen(
                        onBackClick = {
                            currentScreen = "rancher_detail"
                        },
                        onNavigateToAnimalDetail = { animalId ->
                            currentScreen = "rancher_animal_detail"
                        }
                    )
                }
                "rancher_animal_detail" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalDetailScreen(
                        onBackClick = {
                            currentScreen = "rancher_animals"
                        },
                        onNavigateToObservations = {
                            currentScreen = "rancher_animal_observations"
                        },
                        onNavigateToHistory = {
                            currentScreen = "rancher_animal_history"
                        },
                        onNavigateToHistoryDetail = { historyId ->
                            currentScreen = "rancher_animal_history_detail"
                        },
                        onNavigateToRegisterAttention = {
                            currentScreen = "rancher_animal_register_attention"
                        }
                    )
                }

                // VISTAS DERIVADAS DEL DETALLE DEL ANIMAL
                "rancher_animal_observations" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalObservationsScreen(
                        onBackClick = {
                            currentScreen = "rancher_animal_detail"
                        }
                    )
                }
                "rancher_animal_history" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalHistoryScreen(
                        onBackClick = {
                            currentScreen = "rancher_animal_detail"
                        },
                        onNavigateToHistoryDetail = { historyId ->
                            currentScreen = "rancher_animal_history_detail"
                        }
                    )
                }
                "rancher_animal_history_detail" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalHistoryDetailScreen(
                        onBackClick = {
                            currentScreen = "rancher_animal_history"
                        },
                        onNavigateToIndications = {
                            currentScreen = "rancher_animal_indication"
                        },
                        onNavigateToEdit = {
                            currentScreen = "rancher_animal_register_attention"
                        }
                    )
                }
                "rancher_animal_indication" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalIndicationScreen(
                        onBackClick = {
                            currentScreen = "rancher_animal_history_detail"
                        },
                        onNavigateToHistoryDetail = {
                            currentScreen = "rancher_animal_history_detail"
                        }
                    )
                }
                "rancher_animal_register_attention" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRancherAnimalRegisterAttentionScreen(
                        onBackClick = {
                            currentScreen = "rancher_animal_detail"
                        },
                        onSaveAttentionSuccess = {
                            currentScreen = "rancher_animal_detail"
                        }
                    )
                }

                // VISTAS SECUNDARIAS DESDE INICIO
                "invites" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianInviteScreen(
                        onBackClick = {
                            currentScreen = "ranchers"
                        }
                    )
                }
                "register_attention" -> {
                    activeTab = VeterinarianTab.RANCHERS
                    VeterinarianRegisterAttentionScreen(
                        onBackClick = {
                            currentScreen = "home"
                        },
                        onSaveSuccess = {
                            currentScreen = "home"
                        }
                    )
                }

                // VISTA PERFIL / MI CUENTA
                "profile" -> {
                    activeTab = VeterinarianTab.PROFILE
                    VeterinarianProfileScreen(
                        onLogoutClick = {
                            onNavigateToLogin()
                        }
                    )
                }
            }
        }
    }
}