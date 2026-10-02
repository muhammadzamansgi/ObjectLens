package com.objectlens.app.ml

data class Prediction(val label: String, val confidence: Float) {
    val category: Category get() = Category.of(label)
}

enum class Category(val title: String) {
    DOG("Dog"), CAT("Cat"), VEHICLE("Vehicle"), ANIMAL("Animal"), OBJECT("Object");

    companion object {
        private val vehicleWords = listOf(
            "car", "cab", "taxi", "van", "truck", "jeep", "limousine", "minivan", "pickup",
            "convertible", "racer", "wagon", "bus", "trolley", "motor", "scooter", "bike",
            "bicycle", "moped", "tractor", "ambulance", "fire engine", "police", "tank",
            "airliner", "airplane", "warplane", "boat", "ship", "liner", "canoe", "yacht",
            "train", "locomotive", "streetcar", "tram", "snowmobile", "go-kart"
        )
        private val dogWords = listOf(
            "retriever", "terrier", "spaniel", "hound", "poodle", "shepherd", "bulldog", "husky",
            "collie", "beagle", "corgi", "rottweiler", "dalmatian", "chihuahua", "pug", "boxer",
            "mastiff", "setter", "pointer", "schnauzer", "pinscher", "malamute", "samoyed",
            "labrador", "papillon", "pomeranian", "maltese", "shih-tzu", "basenji", "akita",
            "dingo", "dhole", "doberman", "great dane", "saint bernard", "newfoundland",
            "chow", "keeshond", "borzoi", "whippet", "greyhound", "sheepdog", "kelpie"
        )
        private val catWords = listOf("tabby", "tiger cat", "persian cat", "siamese cat", "egyptian cat")
        private val animalWords = listOf(
            "lion", "tiger", "leopard", "jaguar", "cheetah", "bear", "wolf", "fox", "horse",
            "zebra", "elephant", "monkey", "bird", "eagle", "owl", "parrot", "fish", "shark",
            "snake", "lizard", "frog", "turtle", "cow", "ox", "sheep", "goat", "deer", "rabbit",
            "hare", "squirrel", "hamster", "cat", "dog", "penguin", "swan", "duck", "goose"
        )

        fun of(label: String): Category {
            val l = label.lowercase()
            return when {
                catWords.any { l.contains(it) } -> CAT
                dogWords.any { l.contains(it) } -> DOG
                vehicleWords.any { w -> Regex("\\b${Regex.escape(w)}").containsMatchIn(l) } -> VEHICLE
                animalWords.any { l.contains(it) } -> ANIMAL
                else -> OBJECT
            }
        }
    }
}
