package pe.novatick.anitec.features.livestock.application
import pe.novatick.anitec.features.livestock.domain.*
class QueryInventory(private val repository: LivestockRepository) {
    suspend operator fun invoke(filter: InventoryFilter)=repository.inventory(filter)
}
class CreateFarm(private val repository: LivestockRepository) {
    suspend operator fun invoke(name: String,location: String?)=repository.createFarm(name,location)
}
