package com.example.data.generator

import kotlin.random.Random

object NameGenerator {
    val MALE_NAMES = listOf(
        "Aeron", "Aldric", "Alvaris", "Ammora", "Amon", "Arkan", "Arthas", "Ashryn", "Askael", "Aurelion",
        "Axton", "Baldor", "Bane", "Barion", "Bastian", "Belric", "Berion", "Blaze", "Bram", "Brion",
        "Byron", "Caelum", "Cairos", "Calix", "Cassian", "Cedric", "Cerion", "Ciro", "Cyrus", "Cyron",
        "Cyval", "Darian", "Darion", "Darius", "Drake", "Draven", "Drex", "Drian", "Drogan", "Duran",
        "Dyron", "Eldric", "Elios", "Elric", "Elvar", "Emrys", "Endric", "Eron", "Ervan", "Ezran",
        "Ezric", "Fael", "Faris", "Fenric", "Feron", "Feyron", "Firon", "Flint", "Frax", "Frey",
        "Fyron", "Gaius", "Garen", "Garrick", "Gideon", "Giron", "Gorath", "Gravis", "Grion", "Grynn",
        "Gyron", "Hadrian", "Halric", "Hector", "Helios", "Hendric", "Heron", "Hirox", "Hyrion", "Hazel",
        "Harkan", "Icarus", "Ignis", "Ilios", "Iron", "Irvan", "Izran", "Izric", "Izor", "Ixion",
        "Izan", "Jarek", "Jaron", "Jax", "Jeron", "Jorik", "Jovan", "Jex", "Jiron", "Jarekion", "Jaxor"
    )

    val FEMALE_NAMES = listOf(
        "Aelira", "Aeris", "Ashira", "Aurelia", "Ayra", "Aylen", "Arcelia", "Aria", "Astrid", "Avelyn",
        "Belira", "Bella", "Berina", "Briselle", "Brynn", "Blaire", "Brelia", "Brina", "Bylena", "Byra",
        "Caelia", "Calira", "Celestia", "Celine", "Cyra", "Cyrene", "Cyris", "Cyvalia", "Cerina", "Cassia",
        "Daelia", "Daria", "Delira", "Delphine", "Dira", "Dione", "Dralia", "Dreya", "Dyra", "Dyne",
        "Elaria", "Elira", "Elowen", "Emira", "Enya", "Eris", "Eryna", "Ezra", "Ezlira", "Evania",
        "Faelira", "Fayra", "Fenira", "Fiora", "Fira", "Freyra", "Fynia", "Fyra", "Felis", "Faria",
        "Gaelia", "Galira", "Gwen", "Gwena", "Gwyra", "Girena", "Gisela", "Glacia", "Griselle", "Gyra",
        "Haelia", "Hanael", "Hazel", "Helia", "Hera", "Hirena", "Hylia", "Hynra", "Hestia", "Hyra",
        "Irelia", "Iris", "Illyra", "Ilena", "Ivra", "Ivyn", "Izra", "Izella", "Isera", "Iryna"
    )

    fun generateName(isMale: Boolean): String {
        val list = if (isMale) MALE_NAMES else FEMALE_NAMES
        val first = list.random()
        var second = list.random()
        while (second == first) {
            second = list.random()
        }
        return "$first $second"
    }
}
