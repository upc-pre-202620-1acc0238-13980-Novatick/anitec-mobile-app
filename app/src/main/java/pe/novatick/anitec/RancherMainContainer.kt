package pe.novatick.anitec.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pe.novatick.anitec.RancherCattleScreen
import pe.novatick.anitec.RancherHomeScreen
import pe.novatick.anitec.RancherRegisterAnimalScreen
import pe.novatick.anitec.ui.components.RancherBottomBar
import pe.novatick.anitec.ui.navigation.RancherTab

@Composable
fun RancherMainContainer(
    onNavigateToLogin: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf(RancherTab.HOME) }
    var currentScreen by remember { mutableStateOf("home") }
    var selectedAnimalId by remember { mutableStateOf<String?>(null) }

    // Estado para recordar la pantalla previa antes de crear una observación
    var previousScreenForObservation by remember { mutableStateOf("animal_detail") }
    var previousScreenForHistoryDetail by remember { mutableStateOf("animal_detail") }
    var previousScreenForIndicationDetail by remember { mutableStateOf("home") }
    var previousScreenForVisitDetail by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
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
                        previousScreenForIndicationDetail = "home"
                        activeTab = RancherTab.CATTLE
                        currentScreen = "indication_detail"
                    }
                )

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

                "register_animal" -> {
                    activeTab = RancherTab.CATTLE
                    RancherRegisterAnimalScreen(
                        onBackClick = { currentScreen = "cattle" },
                        onSaveSuccess = {
                            activeTab = RancherTab.CATTLE
                            currentScreen = "cattle"
                        }
                    )
                }

                "animal_detail" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalDetailScreen(
                        animalId = selectedAnimalId,
                        onBackClick = { currentScreen = "cattle" },
                        onNavigateToObservations = { currentScreen = "animal_observations" },
                        onNavigateToNewObservation = { currentScreen = "animal_new_observation" },
                        onNavigateToHistory = { currentScreen = "animal_history" },
                        onNavigateToHistoryDetail = { currentScreen = "animal_history_detail" },
                        onNavigateToEditAnimal = { currentScreen = "edit_animal" },
                        onConfirmDeactivate = { currentScreen = "cattle" }
                    )
                }

                "animal_observations" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalObservationsScreen(
                        onBackClick = {
                            currentScreen = "animal_detail"
                        },
                        onNavigateToNewObservation = {
                            currentScreen = "animal_new_observation"
                        }
                    )
                }
                "animal_new_observation" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalNewObservationScreen(
                        onNavigateBack = {
                            currentScreen = previousScreenForObservation
                        }
                    )
                }
                "animal_history" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalHistoryScreen(
                        onBackClick = {
                            currentScreen = "animal_detail"
                        },
                        onNavigateToHistoryDetail = { historyId ->
                            currentScreen = "animal_history_detail"
                        }
                    )
                }
                "animal_history_detail" -> {
                    activeTab = RancherTab.CATTLE
                    RancherAnimalHistoryDetailScreen(
                        onBackClick = {
                            currentScreen = previousScreenForHistoryDetail
                        },
                        onNavigateToIndicationDetail = {
                            currentScreen = "indication_detail"
                        }
                    )
                }

                "indication_detail" -> {
                    activeTab = RancherTab.CATTLE
                    RancherIndicationDetailScreen(
                        onBackClick = {
                            currentScreen = previousScreenForIndicationDetail
                            if (previousScreenForIndicationDetail == "home") {
                                activeTab = RancherTab.HOME
                            }
                        }
                    )
                }

                "edit_animal" -> {
                    activeTab = RancherTab.CATTLE
                    RancherEditAnimalScreen(
                        onBackClick = {
                            currentScreen = "animal_detail"
                        },
                        onSaveSuccess = {
                            currentScreen = "animal_detail"
                        }
                    )
                }

                "visits" -> {
                    activeTab = RancherTab.HOME
                    RancherVisitsScreen(
                        onBackClick = {
                            currentScreen = "home"
                        },
                        onNavigateToVisitDetail = { visitId ->
                            currentScreen = "visit_detail"
                        }
                    )
                }

                "visit_detail" -> {
                    activeTab = RancherTab.HOME
                    RancherVisitDetailScreen(
                        onBackClick = {
                            currentScreen = previousScreenForVisitDetail
                        }
                    )
                }

                "invite_vets" -> {
                    activeTab = RancherTab.HOME
                    RancherInviteVetsScreen(
                        onBackClick = {
                            currentScreen = "home"
                        },
                        onSendSuccess = {
                            currentScreen = "home"
                        }
                    )
                }

                "vets" -> {
                    activeTab = RancherTab.VETS
                    RancherVetsScreen(
                        onInviteVetClick = {
                            activeTab = RancherTab.HOME
                            currentScreen = "invite_vets"
                        },
                        onRemoveVetClick = {
                            activeTab = RancherTab.HOME
                            currentScreen = "home"
                        },
                        onCancelInviteClick = {
                            activeTab = RancherTab.HOME
                            currentScreen = "home"
                        }
                    )
                }

                "profile" -> {
                    activeTab = RancherTab.PROFILE
                    RancherProfileScreen(
                        onLogoutClick = {
                            onNavigateToLogin()
                        }
                    )
                }
            }
        }
    }
}