package com.cotemig.receitas.data

object Traducoes {

    private val categorias = mapOf(
        "Beef" to "Carne bovina",
        "Breakfast" to "Café da manhã",
        "Chicken" to "Frango",
        "Dessert" to "Sobremesa",
        "Goat" to "Cabrito",
        "Lamb" to "Cordeiro",
        "Miscellaneous" to "Variados",
        "Pasta" to "Massas",
        "Pork" to "Porco",
        "Seafood" to "Frutos do mar",
        "Side" to "Acompanhamento",
        "Starter" to "Entrada",
        "Vegan" to "Vegano",
        "Vegetarian" to "Vegetariano"
    )

    private val areas = mapOf(
        "American" to "Americana",
        "British" to "Britânica",
        "Canadian" to "Canadense",
        "Chinese" to "Chinesa",
        "Croatian" to "Croata",
        "Dutch" to "Holandesa",
        "Egyptian" to "Egípcia",
        "Filipino" to "Filipina",
        "French" to "Francesa",
        "Greek" to "Grega",
        "Indian" to "Indiana",
        "Irish" to "Irlandesa",
        "Italian" to "Italiana",
        "Jamaican" to "Jamaicana",
        "Japanese" to "Japonesa",
        "Kenyan" to "Queniana",
        "Malaysian" to "Malaia",
        "Mexican" to "Mexicana",
        "Moroccan" to "Marroquina",
        "Norwegian" to "Norueguesa",
        "Polish" to "Polonesa",
        "Portuguese" to "Portuguesa",
        "Russian" to "Russa",
        "Spanish" to "Espanhola",
        "Thai" to "Tailandesa",
        "Tunisian" to "Tunisiana",
        "Turkish" to "Turca",
        "Ukrainian" to "Ucraniana",
        "Uruguayan" to "Uruguaia",
        "Vietnamese" to "Vietnamita",
        "Unknown" to "Desconhecida"
    )

    fun categoria(nome: String): String = categorias[nome] ?: nome

    fun area(nome: String): String = areas[nome] ?: nome
}
