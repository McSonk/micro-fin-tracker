package com.eromn.microfintracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Transaction (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String = "",
    // TODO: Change to BigDecimal
    val amount: Double = 0.0,
    val timestamp: Long = 0,
    @ColumnInfo(name = "is_read", defaultValue = "0")
    val isRead: Boolean = false,
    @ColumnInfo(name = "category_id")
    val categoryId: Int? = Category.OTHERS.serverId
)

enum class Category(
    val serverId: Int,
    val displayName: String,
    val description: String
) {
    TRANSPORT(1, "Transporte", "Gastos de transporte, metro, autobús, bici compartida, etc."),
    UBER(2, "Uber", "Uber u otros taxis de aplicación"),
    RESTAURANTS(3, "Restaurantes", "Restaurantes u otros lugares \"lujosos\" para comer"),
    FOOD(4, "Comida", "Fondas, restaurantes sencillos o alimentos pequeños"),
    ENTERTAINMENT(5, "Entretenimiento", "Salidas al cine, amigos, etc"),
    HEALTH(6, "Salud", "Farmacias, consultas médicas"),
    MAINTENANCE(7, "Mantenimiento", "Gastos relacionados con mantenimiento de la casa u otros objetos"),
    CLOTHING(8, "Ropa", "Compras de ropa y accesorios"),
    TRAVEL(9, "Viajes", "Gastos de viajes y vacaciones"),
    EDUCATION(10, "Educación", "Cursos, libros, colegiaturas"),
    UNFORESEEN(11, "Imprevistos", "Gastos inesperados o emergencias"),
    PERSONAL_CARE(14, "Cuidado personal", "Gastos en peluquería, spa, etc."),
    TAXES(15, "Impuestos", "Pago de impuestos y contribuciones"),
    FINANCES(16, "Finanzas", "Gastos financieros, como comisiones bancarias"),
    TECHNOLOGY(17, "Tecnología", "Gastos en tecnología, como dispositivos electrónicos"),
    DANCE_SPORTS(18, "Danza/Deportes", "Gastos relacionados con clases de danza o deportes específicos, así como entradas a eventos de fiestas sociales de baile"),
    OTHERS(19, "Otros", "Cualquier otro gasto no categorizado"),
    PLATFORMS(20, "Plataformas", "Netflix, Spotify, etc"),
    SERVICES(21, "Servicios", "Luz, agua, gas, etc."),
    DRINKS(25, "Bebidas", "Starbucks, Cielito, cafés, tés, u otro tipo de bebidas"),
    SNACKS(26, "Golosinas", "Dulces, chocolates, y otros antojos"),
    STATIONERY(27, "Papelería", "Artículos de papelería y oficina"),
    PANTRY(28, "Despensa", "Artículos de despensa y alimentos no perecederos"),
    DONATIONS(29, "Donaciones", "Donaciones o limosnas"),
    CONVENIENCE(31, "Conveniencia", "Para las tiendas de conveniencia"),
    HOBBIES(33, "Pasatiempos", "Gastos de ocio y diversión");

    companion object {
        /**
         * Helper to map the DB integer to your Enum.
         * Falls back to OTHERS if the ID is null or not found.
         */
        fun fromId(id: Int?): Category =
            entries.firstOrNull { it.serverId == id } ?: OTHERS
    }
}